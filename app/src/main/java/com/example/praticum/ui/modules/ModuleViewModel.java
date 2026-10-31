package com.example.praticum.ui.modules;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.praticum.data.database.DatabaseProvider;
import com.example.praticum.data.database.AppDatabase;
import com.example.praticum.data.entity.Course;
import com.example.praticum.data.entity.Specialization;
import com.example.praticum.R;
import com.example.praticum.data.repository.ModuleRepository;

import java.util.List;
import java.util.UUID;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModuleViewModel extends AndroidViewModel {

    private final ModuleRepository repository;
    private final AppDatabase database;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<Course>> modules =
            new MutableLiveData<>();
    private final MutableLiveData<List<Specialization>> specializations =
            new MutableLiveData<>();
    private final MutableLiveData<String> error =
            new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess =
            new MutableLiveData<>();

    public ModuleViewModel(@NonNull Application application) {
        super(application);

        database = DatabaseProvider.getDatabase(application);
        repository = new ModuleRepository(database.moduleDao());

        loadAll();
        loadSpecializations();
    }

    public LiveData<List<Course>> getModules() {
        return modules;
    }

    public LiveData<List<Specialization>> getSpecializations() { return specializations; }

    public void loadSpecializations() {
        executor.execute(() -> {
            try {
                String[] configuredNames = getApplication().getResources()
                        .getStringArray(R.array.specializations);
                database.runInTransaction(() -> {
                    for (String name : configuredNames) {
                        if (database.specializationDao().getByName(name) == null) {
                            String code = "SPEC-" + UUID.randomUUID()
                                    .toString().substring(0, 8).toUpperCase(Locale.ROOT);
                            database.specializationDao().insert(
                                    new Specialization(code, name, null));
                        }
                    }
                });
                specializations.postValue(database.specializationDao().getAll());
            } catch (Exception e) {
                error.postValue(getErrorMessage(e));
            }
        });
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }

    public void loadAll() {
        executor.execute(() -> {
            try {
                List<Course> result = repository.getAll();
                modules.postValue(result);
            } catch (Exception e) {
                error.postValue(getErrorMessage(e));
            }
        });
    }

    public void loadByLevel(long levelId) {
        executor.execute(() -> {
            try {
                modules.postValue(repository.getByLevelId(levelId));
            } catch (Exception e) {
                error.postValue(getErrorMessage(e));
            }
        });
    }

    public void loadByLevelAndSemester(
            long levelId, int semester) {
        executor.execute(() -> {
            try {
                modules.postValue(
                        repository.getByLevelAndSemester(levelId, semester)
                );
            } catch (Exception e) {
                error.postValue(getErrorMessage(e));
            }
        });
    }

    public void add(Course module) {
        executeOperation(() -> repository.insert(module));
    }

    public void update(Course module) {
        executeOperation(() -> {
            repository.update(module);
            return null;
        });
    }

    public void delete(Course module) {
        executeOperation(() -> {
            repository.delete(module);
            return null;
        });
    }

    private void executeOperation(Operation operation) {
        executor.execute(() -> {
            try {
                error.postValue(null);
                operation.run();
                operationSuccess.postValue(true);
                modules.postValue(repository.getAll());
            } catch (Exception e) {
                operationSuccess.postValue(false);
                error.postValue(getErrorMessage(e));
            }
        });
    }

    private String getErrorMessage(Exception e) {
        String message = e.getMessage();
        return message != null
                ? message
                : "Une erreur est survenue lors de l'opération.";
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    private interface Operation {
        Object run() throws Exception;
    }
}
