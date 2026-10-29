package com.example.praticum.ui.evaluation;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.praticum.data.database.AppDatabase;
import com.example.praticum.data.database.DatabaseProvider;
import com.example.praticum.data.entity.Evaluation;
import com.example.praticum.data.entity.EvaluationScore;
import com.example.praticum.data.entity.EvaluationTeam;
import com.example.praticum.data.entity.EvaluationTeamMember;
import com.example.praticum.data.entity.Session;
import com.example.praticum.data.entity.SessionCriterion;
import com.example.praticum.data.entity.Student;
import com.example.praticum.data.entity.StudentGroup;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
                sheetState.postValue(chargerEtat(sessionId));
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

    private SheetState chargerEtat(int sessionId) {
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

        List<StudentGroup> groupes =
                database.studentGroupDao().getAll();

        Set<String> nomsUniques = new LinkedHashSet<>();

        for (StudentGroup groupe : groupes) {
            if (groupe.getName() != null
                    && !groupe.getName().trim().isEmpty()) {
                nomsUniques.add(groupe.getName().trim());
            }
        }

        List<String> groupNames = new ArrayList<>(nomsUniques);

        Evaluation evaluation =
                database.evaluationDao()
                        .getBySessionId(sessionId);

        List<EvaluationTeamState> teams = new ArrayList<>();

        if (evaluation != null) {
            List<EvaluationTeam> roomTeams =
                    database.evaluationTeamDao()
                            .getByEvaluationId(evaluation.getId());

            for (EvaluationTeam team : roomTeams) {
                teams.add(chargerEquipe(team, criteria));
            }
        }

        int totalStudents =
                database.studentDao().getAll().size();

        return new SheetState(
                session,
                criteria,
                groupNames,
                teams,
                totalStudents,
                evaluation != null
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

            StudentGroup group =
                    database.studentGroupDao()
                            .getById(student.getGroupId());

            students.add(
                    new StudentWithGroup(
                            student,
                            group == null ? "" : group.getName()
                    )
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

        return new EvaluationTeamState(
                team,
                students,
                validCriteria,
                scores
        );
    }

    public void addStudent(
            int sessionId,
            long selectedTeamId,
            String groupName,
            int groupOrder) {

        executor.execute(() -> {
            try {
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

                    StudentGroup selectedGroup = null;

                    for (StudentGroup group
                            : database.studentGroupDao().getAll()) {
                        if (groupName.equals(group.getName())) {
                            selectedGroup = group;
                            break;
                        }
                    }

                    if (selectedGroup == null) {
                        throw new IllegalArgumentException(
                                "Le groupe sélectionné n'existe plus."
                        );
                    }

                    Student selectedStudent = null;

                    for (Student student
                            : database.studentDao().getByGroupId(
                            selectedGroup.getId())) {

                        if (student.getGroupOrder() == groupOrder) {
                            selectedStudent = student;
                            break;
                        }
                    }

                    if (selectedStudent == null) {
                        throw new IllegalArgumentException(
                                "Aucun étudiant trouvé pour ce groupe "
                                        + "et cet ordre."
                        );
                    }

                    Evaluation evaluation =
                            database.evaluationDao()
                                    .getBySessionId(sessionId);

                    if (evaluation == null) {
                        evaluation = new Evaluation(sessionId);

                        long id = database.evaluationDao()
                                .insert(evaluation);

                        evaluation.setId((int) id);
                    }

                    EvaluationTeam team;

                    if (selectedTeamId > 0) {
                        team = database.evaluationTeamDao()
                                .getById(selectedTeamId);

                        if (team == null
                                || team.getEvaluationId()
                                != evaluation.getId()) {
                            throw new IllegalArgumentException(
                                    "L'équipe sélectionnée n'existe plus."
                            );
                        }
                    } else {
                        Integer maxNumber =
                                database.evaluationTeamDao()
                                        .getMaxTeamNumber(
                                                evaluation.getId()
                                        );

                        int nextNumber = maxNumber == null
                                ? 1
                                : maxNumber + 1;

                        team = new EvaluationTeam(
                                evaluation.getId(),
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
                                            evaluation.getId(),
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
                                    evaluation.getId()
                            )
                    );
                });

                sheetState.postValue(chargerEtat(sessionId));

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

                sheetState.postValue(chargerEtat(sessionId));

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
            long teamId,
            List<EvaluationScore> scores) {

        List<ScoreInput> copies = copierScores(scores);

        executor.execute(() -> {
            try {
                database.runInTransaction(() -> {
                    Session session =
                            database.sessionDao().getById(sessionId);

                    if (session == null) {
                        throw new IllegalArgumentException(
                                "La séance n'existe plus."
                        );
                    }

                    Evaluation evaluation =
                            database.evaluationDao()
                                    .getBySessionId(sessionId);

                    if (evaluation == null) {
                        evaluation = new Evaluation(sessionId);

                        long id = database.evaluationDao()
                                .insert(evaluation);

                        evaluation.setId((int) id);
                    }

                    database.evaluationDao()
                            .setCommonScoringMode(evaluation.getId());

                    if (teamId > 0) {
                        enregistrerNotes(teamId, copies);
                    }
                });

                sheetState.postValue(chargerEtat(sessionId));

                operationResult.postValue(
                        new OperationResult(
                                true,
                                "Évaluation enregistrée.",
                                true,
                                false
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
        private final List<String> groupNames;
        private final List<EvaluationTeamState> teams;
        private final int totalStudents;
        private final boolean evaluationExists;

        SheetState(
                Session session,
                List<SessionCriterion> criteria,
                List<String> groupNames,
                List<EvaluationTeamState> teams,
                int totalStudents,
                boolean evaluationExists) {
            this.session = session;
            this.criteria = new ArrayList<>(criteria);
            this.groupNames = new ArrayList<>(groupNames);
            this.teams = new ArrayList<>(teams);
            this.totalStudents = totalStudents;
            this.evaluationExists = evaluationExists;
        }

        public Session getSession() {
            return session;
        }

        public List<SessionCriterion> getCriteria() {
            return new ArrayList<>(criteria);
        }

        public List<String> getGroupNames() {
            return new ArrayList<>(groupNames);
        }

        public List<EvaluationTeamState> getTeams() {
            return new ArrayList<>(teams);
        }

        public int getTotalStudents() {
            return totalStudents;
        }

        public boolean hasEvaluation() {
            return evaluationExists;
        }
    }

    public static final class EvaluationTeamState {
        private final EvaluationTeam team;
        private final List<StudentWithGroup> students;
        private final List<SessionCriterion> criteria;
        private final List<EvaluationScore> scores;

        EvaluationTeamState(
                EvaluationTeam team,
                List<StudentWithGroup> students,
                List<SessionCriterion> criteria,
                List<EvaluationScore> scores) {
            this.team = team;
            this.students = new ArrayList<>(students);
            this.criteria = new ArrayList<>(criteria);
            this.scores = new ArrayList<>(scores);
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
            for (EvaluationScore score : scores) {
                if (score.getScore() > 0) {
                    return true;
                }
            }

            return false;
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

        StudentWithGroup(Student student, String groupName) {
            this.student = student;
            this.groupName = groupName == null ? "" : groupName;
        }

        public Student getStudent() {
            return student;
        }

        public String getGroupName() {
            return groupName;
        }
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