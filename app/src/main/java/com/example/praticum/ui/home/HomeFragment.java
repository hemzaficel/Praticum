
package com.example.praticum.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.praticum.MainActivity;
import com.example.praticum.data.dao.DashboardStatsDao;
import com.example.praticum.data.entity.AcademicYear;
import com.example.praticum.data.database.AppDatabase;
import com.example.praticum.data.database.DatabaseProvider;
import com.example.praticum.data.database.DatabaseSeeder;
import com.example.praticum.R;
import com.example.praticum.ui.academicyears.AcademicYearsFragment;
import com.example.praticum.ui.groups.GroupsFragment;
import com.example.praticum.ui.levels.LevelsFragment;
import com.example.praticum.ui.modules.ModulesFragment;
import com.example.praticum.ui.sessions.SessionsFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextInputLayout tilHomeSearch;
    private TextInputEditText etHomeSearch;

    private final List<SearchableItem> searchableItems = new ArrayList<>();

    private static class SearchableItem {
        final View view;
        final String searchableText;

        SearchableItem(View view, String searchableText) {
            this.view = view;
            this.searchableText = searchableText.toLowerCase(Locale.ROOT);
        }
    }

    public HomeFragment() {
        // Constructeur requis
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_home,
                container,
                false
        );

        initializeHeader(view);
        initializeSearch(view);
        initializeConfigurationCards(view);

        return view;
    }

    private void initializeHeader(View root) {
        View header = root.findViewById(R.id.includeHomeHeader);

        if (header == null) {
            return;
        }

        TextView title = header.findViewById(R.id.tvCommonHeaderTitle);
        TextView subtitle = header.findViewById(R.id.tvCommonHeaderSubtitle);
        ImageButton menuButton = header.findViewById(R.id.btnCommonMenu);
        ImageButton searchButton = header.findViewById(
                R.id.btnCommonHeaderSearch
        );

        if (title != null) {
            title.setText("Bonjour, Enseignant");
        }

        if (subtitle != null) {
            subtitle.setText("Voici votre espace de travail");
        }

        if (menuButton != null) {
            menuButton.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).ouvrirMenuLateral();
                }
            });
        }

        if (searchButton != null) {
            searchButton.setOnClickListener(v -> toggleSearch());
        }
    }

    private void initializeSearch(View root) {
        tilHomeSearch = root.findViewById(R.id.tilHomeSearch);
        etHomeSearch = root.findViewById(R.id.etHomeSearch);

        if (etHomeSearch == null) {
            return;
        }

        // Éléments que la recherche peut filtrer.
        addSearchableItem(
                root,
                R.id.cardDashboardAcademicYears,
                "Années universitaires année académique"
        );

        addSearchableItem(
                root,
                R.id.cardDashboardLevels,
                "Niveaux d'étude niveaux L1 L2 L3"
        );

        addSearchableItem(
                root,
                R.id.cardDashboardModules,
                "Modules matières enseignement"
        );

        addSearchableItem(
                root,
                R.id.cardDashboardGroups,
                "Groupes et étudiants membres"
        );

        addSearchableItem(
                root,
                R.id.cardDashboardSession,
                "Séances de TP planification planning"
        );

        addSearchableItem(
                root,
                R.id.cardDashboardEvaluations,
                "Évaluations notes étudiants"
        );

        addSearchableItem(
                root,
                R.id.cardDashboardRecentActivity,
                "Activité récente groupe créé séance planifiée"
        );

        etHomeSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                filterItems(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        etHomeSearch.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    private void addSearchableItem(
            View root,
            int viewId,
            String searchableText) {

        View item = root.findViewById(viewId);

        if (item != null) {
            searchableItems.add(new SearchableItem(item, searchableText));
        }
    }

    private void toggleSearch() {
        if (tilHomeSearch == null) {
            return;
        }

        if (tilHomeSearch.getVisibility() == View.VISIBLE) {
            tilHomeSearch.setVisibility(View.GONE);

            if (etHomeSearch != null) {
                etHomeSearch.setText("");
            }

            filterItems("");
            hideKeyboard();

        } else {
            tilHomeSearch.setVisibility(View.VISIBLE);

            if (etHomeSearch != null) {
                etHomeSearch.requestFocus();
            }
        }
    }

    private void filterItems(String query) {
        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);

        for (SearchableItem item : searchableItems) {
            boolean matches = normalizedQuery.isEmpty()
                    || item.searchableText.contains(normalizedQuery);

            item.view.setVisibility(matches ? View.VISIBLE : View.GONE);
        }
    }

    private void hideKeyboard() {
        if (getContext() == null || etHomeSearch == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager)
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(etHomeSearch.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroyView() {
        searchableItems.clear();
        tilHomeSearch = null;
        etHomeSearch = null;
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        View root = getView();
        if (root != null) {
            loadDashboardStatistics(root);
        }
    }

    private void loadDashboardStatistics(@NonNull View root) {
        Context appContext = requireContext().getApplicationContext();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            AcademicYear activeYear = null;
            int studentsCount = 0;
            int groupsCount = 0;
            int enrollmentsCount = 0;
            int evaluationsCount = 0;

            try {
                AppDatabase database = DatabaseProvider.getDatabase(appContext);
                activeYear = database.academicYearDao().getActive();
                DashboardStatsDao statsDao = database.dashboardStatsDao();
                studentsCount = statsDao.countStudents();

                if (activeYear != null) {
                    int yearId = activeYear.getId();
                    groupsCount = statsDao.countGroupsForAcademicYear(yearId);
                    enrollmentsCount = statsDao.countEnrollmentsForAcademicYear(yearId);
                    evaluationsCount = statsDao
                            .countEvaluationsPerformedForAcademicYear(yearId);
                }
            } catch (Exception exception) {
                android.util.Log.e("HomeFragment", "Chargement des statistiques impossible", exception);
            }

            final AcademicYear loadedYear = activeYear;
            final int loadedStudents = studentsCount;
            final int loadedGroups = groupsCount;
            final int loadedEnrollments = enrollmentsCount;
            final int loadedEvaluations = evaluationsCount;

            mainHandler.post(() -> {
                if (!isAdded() || getView() != root) {
                    return;
                }

                TextView yearView = root.findViewById(R.id.tvDashboardAcademicYear);
                TextView groupsView = root.findViewById(R.id.tvDashboardGroups);
                TextView studentsView = root.findViewById(R.id.tvDashboardStudents);
                TextView enrollmentsView = root.findViewById(R.id.tvDashboardSessions);
                TextView evaluationsView = root.findViewById(R.id.tvDashboardPendingAttendance);

                yearView.setText(loadedYear == null
                        ? "Aucune année active"
                        : loadedYear.getLabel());
                groupsView.setText(String.valueOf(loadedGroups));
                studentsView.setText(String.valueOf(loadedStudents));
                enrollmentsView.setText(String.valueOf(loadedEnrollments));
                evaluationsView.setText(String.valueOf(loadedEvaluations));
            });
        }, "dashboard-statistics").start();
    }

    private void initializeConfigurationCards(View root) {
        root.findViewById(R.id.cardDashboardAcademicYears)
                .setOnClickListener(v ->
                        ouvrirEcran(new AcademicYearsFragment()));

        root.findViewById(R.id.cardDashboardLevels)
                .setOnClickListener(v ->
                        ouvrirEcran(new LevelsFragment()));

        root.findViewById(R.id.cardDashboardModules)
                .setOnClickListener(v ->
                        ouvrirEcran(new ModulesFragment()));

        root.findViewById(R.id.cardDashboardGroups)
                .setOnClickListener(v ->
                        ouvrirEcran(new GroupsFragment()));

        root.findViewById(R.id.cardDashboardSession)
                .setOnClickListener(v ->
                        ouvrirEcran(new SessionsFragment()));

        View seedButton = root.findViewById(R.id.btnSeedTestConfiguration);
        if (seedButton != null) seedButton.setOnClickListener(this::seedTestConfiguration);
    }


    private void seedTestConfiguration(View button) {
        button.setEnabled(false);
        Context appContext = requireContext().getApplicationContext();
        Handler mainHandler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            try {
                AppDatabase database = DatabaseProvider.getDatabase(appContext);
                int insertedCount = DatabaseSeeder.seed(database);
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    button.setEnabled(true);
                    String message = insertedCount == 0
                            ? "La configuration de test est déjà présente."
                            : insertedCount + " élément(s) de configuration ajoutés.";
                    Toast.makeText(appContext, message, Toast.LENGTH_LONG).show();
                    View currentRoot = getView();
                    if (currentRoot != null) {
                        loadDashboardStatistics(currentRoot);
                    }
                });
            } catch (Exception exception) {
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    button.setEnabled(true);
                    Toast.makeText(appContext,
                            "Échec de l'insertion : " + exception.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }, "database-seeder").start();
    }
    private void ouvrirEcran(androidx.fragment.app.Fragment fragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).ouvrirEcranSecondaire(fragment);
        }
    }
}

