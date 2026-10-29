
package com.example.praticum.ui.evaluation;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;
import com.example.praticum.ui.attendance.AttendanceDataMemory;
import com.example.praticum.ui.attendance.AttendanceRecordMemory;
import com.example.praticum.ui.sessions.SessionMemory;
import com.example.praticum.ui.sessions.SessionCriterion;
import com.example.praticum.ui.sessions.SessionRepositoryMemory;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class EvaluationSheetFragment extends Fragment {

    private EvaluationSheetMemory sheet;
    private SessionMemory session;
    private EvaluationTeamMemory currentTeam;
    private int currentTeamIndex = 0;

    private TextView sessionView;
    private TextView maxScoreView;
    private TextView progressView;
    private TextView remainingView;
    private TextView memberCountView;
    private TextView teamNameView;
    private TextView totalStatusView;
    private TextView totalView;
    private TextView positionView;

    private ProgressBar progressBar;
    private AutoCompleteTextView groupSearch;
    private EditText orderSearch;

    private RecyclerView studentsRecycler;
    private RecyclerView criteriaRecycler;

    private EvaluationTeamAdapter teamAdapter;
    private EvaluationCriterionAdapter criterionAdapter;

    public EvaluationSheetFragment() {
        // Constructeur requis.
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_evaluation_sheet,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();

        int sessionId = args == null
                ? -1
                : args.getInt("session_id", -1);

        String sessionTitle = args == null
                ? ""
                : args.getString("session_title", "");

        if (sessionId < 0) {
            showMessage("Séance invalide.");
            getParentFragmentManager().popBackStack();
            return;
        }

        // Récupérer la séance et ses critères configurés.
        session = SessionRepositoryMemory.getInstance()
                .getSessionById(sessionId);

        if (session == null) {
            showMessage("Cette séance est introuvable.");
            getParentFragmentManager().popBackStack();
            return;
        }

        String titre = session.getTitle();

        if (titre == null || titre.trim().isEmpty()) {
            titre = sessionTitle;
        }

        sheet = EvaluationRepositoryMemory.getInstance()
                .getSheet(sessionId, titre);

        // Le barème de la fiche dépend des critères de la séance.
        actualiserBareme();

        bindViews(view);
        configurerRetour(view);
        configurerListes();
        configurerRechercheGroupe();
        configurerAjoutEtNavigation(view);
        configurerEnregistrement(view);

        initialiserEquipes();
        actualiserProgression();
    }

    private void bindViews(View view) {
        sessionView = view.findViewById(R.id.tvEvaluationSession);
        maxScoreView = view.findViewById(R.id.tvEvaluationMaxScore);
        progressView = view.findViewById(R.id.tvGradeProgress);
        remainingView = view.findViewById(R.id.tvGradeRemaining);
        progressBar = view.findViewById(R.id.progressGradeEntry);
        memberCountView = view.findViewById(R.id.tvEvaluationMemberCount);
        teamNameView = view.findViewById(R.id.tvEvaluationTeamName);
        totalStatusView = view.findViewById(R.id.tvEvaluationTotalStatus);
        totalView = view.findViewById(R.id.tvEvaluationTotal);
        positionView = view.findViewById(R.id.tvEvaluationPosition);

        groupSearch = view.findViewById(R.id.autoCompleteSearchGroup);
        orderSearch = view.findViewById(R.id.etSearchOrder);

        studentsRecycler = view.findViewById(R.id.rvEvaluationStudents);
        criteriaRecycler = view.findViewById(R.id.rvEvaluationCriteria);

        if (sessionView != null) {
            sessionView.setText(sheet.getSessionTitle());
        }

        afficherBareme();

        View scanButton = view.findViewById(R.id.btnScanTeamResponse);

        if (scanButton != null) {
            scanButton.setOnClickListener(v ->
                    showMessage(
                            "La numérisation sera ajoutée ultérieurement."
                    )
            );
        }
    }

    private void afficherBareme() {
        if (maxScoreView == null || session == null) {
            return;
        }

        double max = calculerBaremeSession();

        maxScoreView.setText(
                String.format(
                        Locale.getDefault(),
                        "%.2f points",
                        max
                )
        );
    }

    private double calculerBaremeSession() {
        if (session == null) {
            return 0;
        }

        double total = 0;

        for (SessionCriterion criterion : session.getCriteria()) {
            if (criterion != null
                    && Double.isFinite(criterion.getMaxScore())
                    && criterion.getMaxScore() > 0) {

                total += criterion.getMaxScore();
            }
        }

        return total;
    }

    private void actualiserBareme() {
        if (sheet == null || session == null) {
            return;
        }

        sheet.setMaxScore(calculerBaremeSession());
    }

    private void configurerRetour(View view) {
        View backButton = view.findViewById(R.id.btnEvaluationBack);

        if (backButton != null) {
            backButton.setOnClickListener(v ->
                    getParentFragmentManager().popBackStack()
            );
        }
    }

    private void configurerListes() {
        studentsRecycler.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        criteriaRecycler.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
    }

    private void configurerRechercheGroupe() {
        List<String> groups = AttendanceDataMemory.getGroups();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                groups
        );

        groupSearch.setAdapter(adapter);
        groupSearch.setThreshold(1);
    }

    private void initialiserEquipes() {
        if (sheet.getTeams().isEmpty()) {
            currentTeamIndex = -1;
            currentTeam = null;
            afficherFicheVide();
            return;
        }

        currentTeamIndex = 0;
        afficherEquipeCourante();
    }

    private void afficherFicheVide() {
        currentTeam = null;
        currentTeamIndex = -1;

        teamNameView.setText("Nouvelle équipe");
        memberCountView.setText("0 étudiant");
        positionView.setText("Aucune équipe");

        afficherBareme();

        totalView.setText(
                String.format(
                        Locale.getDefault(),
                        "%.2f / %.2f",
                        0.0,
                        calculerBaremeSession()
                )
        );

        totalStatusView.setText("Aucune note saisie");

        studentsRecycler.setAdapter(null);
        criteriaRecycler.setAdapter(null);

        if (progressBar != null) {
            progressBar.setProgress(0);
        }

        actualiserProgression();

        EvaluationRepositoryMemory.getInstance().saveSheet(sheet);
    }

    private void afficherEquipeCourante() {
        if (sheet.getTeams().isEmpty()) {
            afficherFicheVide();
            return;
        }

        currentTeamIndex = Math.max(
                0,
                Math.min(currentTeamIndex, sheet.getTeams().size() - 1)
        );

        currentTeam = sheet.getTeams().get(currentTeamIndex);

        teamNameView.setText(currentTeam.getName());

        memberCountView.setText(
                currentTeam.getStudents().size() + " étudiant(s)"
        );

        positionView.setText(
                (currentTeamIndex + 1) + " / " + sheet.getTeams().size()
        );

        teamAdapter = new EvaluationTeamAdapter(
                currentTeam.getStudents(),
                this::retirerEtudiant
        );

        studentsRecycler.setAdapter(teamAdapter);

        criterionAdapter = new EvaluationCriterionAdapter(
                currentTeam.getCriteria(),
                this::actualiserTotal
        );

        criteriaRecycler.setAdapter(criterionAdapter);

        actualiserBareme();
        actualiserTotal();
    }

    private void configurerAjoutEtNavigation(View view) {
        View addButton = view.findViewById(R.id.btnAddStudentToTeam);
        View previousButton = view.findViewById(R.id.btnPreviousEvaluation);
        View nextButton = view.findViewById(R.id.btnNextEvaluation);

        if (addButton != null) {
            addButton.setOnClickListener(v -> ajouterEtudiant());
        }

        if (previousButton != null) {
            previousButton.setOnClickListener(v -> {
                if (!sheet.getTeams().isEmpty()
                        && currentTeamIndex > 0) {

                    currentTeamIndex--;
                    afficherEquipeCourante();
                }
            });
        }

        if (nextButton != null) {
            nextButton.setOnClickListener(v -> {
                if (!sheet.getTeams().isEmpty()
                        && currentTeamIndex < sheet.getTeams().size() - 1) {

                    currentTeamIndex++;
                    afficherEquipeCourante();
                }
            });
        }
    }

    private void ajouterEtudiant() {
        String groupName = groupSearch.getText().toString().trim();
        String orderText = orderSearch.getText().toString().trim();

        if (TextUtils.isEmpty(groupName)) {
            groupSearch.setError("Choisissez un groupe");
            return;
        }

        if (TextUtils.isEmpty(orderText)) {
            orderSearch.setError("Saisissez l'ordre de l'étudiant");
            return;
        }

        int order;

        try {
            order = Integer.parseInt(orderText);
        } catch (NumberFormatException e) {
            orderSearch.setError("Ordre invalide");
            return;
        }

        AttendanceRecordMemory selected = null;

        for (AttendanceRecordMemory student :
                AttendanceDataMemory.getStudents(groupName)) {

            if (student.getGroupOrder() == order) {
                selected = student;
                break;
            }
        }

        if (selected == null) {
            showMessage(
                    "Aucun étudiant trouvé pour ce groupe et cet ordre."
            );
            return;
        }

        // Créer une équipe lors du premier ajout.
        if (currentTeam == null) {
            if (session.getCriteria().isEmpty()) {
                showMessage(
                        "Configurez d'abord les critères de cette séance."
                );
                return;
            }

            currentTeam = new EvaluationTeamMemory(
                    sheet.getTeams().size() + 1,
                    "Équipe " + (sheet.getTeams().size() + 1)
            );

            initialiserCriteres(currentTeam);

            sheet.addTeam(currentTeam);
            currentTeamIndex = sheet.getTeams().size() - 1;
        }

        // Empêcher qu'un étudiant soit affecté à plusieurs équipes.
        for (EvaluationTeamMemory team : sheet.getTeams()) {
            if (team == currentTeam) {
                continue;
            }

            for (AttendanceRecordMemory student : team.getStudents()) {
                if (student.getStudentId() == selected.getStudentId()) {
                    showMessage(
                            "Cet étudiant appartient déjà à une autre équipe."
                    );
                    return;
                }
            }
        }

        if (!currentTeam.addStudent(selected)) {
            showMessage("Cet étudiant est déjà dans cette équipe.");
            return;
        }

        groupSearch.setText("", false);
        orderSearch.setText("");

        afficherEquipeCourante();

        EvaluationRepositoryMemory.getInstance().saveSheet(sheet);
    }

    private void initialiserCriteres(EvaluationTeamMemory team) {
        if (team == null || session == null) {
            return;
        }

        team.getCriteria().clear();

        List<SessionCriterion> criteresSession = session.getCriteria();

        for (int i = 0; i < criteresSession.size(); i++) {
            SessionCriterion critere = criteresSession.get(i);

            if (critere == null) {
                continue;
            }

            double maxScore = critere.getMaxScore();

            if (!Double.isFinite(maxScore) || maxScore <= 0) {
                continue;
            }

            String title = critere.getTitle() == null
                    ? ""
                    : critere.getTitle().trim();

            String description = critere.getDescription() == null
                    ? ""
                    : critere.getDescription().trim();

            if (title.isEmpty()) {
                title = "Critère " + (i + 1);
            }

            team.getCriteria().add(
                    new EvaluationCriterion(
                            "C" + (i + 1),
                            title,
                            description,
                            maxScore
                    )
            );
        }
    }

    private void retirerEtudiant(AttendanceRecordMemory student) {
        if (currentTeam == null || student == null) {
            return;
        }

        currentTeam.removeStudent(student.getStudentId());

        EvaluationRepositoryMemory.getInstance().saveSheet(sheet);

        afficherEquipeCourante();
    }

    private void actualiserTotal() {
        if (currentTeam == null) {
            if (totalView != null) {
                totalView.setText(
                        String.format(
                                Locale.getDefault(),
                                "%.2f / %.2f",
                                0.0,
                                calculerBaremeSession()
                        )
                );
            }

            if (totalStatusView != null) {
                totalStatusView.setText("Aucune note saisie");
            }

            actualiserProgression();
            return;
        }

        double total = currentTeam.getTotalScore();
        double max = currentTeam.getMaxScore();

        totalView.setText(
                String.format(
                        Locale.getDefault(),
                        "%.2f / %.2f",
                        total,
                        max
                )
        );

        totalStatusView.setText(
                total > 0
                        ? "Notation en cours"
                        : "Aucune note saisie"
        );

        actualiserProgression();

        EvaluationRepositoryMemory.getInstance().saveSheet(sheet);
    }

    private void actualiserProgression() {
        int totalEtudiants = 0;

        for (String groupe : AttendanceDataMemory.getGroups()) {
            totalEtudiants += AttendanceDataMemory.getStudents(groupe).size();
        }

        Set<Integer> etudiantsEvalués = new HashSet<>();

        for (EvaluationTeamMemory team : sheet.getTeams()) {
            for (AttendanceRecordMemory student : team.getStudents()) {
                etudiantsEvalués.add(student.getStudentId());
            }
        }

        int nombreEvalués = etudiantsEvalués.size();

        int nombreRestants = Math.max(
                0,
                totalEtudiants - nombreEvalués
        );

        int pourcentage = totalEtudiants == 0
                ? 0
                : nombreEvalués * 100 / totalEtudiants;

        if (progressBar != null) {
            progressBar.setMax(100);
            progressBar.setProgress(pourcentage);
        }

        if (progressView != null) {
            progressView.setText(
                    nombreEvalués + " / " + totalEtudiants
                            + " étudiants évalués"
            );
        }

        if (remainingView != null) {
            remainingView.setText(
                    nombreRestants + " étudiant(s) restant(s)"
            );
        }
    }

    private void configurerEnregistrement(View view) {
        Button saveButton = view.findViewById(R.id.btnSaveEvaluation);

        if (saveButton != null) {
            saveButton.setOnClickListener(v -> {
                EvaluationRepositoryMemory.getInstance().saveSheet(sheet);

                showMessage("Évaluation enregistrée en mémoire.");

                getParentFragmentManager().popBackStack();
            });
        }
    }

    private void showMessage(String message) {
        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}
