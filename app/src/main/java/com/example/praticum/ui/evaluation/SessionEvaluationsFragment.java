
package com.example.praticum.ui.evaluation;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;
import com.example.praticum.ui.attendance.AttendanceRecordMemory;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SessionEvaluationsFragment extends Fragment {

    private static final String ARG_SESSION_ID = "session_id";
    private static final String ARG_SESSION_TITLE = "session_title";

    private int sessionId = -1;
    private String sessionTitle = "";

    private EvaluationSheetMemory sheet;

    private TextView tvAverage;
    private TextView tvBestScore;
    private TextView tvResultCount;
    private TextView tvEmptyMessage;

    private TextInputEditText searchInput;
    private MaterialButtonToggleGroup filterGroup;
    private RecyclerView resultsRecyclerView;
    private View emptyLayout;

    private EvaluationStudentAdapter adapter;

    private final List<EvaluationTeamMemory> teams = new ArrayList<>();

    private final List<EvaluationStudentAdapter.StudentEvaluationItem> allItems =
            new ArrayList<>();

    private String searchQuery = "";

    private int activeFilter = R.id.btnEvaluationResultAll;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_session_evaluations,
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

        if (args != null) {
            sessionId = args.getInt(ARG_SESSION_ID, -1);
            sessionTitle = args.getString(ARG_SESSION_TITLE, "");
        }

        initialiserVues(view);
        configurerRecyclerView();
        configurerActions();
        chargerEvaluation();
    }

    private void initialiserVues(@NonNull View view) {
        tvAverage = view.findViewById(
                R.id.tvEvaluationDetailAverage
        );

        tvBestScore = view.findViewById(
                R.id.tvEvaluationDetailBestScore
        );

        tvResultCount = view.findViewById(
                R.id.tvEvaluationDetailResultCount
        );

        tvEmptyMessage = view.findViewById(
                R.id.tvEvaluationDetailEmptyMessage
        );

        searchInput = view.findViewById(
                R.id.etEvaluationDetailSearch
        );

        filterGroup = view.findViewById(
                R.id.toggleEvaluationResultFilters
        );

        resultsRecyclerView = view.findViewById(
                R.id.rvEvaluationDetailResults
        );

        emptyLayout = view.findViewById(
                R.id.layoutEvaluationDetailEmpty
        );
    }

    private void configurerRecyclerView() {
        if (resultsRecyclerView == null) {
            return;
        }

        resultsRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        adapter = new EvaluationStudentAdapter();

        resultsRecyclerView.setAdapter(adapter);
    }

    private void configurerActions() {

        if (searchInput != null) {
            searchInput.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(
                        CharSequence s,
                        int start,
                        int count,
                        int after) {
                }

                @Override
                public void onTextChanged(
                        CharSequence s,
                        int start,
                        int before,
                        int count) {

                    searchQuery = s == null
                            ? ""
                            : s.toString().trim();

                    afficherResultats();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        if (filterGroup != null) {
            filterGroup.addOnButtonCheckedListener(
                    (group, checkedId, isChecked) -> {

                        if (isChecked) {
                            activeFilter = checkedId;
                            afficherResultats();
                        }
                    }
            );
        }
    }

    private void chargerEvaluation() {
        teams.clear();
        allItems.clear();
        sheet = null;

        EvaluationRepositoryMemory repository =
                EvaluationRepositoryMemory.getInstance();

        // Ne pas créer de feuille lors de la consultation
        // si aucune évaluation n'existe.
        if (sessionId >= 0 && repository.hasEvaluation(sessionId)) {

            sheet = repository.getSheet(sessionId, sessionTitle);

            if (sheet != null && sheet.getTeams() != null) {
                teams.addAll(sheet.getTeams());
            }
        }

        construireListeEtudiants();
        actualiserStatistiques();
        afficherResultats();
    }

    /**
     * Détermine si l'évaluation d'une équipe est renseignée.
     * Le statut est commun aux membres de l'équipe.
     */
    private boolean equipeEvaluee(EvaluationTeamMemory team) {
        if (team == null
                || team.getCriteria() == null
                || team.getCriteria().isEmpty()) {
            return false;
        }

        if (team.isEvaluationTerminee()) {
            return true;
        }

        // Compatibilité avec les feuilles où le statut
        // n'a pas encore été actualisé.
        for (EvaluationCriterion criterion : team.getCriteria()) {
            if (criterion != null && criterion.getScore() > 0) {
                return true;
            }
        }

        return false;
    }

    private double getScoreSur20(EvaluationTeamMemory team) {
        if (team == null || team.getMaxScore() <= 0) {
            return 0;
        }

        return team.getTotalScore() * 20.0 / team.getMaxScore();
    }

    /**
     * Construit une entrée par étudiant, en associant
     * chaque étudiant à son équipe et à la note de celle-ci.
     */
    private void construireListeEtudiants() {
        allItems.clear();

        for (EvaluationTeamMemory team : teams) {
            if (team == null || team.getStudents() == null) {
                continue;
            }

            boolean evaluated = equipeEvaluee(team);
            double score = getScoreSur20(team);

            for (AttendanceRecordMemory student : team.getStudents()) {
                if (student == null) {
                    continue;
                }

                allItems.add(
                        new EvaluationStudentAdapter.StudentEvaluationItem(
                                student,
                                team,
                                evaluated,
                                score
                        )
                );
            }
        }
    }

    private void actualiserStatistiques() {
        int evaluatedCount = 0;
        double sum = 0;
        double best = -1;

        for (EvaluationStudentAdapter.StudentEvaluationItem item : allItems) {
            if (!item.isEvaluated()) {
                continue;
            }

            double score = item.getScoreSur20();

            sum += score;
            best = Math.max(best, score);
            evaluatedCount++;
        }

        if (tvAverage != null) {
            tvAverage.setText(
                    evaluatedCount == 0
                            ? "— / 20"
                            : String.format(
                            Locale.getDefault(),
                            "%.2f / 20",
                            sum / evaluatedCount
                    )
            );
        }

        if (tvBestScore != null) {
            tvBestScore.setText(
                    best < 0
                            ? "— / 20"
                            : String.format(
                            Locale.getDefault(),
                            "%.2f / 20",
                            best
                    )
            );
        }
    }

    private void afficherResultats() {
        if (adapter == null) {
            return;
        }

        List<EvaluationStudentAdapter.StudentEvaluationItem> filtered =
                new ArrayList<>();

        for (EvaluationStudentAdapter.StudentEvaluationItem item : allItems) {

            // Filtre : évaluations renseignées
            if (activeFilter == R.id.btnEvaluationResultEntered
                    && !item.isEvaluated()) {
                continue;
            }

            // Filtre : évaluations non renseignées
            if (activeFilter == R.id.btnEvaluationResultMissing
                    && item.isEvaluated()) {
                continue;
            }

            // Recherche simultanée par nom, matricule ou groupe
            if (!correspondRecherche(item, searchQuery)) {
                continue;
            }

            filtered.add(item);
        }

        adapter.submitList(filtered);

        if (tvResultCount != null) {
            int count = filtered.size();

            tvResultCount.setText(
                    count + (count > 1 ? " étudiants" : " étudiant")
            );
        }

        boolean empty = filtered.isEmpty();

        if (emptyLayout != null) {
            emptyLayout.setVisibility(
                    empty ? View.VISIBLE : View.GONE
            );
        }

        if (resultsRecyclerView != null) {
            resultsRecyclerView.setVisibility(
                    empty ? View.GONE : View.VISIBLE
            );
        }

        if (empty && tvEmptyMessage != null) {
            if (allItems.isEmpty()) {
                tvEmptyMessage.setText(
                        "Aucun étudiant n'est associé à une évaluation "
                                + "pour cette séance."
                );
            } else if (activeFilter == R.id.btnEvaluationResultEntered) {
                tvEmptyMessage.setText(
                        "Aucune évaluation renseignée ne correspond "
                                + "à votre recherche."
                );
            } else if (activeFilter == R.id.btnEvaluationResultMissing) {
                tvEmptyMessage.setText(
                        "Aucune évaluation non renseignée ne correspond "
                                + "à votre recherche."
                );
            } else {
                tvEmptyMessage.setText(
                        "Aucun étudiant ne correspond à votre recherche."
                );
            }
        }
    }

    private boolean correspondRecherche(
            EvaluationStudentAdapter.StudentEvaluationItem item,
            String query) {

        if (query == null || query.trim().isEmpty()) {
            return true;
        }

        AttendanceRecordMemory student = item.getStudent();
        EvaluationTeamMemory team = item.getTeam();

        String normalized = normaliser(query);

        return normaliser(student.getStudentName()).contains(normalized)
                || normaliser(student.getArabicName()).contains(normalized)
                || normaliser(student.getMatricule()).contains(normalized)
                || normaliser(student.getGroupName()).contains(normalized)
                || (team != null
                && normaliser(team.getName()).contains(normalized));
    }

    private String normaliser(String value) {
        return value == null
                ? ""
                : value.toLowerCase(Locale.ROOT).trim();
    }
}
