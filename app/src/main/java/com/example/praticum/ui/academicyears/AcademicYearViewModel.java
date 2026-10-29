package com.example.praticum.ui.academicyears;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.praticum.data.database.AppDatabase;
import com.example.praticum.data.database.DatabaseProvider;
import com.example.praticum.data.entity.AcademicYear;
import com.example.praticum.data.repository.AcademicYearRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AcademicYearViewModel extends AndroidViewModel {

    private final AcademicYearRepository repository;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<AcademicYear>> academicYears =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<AcademicYear> activeAcademicYear =
            new MutableLiveData<>();

    private final MutableLiveData<String> operationMessage =
            new MutableLiveData<>();

    private final MutableLiveData<Boolean> operationSuccess =
            new MutableLiveData<>();

    public AcademicYearViewModel(@NonNull Application application) {
        super(application);

        AppDatabase database = DatabaseProvider.getDatabase(application);

        repository = new AcademicYearRepository(
                database,
                database.academicYearDao()
        );

        loadAcademicYears();
    }

    public LiveData<List<AcademicYear>> getAcademicYears() {
        return academicYears;
    }

    public LiveData<AcademicYear> getActiveAcademicYear() {
        return activeAcademicYear;
    }

    public LiveData<String> getOperationMessage() {
        return operationMessage;
    }

    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }

    public void loadAcademicYears() {
        executor.execute(() -> {
            try {
                refreshData();
            } catch (Exception e) {
                publishResult(false, errorMessage(e));
            }
        });
    }

    public void addAcademicYear(AcademicYear year) {
        executeOperation(() -> repository.insert(year),
                "Année universitaire ajoutée avec succès.");
    }

    public void updateAcademicYear(AcademicYear year) {
        executeOperation(() -> {
            repository.update(year);
            return null;
        }, "Année universitaire modifiée avec succès.");
    }

    public void activateAcademicYear(long id) {
        executeOperation(() -> {
            repository.setActive(id);
            return null;
        }, "Année universitaire activée avec succès.");
    }

    public void deleteAcademicYear(long id) {
        executeOperation(() -> {
            repository.delete(id);
            return null;
        }, "Année universitaire supprimée avec succès.");
    }

    private void executeOperation(
            Operation operation,
            String successMessage) {

        executor.execute(() -> {
            try {
                operation.execute();
                refreshData();
                publishResult(true, successMessage);
            } catch (Exception e) {
                publishResult(false, errorMessage(e));
            }
        });
    }

    private void refreshData() {
        List<AcademicYear> years = repository.getAll();
        AcademicYear active = repository.getActive();

        academicYears.postValue(
                years != null ? years : new ArrayList<>());

        activeAcademicYear.postValue(active);
    }

    private void publishResult(boolean success, String message) {
        operationSuccess.postValue(success);
        operationMessage.postValue(message);
    }

    private String errorMessage(Exception e) {
        String message = e.getMessage();

        if (message == null || message.trim().isEmpty()) {
            return "Une erreur est survenue. Veuillez réessayer.";
        }

        return message;
    }

    private interface Operation {
        Object execute();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}