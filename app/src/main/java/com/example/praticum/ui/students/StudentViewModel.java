package com.example.praticum.ui.students;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.praticum.data.dao.AcademicYearDao;
import com.example.praticum.data.dao.StudentDao;
import com.example.praticum.data.database.AppDatabase;
import com.example.praticum.data.database.DatabaseProvider;
import com.example.praticum.data.entity.AcademicYear;
import com.example.praticum.data.entity.Student;
import com.example.praticum.data.entity.StudentGroup;
import com.example.praticum.data.repository.StudentGroupRepository;
import com.example.praticum.data.repository.StudentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudentViewModel extends AndroidViewModel {

    private final AppDatabase database;
    private final StudentRepository repository;
    private final StudentGroupRepository groupRepository;
    private final StudentDao studentDao;
    private final AcademicYearDao academicYearDao;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<Student>> students =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<StudentGroup>> groups =
            new MutableLiveData<>(new ArrayList<>());

    // Étudiant sélectionné (détail / modification)
    private final MutableLiveData<Student> selectedStudent =
            new MutableLiveData<>();

    private final MutableLiveData<StudentGroup> selectedGroup =
            new MutableLiveData<>();

    private final MutableLiveData<AcademicYear> selectedAcademicYear =
            new MutableLiveData<>();

    private final MutableLiveData<String> error =
            new MutableLiveData<>();

    private final MutableLiveData<String> message =
            new MutableLiveData<>();

    private final MutableLiveData<Boolean> loading =
            new MutableLiveData<>(false);

    public StudentViewModel(@NonNull Application application) {
        super(application);

        database = DatabaseProvider.getDatabase(application);
        studentDao = database.studentDao();
        academicYearDao = database.academicYearDao();

        repository = new StudentRepository(database, studentDao);
        groupRepository =
                new StudentGroupRepository(database.studentGroupDao());
    }

    public LiveData<List<Student>> getStudents() {
        return students;
    }

    public LiveData<List<StudentGroup>> getGroups() {
        return groups;
    }

    public LiveData<Student> getSelectedStudent() {
        return selectedStudent;
    }

    public LiveData<StudentGroup> getSelectedGroup() {
        return selectedGroup;
    }

    public LiveData<AcademicYear> getSelectedAcademicYear() {
        return selectedAcademicYear;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    /** Charge les étudiants et les groupes (liste et formulaire). */
    public void loadStudents() {
        executor.execute(() -> {
            loading.postValue(true);

            try {
                rafraichirListes();
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

                if (student == null) {
                    selectedStudent.postValue(null);
                    error.postValue("Étudiant introuvable.");
                    return;
                }

                StudentGroup group =
                        groupRepository.getById(student.getGroupId());

                AcademicYear year = group == null
                        ? null
                        : academicYearDao.getById(group.getAcademicYearId());

                // Les valeurs annexes sont publiées avant l'étudiant.
                selectedGroup.postValue(group);
                selectedAcademicYear.postValue(year);
                selectedStudent.postValue(student);

                error.postValue(null);

            } catch (Exception e) {
                error.postValue(getErrorMessage(e));
            }
        });
    }

    public void addStudent(Student student) {
        executor.execute(() -> {
            loading.postValue(true);
            error.postValue(null);
            message.postValue(null);

            try {
                // L'ordre dans le groupe est attribué par le repository.
                repository.insert(student);
                rafraichirListes();

                message.postValue("Étudiant ajouté.");

            } catch (Exception e) {
                error.postValue(getErrorMessage(e));

            } finally {
                loading.postValue(false);
            }
        });
    }

    public void updateStudent(Student student) {
        executor.execute(() -> {
            loading.postValue(true);
            error.postValue(null);
            message.postValue(null);

            try {
                Student existant =
                        repository.getById(student.getStudentId());

                if (existant == null) {
                    throw new IllegalArgumentException(
                            "Étudiant introuvable.");
                }

                if (existant.getGroupId() == student.getGroupId()) {
                    // Même groupe : l'ordre est conservé.
                    student.setGroupOrder(existant.getGroupOrder());
                    repository.update(student);

                } else {
                    // Changement de groupe : prochain ordre du nouveau
                    // groupe (règle de l'ancien StudentsFragment).
                    database.runInTransaction(() -> {
                        Integer maxOrdre = studentDao.getMaxGroupOrder(
                                student.getGroupId());

                        student.setGroupOrder(
                                maxOrdre == null ? 1 : maxOrdre + 1);

                        repository.update(student);
                    });
                }

                rafraichirListes();

                message.postValue("Informations de l'étudiant modifiées.");

            } catch (Exception e) {
                error.postValue(getErrorMessage(e));

            } finally {
                loading.postValue(false);
            }
        });
    }

    public void deleteStudent(Student student) {
        executor.execute(() -> {
            loading.postValue(true);
            error.postValue(null);
            message.postValue(null);

            try {
                repository.delete(student);
                rafraichirListes();

                message.postValue("Étudiant supprimé.");

            } catch (Exception e) {
                error.postValue(getErrorMessage(e));

            } finally {
                loading.postValue(false);
            }
        });
    }

    private void rafraichirListes() {
        List<StudentGroup> listeGroupes = groupRepository.getAll();
        List<Student> listeEtudiants = repository.getAll();

        // Les groupes sont publiés avant les étudiants.
        groups.postValue(
                listeGroupes != null ? listeGroupes : new ArrayList<>());

        students.postValue(
                listeEtudiants != null ? listeEtudiants : new ArrayList<>());
    }

    private String getErrorMessage(Exception e) {
        if (e instanceof IllegalArgumentException
                || e instanceof IllegalStateException) {
            String detail = e.getMessage();

            if (detail != null && !detail.trim().isEmpty()) {
                return detail;
            }
        }

        return "Une erreur est survenue lors de l'opération. "
                + "Veuillez réessayer.";
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}