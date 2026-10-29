
package com.example.praticum.ui.levels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.praticum.data.database.DatabaseProvider;
import com.example.praticum.data.entity.Level;
import com.example.praticum.data.repository.LevelRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LevelViewModel extends AndroidViewModel {

    private final LevelRepository repository;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<Level>> levels =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<Level> selectedLevel =
            new MutableLiveData<>();

    private final MutableLiveData<String> error =
            new MutableLiveData<>();

    private final MutableLiveData<String> message =
            new MutableLiveData<>();

    private final MutableLiveData<Boolean> loading =
            new MutableLiveData<>(false);

    public LevelViewModel(@NonNull Application application) {
        super(application);

        repository = new LevelRepository(
                DatabaseProvider.getDatabase(application).levelDao()
        );

        loadLevels();
    }

    public LiveData<List<Level>> getLevels() {
        return levels;
    }

    public LiveData<Level> getSelectedLevel() {
        return selectedLevel;
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

    public void loadLevels() {
        executor.execute(() -> {
            loading.postValue(true);

            try {
                List<Level> result = repository.getAll();

                levels.postValue(
                        result != null ? result : new ArrayList<>()
                );

                error.postValue(null);

            } catch (Exception e) {
                error.postValue(getErrorMessage(e));

            } finally {
                loading.postValue(false);
            }
        });
    }

    public void loadLevelById(long id) {
        executor.execute(() -> {
            try {
                Level level = repository.getById(id);
                selectedLevel.postValue(level);

                if (level == null) {
                    error.postValue("Niveau introuvable.");
                } else {
                    error.postValue(null);
                }

            } catch (Exception e) {
                error.postValue(getErrorMessage(e));
            }
        });
    }

    public void addLevel(Level level) {
        executor.execute(() -> {
            loading.postValue(true);
            error.postValue(null);
            message.postValue(null);

            try {
                repository.insert(level);
                rafraichirListe();

                message.postValue("Niveau ajouté avec succès.");

            } catch (Exception e) {
                error.postValue(getErrorMessage(e));

            } finally {
                loading.postValue(false);
            }
        });
    }

    public void updateLevel(Level level) {
        executor.execute(() -> {
            loading.postValue(true);
            error.postValue(null);
            message.postValue(null);

            try {
                repository.update(level);
                rafraichirListe();

                message.postValue("Niveau modifié avec succès.");

            } catch (Exception e) {
                error.postValue(getErrorMessage(e));

            } finally {
                loading.postValue(false);
            }
        });
    }

    public void deleteLevel(Level level) {
        executor.execute(() -> {
            loading.postValue(true);
            error.postValue(null);
            message.postValue(null);

            try {
                repository.delete(level);
                rafraichirListe();

                message.postValue("Niveau supprimé avec succès.");

            } catch (Exception e) {
                error.postValue(getErrorMessage(e));

            } finally {
                loading.postValue(false);
            }
        });
    }

    private void rafraichirListe() {
        List<Level> result = repository.getAll();

        levels.postValue(
                result != null ? result : new ArrayList<>()
        );
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
