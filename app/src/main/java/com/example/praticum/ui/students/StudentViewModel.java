package com.example.praticum.ui.students;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.praticum.data.database.AppDatabase;
import com.example.praticum.data.database.DatabaseProvider;
import com.example.praticum.data.entity.Student;
import com.example.praticum.data.repository.StudentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudentViewModel extends AndroidViewModel {
    private final StudentRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<Student>> students = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Student> selectedStudent = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public StudentViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = DatabaseProvider.getDatabase(application);
        repository = new StudentRepository(database, database.studentDao());
    }

    public LiveData<List<Student>> getStudents() { return students; }
    public LiveData<Student> getSelectedStudent() { return selectedStudent; }
    public LiveData<String> getError() { return error; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> isLoading() { return loading; }

    public void loadStudents() {
        executor.execute(() -> {
            loading.postValue(true);
            try {
                students.postValue(repository.getAll());
                error.postValue(null);
            } catch (Exception e) {
                error.postValue(getErrorMessage(e));
            } finally {
                loading.postValue(false);
            }
        });
    }

    public void loadStudentById(String studentId) {
        executor.execute(() -> {
            try {
                Student student = repository.getById(studentId);
                selectedStudent.postValue(student);
                if (student == null) error.postValue("Étudiant introuvable.");
                else error.postValue(null);
            } catch (Exception e) {
                error.postValue(getErrorMessage(e));
            }
        });
    }

    public void addStudent(Student student) {
        runWrite(() -> repository.insert(student), "Étudiant ajouté.");
    }

    public void updateStudent(Student student) {
        runWrite(() -> repository.update(student), "Informations de l'étudiant modifiées.");
    }

    public void deleteStudent(Student student) {
        runWrite(() -> repository.delete(student), "Étudiant supprimé.");
    }

    private void runWrite(Runnable action, String successMessage) {
        executor.execute(() -> {
            loading.postValue(true);
            error.postValue(null);
            message.postValue(null);
            try {
                action.run();
                students.postValue(repository.getAll());
                message.postValue(successMessage);
            } catch (Exception e) {
                error.postValue(getErrorMessage(e));
            } finally {
                loading.postValue(false);
            }
        });
    }

    private String getErrorMessage(Exception e) {
        if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
            String detail = e.getMessage();
            if (detail != null && !detail.trim().isEmpty()) return detail;
        }
        return "Une erreur est survenue lors de l'opération. Veuillez réessayer.";
    }

    @Override protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
