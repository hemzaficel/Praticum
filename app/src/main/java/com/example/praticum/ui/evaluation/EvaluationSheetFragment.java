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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;
import com.example.praticum.data.entity.EvaluationScore;
import com.example.praticum.data.entity.SessionCriterion;
import com.example.praticum.data.entity.Student;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class EvaluationSheetFragment extends Fragment {

    private EvaluationViewModel viewModel;

    private EvaluationViewModel.SheetState state;

    private EvaluationViewModel.EvaluationTeamState currentTeam;

    private int sessionId = -1;
    private String sessionTitle = "";

    private int currentTeamIndex = -1;
    private long currentTeamId = -1;

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

    private ArrayAdapter<String> groupAdapter;

    public EvaluationSheetFragment() {
        // Constructeur public requis par Android.
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

        viewModel = new ViewModelProvider(this)
                .get(EvaluationViewModel.class);

        Bundle args = getArguments();

        sessionId = args == null
                ? -1
                : args.getInt("session_id", -1);

        sessionTitle = args == null
                ? ""
                : args.getString("session_title", "");

        if (sessionId <= 0) {
            showMessage("Séance invalide.");
            getParentFragmentManager().popBackStack();
            return;
        }

        bindViews(view);
        configurerRetour(view);
        configurerListes();
        configurerRechercheGroupe();
        configurerAjoutEtNavigation(view);
        configurerEnregistrement(view);
        observerDonnees();

        viewModel.loadSheet(sessionId);
    }

    private void bindViews(View view) {
        sessionView = view.findViewById(
                R.id.tvEvaluationSession
        );

        maxScoreView = view.findViewById(
                R.id.tvEvaluationMaxScore
        );

        progressView = view.findViewById(
                R.id.tvGradeProgress
        );

        remainingView = view.findViewById(
                R.id.tvGradeRemaining
        );

        progressBar = view.findViewById(
                R.id.progressGradeEntry
        );

        memberCountView = view.findViewById(
                R.id.tvEvaluationMemberCount
        );

        teamNameView = view.findViewById(
                R.id.tvEvaluationTeamName
        );

        totalStatusView = view.findViewById(
                R.id.tvEvaluationTotalStatus
        );

        totalView = view.findViewById(
                R.id.tvEvaluationTotal
        );

        positionView = view.findViewById(
                R.id.tvEvaluationPosition
        );

        groupSearch = view.findViewById(
                R.id.autoCompleteSearchGroup
        );

        orderSearch = view.findViewById(
                R.id.etSearchOrder
        );

        studentsRecycler = view.findViewById(
                R.id.rvEvaluationStudents
        );

        criteriaRecycler = view.findViewById(
                R.id.rvEvaluationCriteria
        );

        if (sessionView != null) {
            sessionView.setText(sessionTitle);
        }

        View scanButton = view.findViewById(
                R.id.btnScanTeamResponse
        );

        if (scanButton != null) {
            scanButton.setOnClickListener(v ->
                    showMessage(
                            "La numérisation sera ajoutée ultérieurement."
                    )
            );
        }
    }

    private void observerDonnees() {
        viewModel.getSheetState().observe(
                getViewLifecycleOwner(),
                nouvelEtat -> {
                    if (nouvelEtat == null || !isAdded()) {
                        return;
                    }

                    afficherEtat(nouvelEtat);
                }
        );

        viewModel.getOperationResult().observe(
                getViewLifecycleOwner(),
                resultat -> {
                    if (resultat == null) {
                        return;
                    }

                    viewModel.clearOperationResult();

                    if (!resultat.isSuccess()) {
                        showMessage(resultat.getMessage());

                        if (resultat.shouldCloseScreen()
                                && isAdded()) {
                            getParentFragmentManager().popBackStack();
                        }

                        return;
                    }

                    if (resultat.shouldClearInputs()) {
                        if (groupSearch != null) {
                            groupSearch.setText("", false);
                            groupSearch.setError(null);
                        }

                        if (orderSearch != null) {
                            orderSearch.setText("");
                            orderSearch.setError(null);
                        }
                    }

                    if (!resultat.getMessage().trim().isEmpty()) {
                        showMessage(resultat.getMessage());
                    }

                    if (resultat.shouldCloseScreen()) {
                        getParentFragmentManager().popBackStack();
                    }
                }
        );
    }

    private void afficherEtat(
            EvaluationViewModel.SheetState nouvelEtat) {

        state = nouvelEtat;

        if (sessionView != null) {
            String title = state.getSession().getTitle();

            sessionView.setText(
                    title == null || title.trim().isEmpty()
                            ? sessionTitle
                            : title
            );
        }

        configurerGroupes();

        List<EvaluationViewModel.EvaluationTeamState> teams =
                state.getTeams();

        if (teams.isEmpty()) {
            currentTeamId = -1;
            currentTeamIndex = -1;

            afficherFicheVide();

        } else {
            int index = trouverIndexEquipeCourante(teams);

            if (index < 0) {
                index = Math.max(
                        0,
                        Math.min(
                                currentTeamIndex,
                                teams.size() - 1
                        )
                );
            }

            currentTeamIndex = index;
            afficherEquipeCourante();
        }

        actualiserBareme();
        actualiserProgression();
    }

    private int trouverIndexEquipeCourante(
            List<EvaluationViewModel.EvaluationTeamState> teams) {

        if (currentTeamId <= 0) {
            return -1;
        }

        for (int i = 0; i < teams.size(); i++) {
            if (teams.get(i).getTeam().getId() == currentTeamId) {
                return i;
            }
        }

        return -1;
    }

    private void configurerGroupes() {
        if (groupSearch == null || state == null) {
            return;
        }

        groupAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                state.getGroupNames()
        );

        groupSearch.setAdapter(groupAdapter);
        groupSearch.setThreshold(1);
    }

    private void configurerRetour(View view) {
        View backButton = view.findViewById(
                R.id.btnEvaluationBack
        );

        if (backButton != null) {
            backButton.setOnClickListener(v ->
                    getParentFragmentManager().popBackStack()
            );
        }
    }

    private void configurerListes() {
        if (studentsRecycler != null) {
            studentsRecycler.setLayoutManager(
                    new LinearLayoutManager(requireContext())
            );
        }

        if (criteriaRecycler != null) {
            criteriaRecycler.setLayoutManager(
                    new LinearLayoutManager(requireContext())
            );
        }
    }

    private void configurerRechercheGroupe() {
        if (groupSearch == null) {
            return;
        }

        groupAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line
        );

        groupSearch.setAdapter(groupAdapter);
        groupSearch.setThreshold(1);
    }

    private void afficherFicheVide() {
        currentTeam = null;

        if (teamNameView != null) {
            teamNameView.setText("Nouvelle équipe");
        }

        if (memberCountView != null) {
            memberCountView.setText("0 étudiant");
        }

        if (positionView != null) {
            positionView.setText("Aucune équipe");
        }

        if (studentsRecycler != null) {
            studentsRecycler.setAdapter(null);
        }

        if (criteriaRecycler != null) {
            criteriaRecycler.setAdapter(null);
        }

        actualiserTotal();
    }

    private void afficherEquipeCourante() {
        if (state == null || state.getTeams().isEmpty()) {
            afficherFicheVide();
            return;
        }

        List<EvaluationViewModel.EvaluationTeamState> teams =
                state.getTeams();

        currentTeamIndex = Math.max(
                0,
                Math.min(currentTeamIndex, teams.size() - 1)
        );

        currentTeam = teams.get(currentTeamIndex);
        currentTeamId = currentTeam.getTeam().getId();

        if (teamNameView != null) {
            teamNameView.setText(
                    currentTeam.getTeam().getName()
            );
        }

        if (memberCountView != null) {
            int count = currentTeam.getStudents().size();

            memberCountView.setText(
                    count
                            + (count > 1
                            ? " étudiants"
                            : " étudiant")
            );
        }

        if (positionView != null) {
            positionView.setText(
                    (currentTeamIndex + 1)
                            + " / "
                            + teams.size()
            );
        }

        teamAdapter = new EvaluationTeamAdapter(
                currentTeam.getStudents(),
                this::retirerEtudiant
        );

        if (studentsRecycler != null) {
            studentsRecycler.setAdapter(teamAdapter);
        }

        criterionAdapter = new EvaluationCriterionAdapter(
                currentTeam.getCriteria(),
                currentTeam.getScores(),
                this::onScoresChanged
        );

        if (criteriaRecycler != null) {
            criteriaRecycler.setAdapter(criterionAdapter);
        }

        actualiserTotal();
    }

    private void configurerAjoutEtNavigation(View view) {
        View addButton = view.findViewById(
                R.id.btnAddStudentToTeam
        );

        View previousButton = view.findViewById(
                R.id.btnPreviousEvaluation
        );

        View nextButton = view.findViewById(
                R.id.btnNextEvaluation
        );

        if (addButton != null) {
            addButton.setOnClickListener(v -> ajouterEtudiant());
        }

        if (previousButton != null) {
            previousButton.setOnClickListener(v -> {
                if (state != null && currentTeamIndex > 0) {
                    currentTeamIndex--;
                    afficherEquipeCourante();
                }
            });
        }

        if (nextButton != null) {
            nextButton.setOnClickListener(v -> {
                if (state != null
                        && currentTeamIndex
                        < state.getTeams().size() - 1) {

                    currentTeamIndex++;
                    afficherEquipeCourante();
                }
            });
        }
    }

    private void ajouterEtudiant() {
        if (state == null) {
            showMessage("Les données ne sont pas encore chargées.");
            return;
        }

        String groupName = groupSearch.getText()
                .toString()
                .trim();

        String orderText = orderSearch.getText()
                .toString()
                .trim();

        if (TextUtils.isEmpty(groupName)) {
            groupSearch.setError("Choisissez un groupe");
            return;
        }

        if (TextUtils.isEmpty(orderText)) {
            orderSearch.setError(
                    "Saisissez l'ordre de l'étudiant"
            );
            return;
        }

        int order;

        try {
            order = Integer.parseInt(orderText);
        } catch (NumberFormatException e) {
            orderSearch.setError("Ordre invalide");
            return;
        }

        long teamId = currentTeam == null
                ? -1
                : currentTeam.getTeam().getId();

        viewModel.addStudent(
                sessionId,
                teamId,
                groupName,
                order
        );
    }

    private void retirerEtudiant(
            EvaluationViewModel.StudentWithGroup entry) {

        if (currentTeam == null || entry == null) {
            return;
        }

        Student student = entry.getStudent();

        viewModel.removeStudent(
                sessionId,
                currentTeam.getTeam().getId(),
                student.getStudentId()
        );
    }

    private void actualiserBareme() {
        if (maxScoreView == null) {
            return;
        }

        maxScoreView.setText(
                String.format(
                        Locale.getDefault(),
                        "%.2f points",
                        calculerBaremeSession()
                )
        );
    }

    private double calculerBaremeSession() {
        if (state == null) {
            return 0;
        }

        double total = 0;

        for (SessionCriterion criterion : state.getCriteria()) {
            double maximum = criterion.getMaxScore();

            if (Double.isFinite(maximum) && maximum > 0) {
                total += maximum;
            }
        }

        return total;
    }

    private void actualiserTotal() {
        double total = currentTeam == null
                ? 0
                : currentTeam.getTotalScore();

        double maximum = currentTeam == null
                ? calculerBaremeSession()
                : currentTeam.getMaxScore();

        if (totalView != null) {
            totalView.setText(
                    String.format(
                            Locale.getDefault(),
                            "%.2f / %.2f",
                            total,
                            maximum
                    )
            );
        }

        if (totalStatusView != null) {
            totalStatusView.setText(
                    total > 0
                            ? "Notation en cours"
                            : "Aucune note saisie"
            );
        }

        actualiserProgression();
    }

    private void onScoresChanged() {
        if (currentTeam == null) {
            return;
        }

        actualiserTotal();

        viewModel.saveScores(
                currentTeam.getTeam().getId(),
                currentTeam.getScores()
        );
    }

    private void actualiserProgression() {
        if (state == null) {
            return;
        }

        Set<String> evaluatedStudentIds = new HashSet<>();

        for (EvaluationViewModel.EvaluationTeamState team
                : state.getTeams()) {

            if (!team.isEvaluated()) {
                continue;
            }

            for (EvaluationViewModel.StudentWithGroup entry
                    : team.getStudents()) {

                evaluatedStudentIds.add(
                        entry.getStudent().getStudentId()
                );
            }
        }

        int totalStudents = state.getTotalStudents();
        int evaluatedCount = evaluatedStudentIds.size();

        int remainingCount = Math.max(
                0,
                totalStudents - evaluatedCount
        );

        int progress = totalStudents == 0
                ? 0
                : evaluatedCount * 100 / totalStudents;

        if (progressBar != null) {
            progressBar.setMax(100);
            progressBar.setProgress(progress);
        }

        if (progressView != null) {
            progressView.setText(
                    evaluatedCount + " / "
                            + totalStudents
                            + " étudiants évalués"
            );
        }

        if (remainingView != null) {
            remainingView.setText(
                    remainingCount + " étudiant(s) restant(s)"
            );
        }
    }

    private void configurerEnregistrement(View view) {
        Button saveButton = view.findViewById(
                R.id.btnSaveEvaluation
        );

        if (saveButton == null) {
            return;
        }

        saveButton.setOnClickListener(v -> {
            long teamId = currentTeam == null
                    ? -1
                    : currentTeam.getTeam().getId();

            List<EvaluationScore> scores = currentTeam == null
                    ? null
                    : currentTeam.getScores();

            viewModel.saveEvaluation(
                    sessionId,
                    teamId,
                    scores
            );
        });
    }

    private void showMessage(String message) {
        if (!isAdded()
                || message == null
                || message.trim().isEmpty()) {
            return;
        }

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onDestroyView() {
        sessionView = null;
        maxScoreView = null;
        progressView = null;
        remainingView = null;
        memberCountView = null;
        teamNameView = null;
        totalStatusView = null;
        totalView = null;
        positionView = null;

        progressBar = null;

        groupSearch = null;
        orderSearch = null;

        studentsRecycler = null;
        criteriaRecycler = null;

        teamAdapter = null;
        criterionAdapter = null;
        groupAdapter = null;

        super.onDestroyView();
    }
}