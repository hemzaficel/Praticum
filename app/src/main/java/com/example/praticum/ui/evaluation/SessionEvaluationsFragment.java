package com.example.praticum.ui.evaluation;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;
import com.example.praticum.data.entity.EvaluationTeam;
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

    private EvaluationViewModel viewModel;

    private TextView tvAverage;
    private TextView tvBestScore;
    private TextView tvResultCount;
    private TextView tvEmptyMessage;

    private TextInputEditText searchInput;
    private MaterialButtonToggleGroup filterGroup;

    private RecyclerView resultsRecyclerView;
    private View emptyLayout;

    private EvaluationStudentAdapter adapter;

    private final List<EvaluationStudentAdapter.StudentEvaluationItem>
            allItems = new ArrayList<>();

    private String searchQuery = "";

    private int activeFilter = R.id.btnEvaluationResultAll;

    public SessionEvaluationsFragment() {
        // Constructeur public requis par Android.
    }

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

        viewModel = new ViewModelProvider(this)
                .get(EvaluationViewModel.class);

        Bundle args = getArguments();

        if (args != null) {
            sessionId = args.getInt(ARG_SESSION_ID, -1);

            sessionTitle = args.getString(
                    ARG_SESSION_TITLE,
                    ""
            );
        }

        initialiserVues(view);
        configurerRecyclerView();
        configurerActions();
        observerDonnees();

        if (sessionId <= 0) {
            afficherMessage("Séance invalide.");
            getParentFragmentManager().popBackStack();
            return;
        }

        viewModel.loadSheet(sessionId);
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
            searchInput.addTextChangedListener(
                    new TextWatcher() {

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
                    }
            );
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

    private void observerDonnees() {
        viewModel.getSheetState().observe(
                getViewLifecycleOwner(),
                state -> {
                    if (state == null || !isAdded()) {
                        return;
                    }

                    construireListeEtudiants(state);
                    actualiserStatistiques();
                    afficherResultats();
                }
        );

        viewModel.getOperationResult().observe(
                getViewLifecycleOwner(),
                result -> {
                    if (result == null) {
                        return;
                    }

                    viewModel.clearOperationResult();

                    if (!result.isSuccess()) {
                        afficherMessage(result.getMessage());

                        if (result.shouldCloseScreen() && isAdded()) {
                            getParentFragmentManager().popBackStack();
                        }
                    }
                }
        );
    }

    private void construireListeEtudiants(
            EvaluationViewModel.SheetState state) {

        allItems.clear();

        for (EvaluationViewModel.EvaluationTeamState teamState
                : state.getTeams()) {

            boolean evaluated = teamState.isEvaluated();

            double scoreSur20 = teamState.getScoreSur20();

            EvaluationTeam team = teamState.getTeam();

            for (EvaluationViewModel.StudentWithGroup student
                    : teamState.getStudents()) {

                allItems.add(
                        new EvaluationStudentAdapter.StudentEvaluationItem(
                                student,
                                team,
                                evaluated,
                                scoreSur20
                        )
                );
            }
        }
    }

    private void actualiserStatistiques() {
        int evaluatedCount = 0;

        double sum = 0;
        double best = -1;

        for (EvaluationStudentAdapter.StudentEvaluationItem item
                : allItems) {

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

        for (EvaluationStudentAdapter.StudentEvaluationItem item
                : allItems) {

            if (activeFilter == R.id.btnEvaluationResultEntered
                    && !item.isEvaluated()) {
                continue;
            }

            if (activeFilter == R.id.btnEvaluationResultMissing
                    && item.isEvaluated()) {
                continue;
            }

            if (!correspondRecherche(item, searchQuery)) {
                continue;
            }

            filtered.add(item);
        }

        adapter.submitList(filtered);

        if (tvResultCount != null) {
            int count = filtered.size();

            tvResultCount.setText(
                    count + (count > 1
                            ? " étudiants"
                            : " étudiant")
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

            } else if (
                    activeFilter == R.id.btnEvaluationResultEntered
            ) {
                tvEmptyMessage.setText(
                        "Aucune évaluation renseignée ne correspond "
                                + "à votre recherche."
                );

            } else if (
                    activeFilter == R.id.btnEvaluationResultMissing
            ) {
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

        EvaluationViewModel.StudentWithGroup entry =
                item.getStudentEntry();

        String firstName = entry.getStudent().getFullName();
        String arabicName = entry.getStudent().getFullNameArabic();
        String matricule = entry.getStudent().getMatricule();
        String groupName = entry.getGroupName();
        String teamName = item.getTeam().getName();

        String search = normaliser(query);

        return normaliser(firstName).contains(search)
                || normaliser(arabicName).contains(search)
                || normaliser(matricule).contains(search)
                || normaliser(groupName).contains(search)
                || normaliser(teamName).contains(search);
    }

    private String normaliser(String value) {
        return value == null
                ? ""
                : value.toLowerCase(Locale.ROOT).trim();
    }

    private void afficherMessage(String message) {
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
        tvAverage = null;
        tvBestScore = null;
        tvResultCount = null;
        tvEmptyMessage = null;

        searchInput = null;
        filterGroup = null;

        resultsRecyclerView = null;
        emptyLayout = null;

        adapter = null;

        super.onDestroyView();
    }
}