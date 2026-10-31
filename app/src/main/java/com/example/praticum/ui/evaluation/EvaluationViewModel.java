package com.example.praticum.ui.evaluation;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.praticum.data.database.AppDatabase;
import com.example.praticum.data.database.DatabaseProvider;
import com.example.praticum.data.entity.Evaluation;
import com.example.praticum.data.entity.Enrollment;
import com.example.praticum.data.entity.EvaluationScore;
import com.example.praticum.data.entity.EvaluationTeam;
import com.example.praticum.data.entity.EvaluationTeamMember;
import com.example.praticum.data.entity.Session;
import com.example.praticum.data.entity.SessionCriterion;
import com.example.praticum.data.entity.Student;
import com.example.praticum.data.entity.StudentGroup;
import com.example.praticum.data.entity.Course;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EvaluationViewModel extends AndroidViewModel {

    private final AppDatabase database;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final MutableLiveData<SheetState> sheetState =
            new MutableLiveData<>();

    private final MutableLiveData<OperationResult> operationResult =
            new MutableLiveData<>();

    public EvaluationViewModel(@NonNull Application application) {
        super(application);
        database = DatabaseProvider.getDatabase(application);
    }

    public LiveData<SheetState> getSheetState() {
        return sheetState;
    }

    public LiveData<OperationResult> getOperationResult() {
        return operationResult;
    }

    public void loadSheet(int sessionId) {
        executor.execute(() -> {
            try {
                sheetState.postValue(chargerEtat(sessionId, -1, true));
            } catch (Exception e) {
                sheetState.postValue(null);

                operationResult.postValue(
                        new OperationResult(
                                false,
                                messageErreur(
                                        e,
                                        "Impossible de charger l'évaluation."
                                ),
                                true,
                                false
                        )
                );
            }
        });
    }

    public void loadSheet(int sessionId, long evaluationId) {
        executor.execute(() -> {
            try {
                sheetState.postValue(chargerEtat(sessionId, evaluationId, false));
            } catch (Exception e) {
                sheetState.postValue(null);
                operationResult.postValue(new OperationResult(false,
                        messageErreur(e, "Impossible de charger l'évaluation."), true, false));
            }
        });
    }

    /** Prépare une feuille vide; l'évaluation sera créée au premier ajout d'étudiant. */
    public void startNewEvaluation(int sessionId) {
        executor.execute(() -> {
            try {
                sheetState.postValue(chargerEtat(sessionId, -1, false));
            } catch (Exception e) {
                sheetState.postValue(null);
                operationResult.postValue(new OperationResult(false,
                        messageErreur(e, "Impossible de démarrer l'évaluation."), true, false));
            }
        });
    }

    private SheetState chargerEtat(int sessionId, long requestedEvaluationId, boolean includeFinalizedHistory) {
        Session session =
                database.sessionDao().getById(sessionId);

        if (session == null) {
            throw new IllegalArgumentException(
                    "La séance demandée n'existe plus."
            );
        }

        List<SessionCriterion> criteria =
                database.sessionCriterionDao()
                        .getBySessionId(sessionId);

        List<EvaluationTeamState> teams = new ArrayList<>();
        List<Evaluation> evaluations = new ArrayList<>();
        Evaluation activeEvaluation = requestedEvaluationId > 0
                ? database.evaluationDao().getById(requestedEvaluationId)
                : null;
        if (activeEvaluation != null && activeEvaluation.getSessionId() == sessionId) {
            evaluations.add(activeEvaluation);
        } else if (includeFinalizedHistory) {
            evaluations.addAll(database.evaluationDao().getFinalizedBySessionId(sessionId));
        }
        for (Evaluation evaluation : evaluations) {
            List<EvaluationTeam> roomTeams = database.evaluationTeamDao()
                    .getByEvaluationId(evaluation.getId());
            for (EvaluationTeam team : roomTeams) {
                teams.add(chargerEquipe(team, criteria));
            }
        }

        Course module = database.moduleDao().getById(session.getModuleId());
        int totalStudents = module == null ? 0
                : database.enrollmentDao().countStudentsInActiveYearByLevelAndSpecialization(
                        module.getLevelId(), module.getSpecializationId());
        List<String> evaluatedStudentIds = module == null ? new ArrayList<>()
                : database.evaluationTeamMemberDao()
                        .getFinalizedStudentIdsBySessionLevelAndSpecializationInActiveYear(
                                sessionId, module.getLevelId(), module.getSpecializationId());

        return new SheetState(
                session,
                criteria,
                teams,
                evaluations,
                totalStudents,
                module == null ? -1 : module.getLevelId(),
                module == null ? -1 : module.getSpecializationId(),
                requestedEvaluationId > 0 && activeEvaluation != null
                        ? activeEvaluation.getId() : -1,
                evaluatedStudentIds
        );
    }

    private EvaluationTeamState chargerEquipe(
            EvaluationTeam team,
            List<SessionCriterion> sessionCriteria) {

        List<StudentWithGroup> students = new ArrayList<>();

        List<EvaluationTeamMember> members =
                database.evaluationTeamMemberDao()
                        .getByTeamId(team.getId());

        for (EvaluationTeamMember member : members) {
            Student student =
                    database.studentDao()
                            .getById(member.getStudentId());

            if (student == null) {
                continue;
            }

            List<Enrollment> studentEnrollments = database.enrollmentDao().getByStudent(student.getStudentId());
            Enrollment latestEnrollment = studentEnrollments.isEmpty() ? null : studentEnrollments.get(0);
            StudentGroup group = latestEnrollment == null ? null
                    : database.studentGroupDao().getById(latestEnrollment.getGroupId());
            students.add(
                    new StudentWithGroup(student, group == null ? "" : group.getName(),
                            latestEnrollment == null ? 0 : latestEnrollment.getGroupOrder())
            );
        }

        Map<Long, EvaluationScore> existingScores = new HashMap<>();

        List<EvaluationScore> loadedScores =
                database.evaluationScoreDao()
                        .getByTeamId(team.getId());

        for (EvaluationScore score : loadedScores) {
            existingScores.put(
                    score.getCriterionId(),
                    score
            );
        }

        List<SessionCriterion> validCriteria = new ArrayList<>();
        List<EvaluationScore> scores = new ArrayList<>();

        for (SessionCriterion criterion : sessionCriteria) {
            double max = criterion.getMaxScore();

            if (!Double.isFinite(max) || max <= 0) {
                continue;
            }

            validCriteria.add(criterion);

            EvaluationScore score =
                    existingScores.get((long) criterion.getId());

            if (score == null) {
                score = new EvaluationScore(
                        team.getId(),
                        criterion.getId(),
                        0
                );
            } else {
                score.setScore(
                        Math.max(0, Math.min(score.getScore(), max))
                );
            }

            scores.add(score);
        }

        Evaluation ownerEvaluation = database.evaluationDao()
                .getById(team.getEvaluationId());
        return new EvaluationTeamState(
                team,
                students,
                validCriteria,
                scores,
                ownerEvaluation != null && ownerEvaluation.isFinalized()
        );
    }

    public void addStudent(
            int sessionId,
            long evaluationId,
            long selectedTeamId,
            String selectedStudentId) {

        executor.execute(() -> {
            try {
                final long[] activeEvaluationId = {evaluationId};
                database.runInTransaction(() -> {
                    Session session =
                            database.sessionDao().getById(sessionId);

                    if (session == null) {
                        throw new IllegalArgumentException(
                                "La séance n'existe plus."
                        );
                    }

                    List<SessionCriterion> criteria =
                            database.sessionCriterionDao()
                                    .getBySessionId(sessionId);

                    boolean hasValidCriterion = false;

                    for (SessionCriterion criterion : criteria) {
                        if (Double.isFinite(criterion.getMaxScore())
                                && criterion.getMaxScore() > 0) {
                            hasValidCriterion = true;
                            break;
                        }
                    }

                    if (!hasValidCriterion) {
                        throw new IllegalArgumentException(
                                "Configurez d'abord les critères "
                                        + "de cette séance."
                        );
                    }

                    if (selectedStudentId == null
                            || selectedStudentId.trim().isEmpty()) {
                        throw new IllegalArgumentException(
                                "Choisissez un étudiant à ajouter à l’équipe."
                        );
                    }

                    Student selectedStudent = database.studentDao()
                            .getById(selectedStudentId);
                    if (selectedStudent == null) {
                        throw new IllegalArgumentException(
                                "L’étudiant sélectionné n’existe plus."
                        );
                    }

                    Enrollment activeEnrollment = database.enrollmentDao()
                            .getActiveByStudent(selectedStudentId);
                    if (activeEnrollment == null) {
                        throw new IllegalArgumentException(
                                "Cet étudiant n’a pas d’inscription dans l’année universitaire active."
                        );
                    }
                    Course module = database.moduleDao().getById(session.getModuleId());
                    if (module == null || activeEnrollment.getLevelId() != module.getLevelId()
                            || activeEnrollment.getSpecializationId() != module.getSpecializationId()) {
                        throw new IllegalArgumentException("Cet étudiant n'est pas inscrit dans le niveau et la spécialité du module de cette séance.");
                    }
                    if (database.evaluationTeamMemberDao()
                            .countStudentFinalizedInSessionExceptEvaluation(
                                    sessionId, evaluationId, selectedStudentId) > 0) {
                        throw new IllegalArgumentException("Cet étudiant a déjà été évalué dans cette séance.");
                    }
                    StudentGroup enrolledGroup = database.studentGroupDao()
                            .getById(activeEnrollment.getGroupId());
                    if (enrolledGroup == null) {
                        throw new IllegalArgumentException(
                                "Le groupe de l’inscription de cet étudiant est introuvable."
                        );
                    }

                    Evaluation evaluation = evaluationId > 0
                            ? database.evaluationDao().getById(evaluationId) : null;
                    if (evaluationId > 0 && (evaluation == null
                            || evaluation.getSessionId() != sessionId)) {
                        throw new IllegalArgumentException("Cette évaluation n'existe plus.");
                    }
                    if (evaluation == null) {
                        evaluation = new Evaluation(sessionId);
                        evaluation.setScoringMode(Evaluation.SCORING_MODE_COMMON);
                        activeEvaluationId[0] = database.evaluationDao().insert(evaluation);
                        evaluation.setId((int) activeEvaluationId[0]);
                    }

                    EvaluationTeam team;

                    if (selectedTeamId > 0) {
                        team = database.evaluationTeamDao()
                                .getById(selectedTeamId);

                        if (team == null
                                || team.getEvaluationId()
                                != activeEvaluationId[0]) {
                            throw new IllegalArgumentException(
                                    "L'équipe sélectionnée n'existe plus."
                            );
                        }
                    } else {
                        Integer maxNumber =
                                database.evaluationTeamDao()
                                        .getMaxTeamNumber(
                                                activeEvaluationId[0]
                                        );

                        int nextNumber = maxNumber == null
                                ? 1
                                : maxNumber + 1;

                        team = new EvaluationTeam(
                                activeEvaluationId[0],
                                nextNumber,
                                "Équipe " + nextNumber
                        );

                        long teamId = database.evaluationTeamDao()
                                .insert(team);

                        team.setId(teamId);
                    }

                    String studentId =
                            selectedStudent.getStudentId();

                    int alreadyAssigned =
                            database.evaluationTeamMemberDao()
                                    .countStudentInEvaluation(
                                            activeEvaluationId[0],
                                            studentId
                                    );

                    if (alreadyAssigned > 0) {
                        throw new IllegalArgumentException(
                                "Cet étudiant appartient déjà "
                                        + "à une équipe de cette évaluation."
                        );
                    }

                    database.evaluationTeamMemberDao().insert(
                            new EvaluationTeamMember(
                                    team.getId(),
                                    studentId,
                                    activeEvaluationId[0]
                            )
                    );
                });

                sheetState.postValue(chargerEtat(sessionId, activeEvaluationId[0], false));

                operationResult.postValue(
                        new OperationResult(
                                true,
                                "",
                                false,
                                true
                        )
                );

            } catch (Exception e) {
                publierErreur(
                        e,
                        "Impossible d'ajouter cet étudiant."
                );
            }
        });
    }

    public void removeStudent(
            int sessionId,
            long evaluationId,
            long teamId,
            String studentId) {

        executor.execute(() -> {
            try {
                database.runInTransaction(() -> {
                    EvaluationTeam team =
                            database.evaluationTeamDao()
                                    .getById(teamId);

                    if (team == null) {
                        throw new IllegalArgumentException(
                                "L'équipe n'existe plus."
                        );
                    }

                    Evaluation evaluation =
                            database.evaluationDao()
                                    .getById(team.getEvaluationId());

                    if (evaluation == null
                            || evaluation.getSessionId() != sessionId) {
                        throw new IllegalArgumentException(
                                "L'équipe ne correspond pas à cette séance."
                        );
                    }

                    int removed =
                            database.evaluationTeamMemberDao()
                                    .removeStudentFromTeam(
                                            teamId,
                                            studentId
                                    );

                    if (removed == 0) {
                        throw new IllegalArgumentException(
                                "L'étudiant n'appartient plus à cette équipe."
                        );
                    }
                });

                sheetState.postValue(chargerEtat(sessionId, evaluationId, false));

            } catch (Exception e) {
                publierErreur(
                        e,
                        "Impossible de retirer cet étudiant."
                );
            }
        });
    }

    public void saveScores(
            long teamId,
            List<EvaluationScore> scores) {

        List<ScoreInput> copies = copierScores(scores);

        executor.execute(() -> {
            try {
                database.runInTransaction(
                        () -> enregistrerNotes(teamId, copies)
                );
            } catch (Exception e) {
                publierErreur(
                        e,
                        "Impossible d'enregistrer les notes."
                );
            }
        });
    }

    public void saveEvaluation(
            int sessionId,
            long evaluationId,
            long teamId,
            List<EvaluationScore> scores) {

        List<ScoreInput> copies = copierScores(scores);

        executor.execute(() -> {
            try {
                final boolean[] editingFinalizedEvaluation = {false};
                database.runInTransaction(() -> {
                    Session session =
                            database.sessionDao().getById(sessionId);

                    if (session == null) {
                        throw new IllegalArgumentException(
                                "La séance n'existe plus."
                        );
                    }

                    Evaluation evaluation = evaluationId > 0
                            ? database.evaluationDao().getById(evaluationId) : null;
                    if (evaluationId <= 0 || evaluation == null || evaluation.getSessionId() != sessionId
                            ) {
                        throw new IllegalArgumentException("Cette évaluation n'existe plus.");
                    }
                    editingFinalizedEvaluation[0] = evaluation.isFinalized();

                    database.evaluationDao()
                            .setCommonScoringMode(evaluation.getId());

                    if (teamId > 0) {
                        enregistrerNotes(teamId, copies);
                    }
                    if (database.evaluationTeamDao().countByEvaluationId(evaluationId) == 0) {
                        throw new IllegalArgumentException("Ajoutez au moins un étudiant avant d'enregistrer.");
                    }
                    database.evaluationDao().finalizeEvaluation(evaluationId, System.currentTimeMillis());
                });

                sheetState.postValue(editingFinalizedEvaluation[0]
                        ? chargerEtat(sessionId, evaluationId, false)
                        : chargerEtat(sessionId, -1, false));

                operationResult.postValue(
                        new OperationResult(
                                true,
                                editingFinalizedEvaluation[0]
                                        ? "Évaluation modifiée."
                                        : "Résultats enregistrés. Le formulaire est prêt pour une nouvelle évaluation.",
                                editingFinalizedEvaluation[0],
                                !editingFinalizedEvaluation[0]
                        )
                );

            } catch (Exception e) {
                publierErreur(
                        e,
                        "Impossible d'enregistrer l'évaluation."
                );
            }
        });
    }

    public void deleteEvaluation(int sessionId, long evaluationId) {
        executor.execute(() -> {
            try {
                database.runInTransaction(() -> {
                    Evaluation evaluation = database.evaluationDao().getById(evaluationId);
                    if (evaluation == null || evaluation.getSessionId() != sessionId
                            || !evaluation.isFinalized()) {
                        throw new IllegalArgumentException("Cette évaluation n'existe plus.");
                    }
                    if (database.evaluationDao().deleteByIdAndSession(evaluationId, sessionId) == 0) {
                        throw new IllegalArgumentException("L'évaluation n'a pas pu être supprimée.");
                    }
                });
                sheetState.postValue(chargerEtat(sessionId, -1, true));
                operationResult.postValue(new OperationResult(
                        true, "Évaluation supprimée.", false, false));
            } catch (Exception e) {
                publierErreur(e, "Impossible de supprimer l'évaluation.");
            }
        });
    }

    private void enregistrerNotes(
            long teamId,
            List<ScoreInput> notes) {

        EvaluationTeam team =
                database.evaluationTeamDao().getById(teamId);

        if (team == null) {
            throw new IllegalArgumentException(
                    "L'équipe n'existe plus."
            );
        }

        Evaluation evaluation =
                database.evaluationDao()
                        .getById(team.getEvaluationId());

        if (evaluation == null) {
            throw new IllegalArgumentException(
                    "L'évaluation n'existe plus."
            );
        }

        for (ScoreInput input : notes) {
            SessionCriterion criterion =
                    database.sessionCriterionDao()
                            .getById(input.criterionId);

            if (criterion == null
                    || criterion.getSessionId()
                    != evaluation.getSessionId()) {
                throw new IllegalArgumentException(
                        "Un critère ne correspond pas à cette séance."
                );
            }

            if (!Double.isFinite(input.score)
                    || input.score < 0
                    || input.score > criterion.getMaxScore()) {
                throw new IllegalArgumentException(
                        "Une note dépasse le barème autorisé."
                );
            }

            database.evaluationScoreDao().insertOrUpdate(
                    new EvaluationScore(
                            teamId,
                            input.criterionId,
                            input.score
                    )
            );
        }
    }

    private List<ScoreInput> copierScores(
            List<EvaluationScore> scores) {

        List<ScoreInput> copies = new ArrayList<>();

        if (scores != null) {
            for (EvaluationScore score : scores) {
                if (score != null) {
                    copies.add(
                            new ScoreInput(
                                    score.getCriterionId(),
                                    score.getScore()
                            )
                    );
                }
            }
        }

        return copies;
    }

    public void clearOperationResult() {
        operationResult.setValue(null);
    }

    private void publierErreur(
            Exception exception,
            String fallback) {

        String message = exception.getMessage();

        operationResult.postValue(
                new OperationResult(
                        false,
                        message == null || message.trim().isEmpty()
                                ? fallback
                                : message,
                        false,
                        false
                )
        );
    }

    private String valeurOuVide(String value) {
        return value == null ? "" : value;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    private static final class ScoreInput {
        final long criterionId;
        final double score;

        ScoreInput(long criterionId, double score) {
            this.criterionId = criterionId;
            this.score = score;
        }
    }

    public static final class OperationResult {
        private final boolean success;
        private final String message;
        private final boolean closeScreen;
        private final boolean clearInputs;

        public OperationResult(
                boolean success,
                String message,
                boolean closeScreen,
                boolean clearInputs) {
            this.success = success;
            this.message = message;
            this.closeScreen = closeScreen;
            this.clearInputs = clearInputs;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public boolean shouldCloseScreen() {
            return closeScreen;
        }

        public boolean shouldClearInputs() {
            return clearInputs;
        }
    }

    public static final class SheetState {
        private final Session session;
        private final List<SessionCriterion> criteria;
        private final List<EvaluationTeamState> teams;
        private final List<Evaluation> evaluations;
        private final int totalStudents;
        private final int moduleLevelId;
        private final int moduleSpecializationId;
        private final long evaluationId;
        private final List<String> evaluatedStudentIds;

        SheetState(
                Session session,
                List<SessionCriterion> criteria,
                List<EvaluationTeamState> teams,
                List<Evaluation> evaluations,
                int totalStudents,
                int moduleLevelId,
                int moduleSpecializationId,
                long evaluationId,
                List<String> evaluatedStudentIds) {
            this.session = session;
            this.criteria = new ArrayList<>(criteria);
            this.teams = new ArrayList<>(teams);
            this.evaluations = evaluations == null ? new ArrayList<>() : new ArrayList<>(evaluations);
            this.totalStudents = totalStudents;
            this.moduleLevelId = moduleLevelId;
            this.moduleSpecializationId = moduleSpecializationId;
            this.evaluationId = evaluationId;
            this.evaluatedStudentIds = evaluatedStudentIds == null
                    ? new ArrayList<>() : new ArrayList<>(evaluatedStudentIds);
        }

        public Session getSession() {
            return session;
        }

        public List<SessionCriterion> getCriteria() {
            return new ArrayList<>(criteria);
        }

        public List<EvaluationTeamState> getTeams() {
            return new ArrayList<>(teams);
        }

        public List<Evaluation> getEvaluations() { return new ArrayList<>(evaluations); }

        public int getTotalStudents() {
            return totalStudents;
        }

        public int getModuleLevelId() { return moduleLevelId; }

        public int getModuleSpecializationId() { return moduleSpecializationId; }

        public long getEvaluationId() {
            return evaluationId;
        }

        public List<String> getEvaluatedStudentIds() {
            return new ArrayList<>(evaluatedStudentIds);
        }
    }

    public static final class EvaluationTeamState {
        private final EvaluationTeam team;
        private final List<StudentWithGroup> students;
        private final List<SessionCriterion> criteria;
        private final List<EvaluationScore> scores;
        private final boolean evaluated;

        EvaluationTeamState(
                EvaluationTeam team,
                List<StudentWithGroup> students,
                List<SessionCriterion> criteria,
                List<EvaluationScore> scores,
                boolean evaluated) {
            this.team = team;
            this.students = new ArrayList<>(students);
            this.criteria = new ArrayList<>(criteria);
            this.scores = new ArrayList<>(scores);
            this.evaluated = evaluated;
        }

        public EvaluationTeam getTeam() {
            return team;
        }

        public List<StudentWithGroup> getStudents() {
            return new ArrayList<>(students);
        }

        public List<SessionCriterion> getCriteria() {
            return new ArrayList<>(criteria);
        }

        public List<EvaluationScore> getScores() {
            return new ArrayList<>(scores);
        }

        public double getTotalScore() {
            double total = 0;

            for (EvaluationScore score : scores) {
                total += score.getScore();
            }

            return total;
        }

        public double getMaxScore() {
            double total = 0;

            for (SessionCriterion criterion : criteria) {
                total += criterion.getMaxScore();
            }

            return total;
        }

        public boolean isEvaluated() {
            return evaluated;
        }

        public double getScoreSur20() {
            double max = getMaxScore();

            return max <= 0
                    ? 0
                    : getTotalScore() * 20.0 / max;
        }
    }

    public static final class StudentWithGroup {
        private final Student student;
        private final String groupName;
        private final int groupOrder;

        StudentWithGroup(Student student, String groupName, int groupOrder) {
            this.student = student;
            this.groupName = groupName == null ? "" : groupName;
            this.groupOrder = groupOrder;
        }

        public Student getStudent() { return student; }
        public String getGroupName() { return groupName; }
        public int getGroupOrder() { return groupOrder; }
    }
    private String messageErreur(Exception exception, String fallback) {
        if (exception == null) {
            return fallback;
        }

        String message = exception.getMessage();

        if (message == null || message.trim().isEmpty()) {
            return fallback;
        }

        return message;
    }
}






