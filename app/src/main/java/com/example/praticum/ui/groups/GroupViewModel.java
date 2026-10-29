package com.example.praticum.ui.groups;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.praticum.data.dao.AcademicYearDao;
import com.example.praticum.data.dao.SessionGroupDao;
import com.example.praticum.data.dao.StudentGroupDao;
import com.example.praticum.data.database.AppDatabase;
import com.example.praticum.data.database.DatabaseProvider;
import com.example.praticum.data.entity.AcademicYear;
import com.example.praticum.data.entity.StudentGroup;
import com.example.praticum.data.repository.StudentGroupRepository;
import com.example.praticum.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupViewModel extends AndroidViewModel {

    /**
     * Retourne les spécialités définies dans res/values/strings.xml.
     */
    public String[] getSpecializations() {
        return getApplication()
                .getResources()
                .getStringArray(R.array.specializations);
    }

    private final StudentGroupRepository repository;
    private final StudentGroupDao groupDao;
    private final SessionGroupDao sessionGroupDao;
    private final AcademicYearDao academicYearDao;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    // Liste
    private final MutableLiveData<List<StudentGroup>> groups =
            new MutableLiveData<>(new ArrayList<>());

    // Effectifs par identifiant de groupe
    private final MutableLiveData<Map<Integer, Integer>> studentCounts =
            new MutableLiveData<>(new HashMap<>());

    private final MutableLiveData<Map<Integer, Integer>> sessionCounts =
            new MutableLiveData<>(new HashMap<>());

    // Groupe sélectionné (détail / modification)
    private final MutableLiveData<StudentGroup> selectedGroup =
            new MutableLiveData<>();

    private final MutableLiveData<AcademicYear> selectedAcademicYear =
            new MutableLiveData<>();

    private final MutableLiveData<Integer> selectedStudentCount =
            new MutableLiveData<>(0);

    private final MutableLiveData<Integer> selectedSessionCount =
            new MutableLiveData<>(0);

    private final MutableLiveData<String> error =
            new MutableLiveData<>();

    private final MutableLiveData<String> message =
            new MutableLiveData<>();

    private final MutableLiveData<Boolean> loading =
            new MutableLiveData<>(false);

    public GroupViewModel(@NonNull Application application) {
        super(application);

        AppDatabase database = DatabaseProvider.getDatabase(application);

        groupDao = database.studentGroupDao();
        sessionGroupDao = database.sessionGroupDao();
        academicYearDao = database.academicYearDao();

        repository = new StudentGroupRepository(groupDao);
    }

    public LiveData<List<StudentGroup>> getGroups() {
        return groups;
    }

    public LiveData<Map<Integer, Integer>> getStudentCounts() {
        return studentCounts;
    }

    public LiveData<Map<Integer, Integer>> getSessionCounts() {
        return sessionCounts;
    }

    public LiveData<StudentGroup> getSelectedGroup() {
        return selectedGroup;
    }

    public LiveData<AcademicYear> getSelectedAcademicYear() {
        return selectedAcademicYear;
    }

    public LiveData<Integer> getSelectedStudentCount() {
        return selectedStudentCount;
    }

    public LiveData<Integer> getSelectedSessionCount() {
        return selectedSessionCount;
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

    public void loadGroups() {
        executor.execute(() -> {
            loading.postValue(true);

            try {
                rafraichirListe();
                error.postValue(null);

            } catch (Exception e) {
                error.postValue(getErrorMessage(e));

            } finally {
                loading.postValue(false);
            }
        });
    }

    public void loadGroupById(long id) {
        executor.execute(() -> {
            try {
                StudentGroup group = repository.getById(id);

                if (group == null) {
                    selectedGroup.postValue(null);
                    error.postValue("Groupe introuvable.");
                    return;
                }

                // Les valeurs annexes sont publiées avant le groupe.
                selectedAcademicYear.postValue(
                        academicYearDao.getById(group.getAcademicYearId())
                );
                selectedStudentCount.postValue(
                        groupDao.countStudents(group.getId())
                );
                selectedSessionCount.postValue(
                        sessionGroupDao.countByGroupId(group.getId())
                );

                selectedGroup.postValue(group);
                error.postValue(null);

            } catch (Exception e) {
                error.postValue(getErrorMessage(e));
            }
        });
    }

    public void addGroup(StudentGroup group) {
        executor.execute(() -> {
            loading.postValue(true);
            error.postValue(null);
            message.postValue(null);

            try {
                repository.insert(group);
                rafraichirListe();

                message.postValue("Groupe ajouté avec succès.");

            } catch (Exception e) {
                error.postValue(getErrorMessage(e));

            } finally {
                loading.postValue(false);
            }
        });
    }

    public void updateGroup(StudentGroup group) {
        executor.execute(() -> {
            loading.postValue(true);
            error.postValue(null);
            message.postValue(null);

            try {
                repository.update(group);
                rafraichirListe();

                message.postValue("Groupe modifié avec succès.");

            } catch (Exception e) {
                error.postValue(getErrorMessage(e));

            } finally {
                loading.postValue(false);
            }
        });
    }

    public void deleteGroup(StudentGroup group) {
        executor.execute(() -> {
            loading.postValue(true);
            error.postValue(null);
            message.postValue(null);

            try {
                repository.delete(group);
                rafraichirListe();

                message.postValue("Groupe supprimé avec succès.");

            } catch (Exception e) {
                error.postValue(getErrorMessage(e));

            } finally {
                loading.postValue(false);
            }
        });
    }

    private void rafraichirListe() {
        List<StudentGroup> resultat = repository.getAll();

        if (resultat == null) {
            resultat = new ArrayList<>();
        }

        Map<Integer, Integer> etudiants = new HashMap<>();
        Map<Integer, Integer> seances = new HashMap<>();

        for (StudentGroup group : resultat) {
            etudiants.put(
                    group.getId(),
                    groupDao.countStudents(group.getId())
            );
            seances.put(
                    group.getId(),
                    sessionGroupDao.countByGroupId(group.getId())
            );
        }

        // Les effectifs sont publiés avant la liste.
        studentCounts.postValue(etudiants);
        sessionCounts.postValue(seances);
        groups.postValue(resultat);
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