package com.example.praticum.ui.sessions;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.praticum.data.dao.ModuleDao;
import com.example.praticum.data.dao.SessionCriterionDao;
import com.example.praticum.data.database.AppDatabase;
import com.example.praticum.data.database.DatabaseProvider;
import com.example.praticum.data.entity.Course;
import com.example.praticum.data.entity.Session;
import com.example.praticum.data.entity.SessionCriterion;
import com.example.praticum.data.repository.SessionRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SessionViewModel extends AndroidViewModel {

    private final AppDatabase database;
    private final SessionRepository repository;
    private final SessionCriterionDao criterionDao;
    private final ModuleDao moduleDao;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<Session>> activeSessions =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<Course>> modules =
            new MutableLiveData<>(new ArrayList<>());

    private final MediatorLiveData<List<Session>> filteredSessions =
            new MediatorLiveData<>();

    private final MutableLiveData<SessionDetails> sessionDetails =
            new MutableLiveData<>();

    private final MutableLiveData<OperationResult> operationResult =
            new MutableLiveData<>();

    private final MutableLiveData<Map<Integer, SessionCardSummary>> sessionCardSummaries =
            new MutableLiveData<>(new HashMap<>());
    private final Map<Integer, SessionCardSummary> sessionCardSummaryCache = new HashMap<>();
    private final Set<Integer> sessionCardSummaryLoads = new HashSet<>();

    private String searchQuery = "";
    private String statusFilter = "Tous";

    public SessionViewModel(@NonNull Application application) {
        super(application);

        database = DatabaseProvider.getDatabase(application);

        repository = new SessionRepository(
                database.sessionDao()
        );

        criterionDao = database.sessionCriterionDao();
        moduleDao = database.moduleDao();

        filteredSessions.setValue(new ArrayList<>());

        filteredSessions.addSource(
                activeSessions,
                sessions -> appliquerFiltres()
        );

        filteredSessions.addSource(
                modules,
                moduleList -> appliquerFiltres()
        );
    }

    public LiveData<List<Session>> getSessions() {
        return filteredSessions;
    }

    public LiveData<List<Course>> getModules() {
        return modules;
    }

    public LiveData<SessionDetails> getSessionDetails() {
        return sessionDetails;
    }

    public LiveData<OperationResult> getOperationResult() {
        return operationResult;
    }

    public LiveData<Map<Integer, SessionCardSummary>> getSessionCardSummaries() {
        return sessionCardSummaries;
    }

    public void loadSessionCardSummaries(List<Session> sessions) {
        if (sessions == null || sessions.isEmpty()) return;
        List<Session> missing = new ArrayList<>();
        synchronized (sessionCardSummaryCache) {
            for (Session session : sessions) {
                if (session != null && !sessionCardSummaryCache.containsKey(session.getId())
                        && sessionCardSummaryLoads.add(session.getId())) {
                    missing.add(session);
                }
            }
        }
        if (missing.isEmpty()) return;
        executor.execute(() -> {
            Map<Integer, SessionCardSummary> loaded = new HashMap<>();
            for (Session session : missing) {
                try {
                    Course module = moduleDao.getById(session.getModuleId());
                    List<SessionCriterion> criteria = criterionDao.getBySessionId(session.getId());
                    double maxScore = 0;
                    if (criteria != null) {
                        for (SessionCriterion criterion : criteria) {
                            maxScore += criterion.getMaxScore();
                        }
                    }
                    String moduleName = module == null ? "" : valeurOuVide(module.getName());
                    String moduleCode = module == null ? "" : valeurOuVide(module.getCode());
                    String moduleLabel = moduleCode.isEmpty() ? moduleName
                            : moduleName.isEmpty() ? moduleCode : moduleCode + " · " + moduleName;
                    loaded.put(session.getId(), new SessionCardSummary(moduleLabel,
                            criteria == null ? 0 : criteria.size(), maxScore));
                } catch (Exception ignored) {
                    loaded.put(session.getId(), new SessionCardSummary("", 0, 0));
                }
            }
            Map<Integer, SessionCardSummary> snapshot;
            synchronized (sessionCardSummaryCache) {
                sessionCardSummaryCache.putAll(loaded);
                for (Session session : missing) sessionCardSummaryLoads.remove(session.getId());
                snapshot = new HashMap<>(sessionCardSummaryCache);
            }
            sessionCardSummaries.postValue(snapshot);
        });
    }

    public void loadSessions() {
        executor.execute(() -> {
            try {
                synchronized (sessionCardSummaryCache) {
                    sessionCardSummaryCache.clear();
                    sessionCardSummaryLoads.clear();
                }
                sessionCardSummaries.postValue(new HashMap<>());
                activeSessions.postValue(
                        repository.getActiveSessions()
                );

            } catch (Exception e) {
                publierErreur(
                        e,
                        "Impossible de charger les séances."
                );
            }
        });
    }

    public void loadModules() {
        executor.execute(() -> {
            try {
                modules.postValue(moduleDao.getAll());

            } catch (Exception e) {
                publierErreur(
                        e,
                        "Impossible de charger les modules."
                );
            }
        });
    }

    public void setSearchQuery(String query) {
        searchQuery = query == null ? "" : query.trim();
        appliquerFiltres();
    }

    public void setStatusFilter(String status) {
        statusFilter = status == null || status.trim().isEmpty()
                ? "Tous"
                : status.trim();

        appliquerFiltres();
    }

    private void appliquerFiltres() {
        List<Session> source = activeSessions.getValue();

        if (source == null) {
            source = new ArrayList<>();
        }

        List<Course> listeModules = modules.getValue();
        List<Session> resultat = new ArrayList<>();

        String requete = normaliser(searchQuery);

        for (Session session : source) {
            if (session == null) {
                continue;
            }

            String nomModule = trouverNomModule(
                    session.getModuleId(),
                    listeModules
            );

            String contenu = normaliser(
                    session.getTitle() + " "
                            + session.getActivityType() + " "
                            + nomModule + " "
                            + valeurOuVide(session.getDescription())
            );

            boolean correspondRecherche =
                    requete.isEmpty()
                            || contenu.contains(requete);

            boolean correspondStatut =
                    "Tous".equalsIgnoreCase(statusFilter)
                            || statusFilter.equalsIgnoreCase(
                            session.getStatus()
                    );

            if (correspondRecherche && correspondStatut) {
                resultat.add(session);
            }
        }

        filteredSessions.setValue(resultat);
    }

    private String trouverNomModule(
            int moduleId,
            List<Course> listeModules) {

        if (listeModules == null) {
            return "";
        }

        for (Course module : listeModules) {
            if (module != null && module.getId() == moduleId) {
                return valeurOuVide(module.getName());
            }
        }

        return "";
    }

    public void loadSession(int sessionId) {
        executor.execute(() -> {
            try {
                Session session = repository.getById(sessionId);

                if (session == null) {
                    sessionDetails.postValue(null);
                    return;
                }

                Course module = moduleDao.getById(
                        session.getModuleId()
                );

                String moduleName = module == null ? "" : valeurOuVide(module.getName());
                String moduleCode = module == null ? "" : valeurOuVide(module.getCode());
                com.example.praticum.data.entity.Specialization specialization = module == null ? null
                        : database.specializationDao().getById(module.getSpecializationId());
                String specializationName = specialization == null ? "" : valeurOuVide(specialization.getName());
                com.example.praticum.data.entity.Level level = module == null ? null
                        : database.levelDao().getById(module.getLevelId());
                String levelName = level == null ? "" : valeurOuVide(level.getName());
                int semester = module == null ? 0 : module.getSemester();
                double coefficient = module == null ? 0 : module.getCoefficient();
                List<SessionCriterion> criteria = criterionDao.getBySessionId(sessionId);

                sessionDetails.postValue(
                        new SessionDetails(
                                session,
                                criteria,
                                moduleName,
                                specializationName,
                                moduleCode,
                                levelName,
                                semester,
                                coefficient
                        )
                );

            } catch (Exception e) {
                sessionDetails.postValue(null);

                publierErreur(
                        e,
                        "Impossible de charger les détails de la séance."
                );
            }
        });
    }

    /**
     * Enregistre la séance et ses critères dans une transaction Room.
     *
     * Les objets venant du formulaire sont copiés avant leur traitement
     * sur le thread d'accès à la base de données.
     */
    public void saveSession(
            @NonNull Session session,
            List<SessionCriterion> formCriteria) {

        final boolean creation = session.getId() <= 0;

        final List<SessionCriterion> criteriaToSave =
                copierCriteres(formCriteria);

        executor.execute(() -> {
            try {
                database.runInTransaction(() -> {
                    if (session.getId() <= 0) {
                        long id = repository.insert(session);
                        session.setId((int) id);

                    } else {
                        repository.update(session);
                    }

                    synchroniserCriteres(
                            session.getId(),
                            criteriaToSave
                    );
                });

                rechargerDonneesApresEcriture();

                operationResult.postValue(
                        new OperationResult(
                                true,
                                creation
                                        ? "Séance ajoutée"
                                        : "Séance modifiée"
                        )
                );

            } catch (Exception e) {
                publierErreur(
                        e,
                        "Impossible d'enregistrer la séance."
                );
            }
        });
    }

    /**
     * Synchronise les critères du formulaire avec les critères persistés.
     *
     * - id existant : mise à jour de la ligne correspondante ;
     * - id nul ou égal à 0 : insertion d'un nouveau critère ;
     * - critère supprimé du formulaire : suppression protégée par la DAO.
     */
    private void synchroniserCriteres(
            int sessionId,
            List<SessionCriterion> criteresFormulaire) {

        List<SessionCriterion> existants =
                criterionDao.getBySessionId(sessionId);

        Map<Integer, SessionCriterion> existantsParId =
                new HashMap<>();

        for (SessionCriterion criterion : existants) {
            existantsParId.put(
                    criterion.getId(),
                    criterion
            );
        }

        Set<Integer> idsConserves = new HashSet<>();

        for (int position = 0;
             position < criteresFormulaire.size();
             position++) {

            SessionCriterion formulaire =
                    criteresFormulaire.get(position);

            if (formulaire == null) {
                throw new IllegalArgumentException(
                        "Un critère est invalide."
                );
            }

            String title = valeurOuVide(
                    formulaire.getTitle()
            ).trim();

            double maxScore = formulaire.getMaxScore();

            if (title.isEmpty()) {
                throw new IllegalArgumentException(
                        "Le titre de chaque critère est obligatoire."
                );
            }

            if (!Double.isFinite(maxScore) || maxScore <= 0) {
                throw new IllegalArgumentException(
                        "Le score maximal de chaque critère "
                                + "doit être supérieur à zéro."
                );
            }

            if (formulaire.getId() > 0) {
                int criterionId = formulaire.getId();

                SessionCriterion existant =
                        existantsParId.get(criterionId);

                if (existant == null) {
                    throw new IllegalArgumentException(
                            "Le critère "
                                    + criterionId
                                    + " n'appartient pas à cette séance."
                    );
                }

                if (!idsConserves.add(criterionId)) {
                    throw new IllegalArgumentException(
                            "Un même critère ne peut pas être "
                                    + "présent plusieurs fois."
                    );
                }

                existant.setTitle(title);

                existant.setDescription(
                        formulaire.getDescription()
                );

                existant.setMaxScore(maxScore);
                existant.setPosition(position);

                int updated = criterionDao.update(existant);

                if (updated == 0) {
                    throw new IllegalArgumentException(
                            "Impossible de modifier le critère "
                                    + title + "."
                    );
                }

            } else {
                SessionCriterion nouveau =
                        new SessionCriterion(
                                sessionId,
                                title,
                                maxScore,
                                position
                        );

                nouveau.setDescription(
                        formulaire.getDescription()
                );

                criterionDao.insert(nouveau);
            }
        }

        /*
         * Toute suppression de critère s'effectue uniquement si aucune
         * note ne le référence. Une erreur annule la transaction complète.
         */
        for (SessionCriterion existant : existants) {
            if (idsConserves.contains(existant.getId())) {
                continue;
            }

            int deleted = criterionDao.deleteIfNotScored(
                    existant.getId()
            );

            if (deleted == 0) {
                throw new IllegalArgumentException(
                        "Le critère « "
                                + existant.getTitle()
                                + " » ne peut pas être supprimé, "
                                + "car il est utilisé dans une évaluation."
                );
            }
        }
    }

    private List<SessionCriterion> copierCriteres(
            List<SessionCriterion> criteres) {

        List<SessionCriterion> copies = new ArrayList<>();

        if (criteres == null) {
            return copies;
        }

        for (SessionCriterion source : criteres) {
            if (source == null) {
                continue;
            }

            SessionCriterion copie = new SessionCriterion(
                    source.getSessionId(),
                    valeurOuVide(source.getTitle()),
                    source.getMaxScore(),
                    source.getPosition()
            );

            copie.setId(source.getId());
            copie.setDescription(source.getDescription());

            copies.add(copie);
        }

        return copies;
    }

    public void deleteSession(int sessionId) {
        executor.execute(() -> {
            try {
                Session session = repository.getById(sessionId);

                if (session == null) {
                    throw new IllegalArgumentException(
                            "Séance introuvable."
                    );
                }

                repository.delete(session);

                rechargerDonneesApresEcriture();

                operationResult.postValue(
                        new OperationResult(
                                true,
                                "Séance supprimée"
                        )
                );

            } catch (Exception e) {
                publierErreur(
                        e,
                        "Impossible de supprimer la séance."
                );
            }
        });
    }

    public void archiveSession(int sessionId) {
        executor.execute(() -> {
            try {
                repository.setArchived(sessionId, true);

                rechargerDonneesApresEcriture();

                operationResult.postValue(
                        new OperationResult(
                                true,
                                "Séance archivée"
                        )
                );

            } catch (Exception e) {
                publierErreur(
                        e,
                        "Impossible d'archiver la séance."
                );
            }
        });
    }

    private void rechargerDonneesApresEcriture() {
        activeSessions.postValue(
                repository.getActiveSessions()
        );

        modules.postValue(
                moduleDao.getAll()
        );
    }

    public void clearOperationResult() {
        operationResult.setValue(null);
    }

    private void publierErreur(
            Exception exception,
            String messageParDefaut) {

        String message = exception.getMessage();

        operationResult.postValue(
                new OperationResult(
                        false,
                        message == null || message.trim().isEmpty()
                                ? messageParDefaut
                                : message
                )
        );
    }

    private String normaliser(String texte) {
        return texte == null
                ? ""
                : texte.toLowerCase(Locale.ROOT).trim();
    }

    private String valeurOuVide(String valeur) {
        return valeur == null ? "" : valeur;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    public static final class SessionCardSummary {
        private final String moduleLabel;
        private final int criteriaCount;
        private final double maxScore;

        public SessionCardSummary(String moduleLabel, int criteriaCount, double maxScore) {
            this.moduleLabel = moduleLabel == null ? "" : moduleLabel;
            this.criteriaCount = criteriaCount;
            this.maxScore = maxScore;
        }

        public String getModuleLabel() { return moduleLabel; }
        public int getCriteriaCount() { return criteriaCount; }
        public double getMaxScore() { return maxScore; }
    }

    public static final class SessionDetails {
        private final Session session;
        private final List<SessionCriterion> criteria;
        private final String moduleName, specializationName, moduleCode, levelName;
        private final int semester;
        private final double coefficient;

        public SessionDetails(Session session, List<SessionCriterion> criteria,
                String moduleName, String specializationName, String moduleCode,
                String levelName, int semester, double coefficient) {
            this.session = session;
            this.criteria = criteria == null ? new ArrayList<>() : new ArrayList<>(criteria);
            this.moduleName = moduleName == null ? "" : moduleName;
            this.specializationName = specializationName == null ? "" : specializationName;
            this.moduleCode = moduleCode == null ? "" : moduleCode;
            this.levelName = levelName == null ? "" : levelName;
            this.semester = semester;
            this.coefficient = coefficient;
        }
        public Session getSession() { return session; }
        public List<SessionCriterion> getCriteria() { return new ArrayList<>(criteria); }
        public String getModuleName() { return moduleName; }
        public String getSpecializationName() { return specializationName; }
        public String getModuleCode() { return moduleCode; }
        public String getLevelName() { return levelName; }
        public int getSemester() { return semester; }
        public double getCoefficient() { return coefficient; }
    }
    public static final class OperationResult {

        private final boolean success;
        private final String message;

        public OperationResult(
                boolean success,
                String message) {

            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
