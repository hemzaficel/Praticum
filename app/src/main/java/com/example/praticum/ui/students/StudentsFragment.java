package com.example.praticum.ui.students;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.MainActivity;
import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.example.praticum.core.common.FilterSortFragment;
import com.example.praticum.data.entity.Student;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class StudentsFragment extends Fragment
        implements FilterSortFragment.OnFilterSortListener {

    public static final String RESULT_KEY = "enrollment_student_result";
    public static final String RESULT_STUDENT_ID = "student_id";
    public static final String RESULT_STUDENT_LABEL = "student_label";
    private static final String ARG_SELECTION_MODE = "selection_mode";

    public static StudentsFragment newInstanceForSelection() {
        StudentsFragment fragment = new StudentsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SELECTION_MODE, true);
        fragment.setArguments(args);
        return fragment;
    }

    private boolean isSelectionMode() {
        return getArguments() != null && getArguments().getBoolean(ARG_SELECTION_MODE, false);
    }

    private static final String FILTRE_TOUS = "Tous";

    private View rootView;

    private TextInputLayout searchLayout;
    private TextInputEditText searchEditText;
    private RecyclerView rvStudents;
    private TextView studentsCount;
    private FrameLayout filterSortContainer;

    private StudentAdapter studentAdapter;
    private StudentViewModel viewModel;

    private List<Student> allStudents = new ArrayList<>();
    private final List<Student> filteredStudents = new ArrayList<>();

    private boolean sortAscending = true;
    private String sortBy = "Nom";

    public StudentsFragment() {
        // Constructeur obligatoire.
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_students,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        rootView = view;

        viewModel = new ViewModelProvider(this)
                .get(StudentViewModel.class);

        initializeViews();
        initializeHeader();
        initializeRecyclerView();
        configureSearch();
        configureAddButton();
        configureSortButton();
        observeData();
    }

    private void initializeViews() {
        searchLayout = rootView.findViewById(R.id.tilStudentsSearch);
        searchEditText = rootView.findViewById(R.id.etStudentsSearch);
        rvStudents = rootView.findViewById(R.id.rvStudents);
        studentsCount = rootView.findViewById(R.id.tvStudentsCount);
        filterSortContainer =
                rootView.findViewById(R.id.filterSortContainer);

        if (searchLayout != null) {
            searchLayout.setVisibility(isSelectionMode() ? View.VISIBLE : View.GONE);
        }
    }

    private void initializeHeader() {
        CommonHeaderHelper.configurerEntetePrincipal(
                rootView,
                isSelectionMode() ? "Choisir un étudiant" : "Étudiants",
                isSelectionMode() ? "Rechercher puis sélectionner un étudiant" : "Gestion des étudiants",
                v -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) requireActivity())
                                .ouvrirMenuLateral();
                    }
                }
        );

        View searchButton =
                rootView.findViewById(R.id.btnCommonHeaderSearch);

        if (searchButton != null) {
            searchButton.setOnClickListener(v -> toggleSearch());
        }
    }

    private void initializeRecyclerView() {
        rvStudents.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        studentAdapter = new StudentAdapter(this::onStudentClicked);

        rvStudents.setAdapter(studentAdapter);
    }

    private void observeData() {
        viewModel.getStudents().observe(
                getViewLifecycleOwner(),
                etudiants -> {
                    allStudents = etudiants == null
                            ? new ArrayList<>()
                            : new ArrayList<>(etudiants);

                    filterStudents(currentQuery());
                }
        );

        viewModel.getError().observe(
                getViewLifecycleOwner(),
                erreur -> {
                    if (erreur != null && !erreur.trim().isEmpty()) {
                        showMessage(erreur);
                    }
                }
        );
    }

    private void toggleSearch() {
        if (searchLayout == null || searchEditText == null) {
            return;
        }

        boolean show = searchLayout.getVisibility() != View.VISIBLE;

        searchLayout.setVisibility(show ? View.VISIBLE : View.GONE);

        if (show) {
            searchEditText.requestFocus();

            searchEditText.postDelayed(() -> {
                if (!isAdded()) {
                    return;
                }

                InputMethodManager imm =
                        (InputMethodManager) requireContext()
                                .getSystemService(
                                        Context.INPUT_METHOD_SERVICE
                                );

                if (imm != null) {
                    imm.showSoftInput(
                            searchEditText,
                            InputMethodManager.SHOW_IMPLICIT
                    );
                }
            }, 150);

        } else {
            hideKeyboard();
            searchEditText.setText("");
            searchEditText.clearFocus();
            filterStudents("");
        }
    }

    private void configureSearch() {
        if (searchEditText == null) {
            return;
        }

        searchEditText.addTextChangedListener(new TextWatcher() {
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

                filterStudents(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        searchEditText.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        hideKeyboard();
                        return true;
                    }
                    return false;
                }
        );
    }

    private void configureAddButton() {
        View addButton = rootView.findViewById(R.id.btnAddStudent);
        if (addButton != null) {
            if (isSelectionMode()) {
                addButton.setVisibility(View.GONE);
            } else {
                addButton.setOnClickListener(v -> openStudentForm());
            }
        }
    }

    private void openStudentForm() {
        hideKeyboard();

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.mainContainer,
                        StudentFormFragment.newInstance(null)
                )
                .addToBackStack("student_form")
                .commit();
    }

    private void configureSortButton() {
        View sortButton = rootView.findViewById(R.id.btnSortStudents);

        if (sortButton != null) {
            if (isSelectionMode()) {
                sortButton.setVisibility(View.GONE);
                return;
            }
            sortButton.setOnClickListener(v -> {
                if (filterSortContainer == null) {
                    return;
                }

                if (filterSortContainer.getVisibility() == View.VISIBLE) {
                    fermerPanneauTri();
                } else {
                    ouvrirPanneauTri();
                }
            });
        }
    }

    private void ouvrirPanneauTri() {
        if (filterSortContainer == null) {
            return;
        }

        filterSortContainer.setVisibility(View.VISIBLE);

        FilterSortFragment fragment =
                FilterSortFragment.newInstance(
                        new String[]{"Nom", "Matricule"},
                        new String[]{FILTRE_TOUS}
                );
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.filterSortContainer, fragment)
                .commit();
    }

    private void fermerPanneauTri() {
        if (filterSortContainer == null) {
            return;
        }

        filterSortContainer.setVisibility(View.GONE);

        Fragment fragment = getChildFragmentManager()
                .findFragmentById(R.id.filterSortContainer);

        if (fragment != null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commit();
        }
    }

    @Override
    public void onApplyFilterSort(
            String selectedSortBy,
            boolean ascending,
            String selectedFilterBy) {

        sortBy = selectedSortBy == null ? "Nom" : selectedSortBy;
        sortAscending = ascending;

        fermerPanneauTri();
        filterStudents(currentQuery());
    }

    private String currentQuery() {
        return searchEditText == null
                || searchEditText.getText() == null
                ? ""
                : searchEditText.getText().toString();
    }

    private void filterStudents(String query) {
        if (studentAdapter == null || !isAdded()) {
            return;
        }

        String trimmedQuery = query == null ? "" : query.trim();
        String normalizedQuery = trimmedQuery.toLowerCase(Locale.ROOT);

        filteredStudents.clear();

        for (Student student : allStudents) {
            String fullName = safe(student.getFullName());
            String fullNameArabic = safe(student.getFullNameArabic());
            String matricule = safe(student.getMatricule());
            String email = safe(student.getEmail());

            boolean matchesSearch =
                    fullName.toLowerCase(Locale.ROOT)
                            .contains(normalizedQuery)
                            || fullNameArabic.contains(trimmedQuery)
                            || matricule.toLowerCase(Locale.ROOT)
                            .contains(normalizedQuery)
                            || email.toLowerCase(Locale.ROOT)
                            .contains(normalizedQuery);

            if (matchesSearch) {
                filteredStudents.add(student);
            }
        }

        Comparator<Student> comparator;

        switch (sortBy) {
            case "Matricule":
                comparator = Comparator.comparing(
                        student -> safe(student.getMatricule()),
                        String.CASE_INSENSITIVE_ORDER
                );
                break;

            case "Nom":
            default:
                comparator = Comparator.comparing(
                        student -> safe(student.getFullName()),
                        String.CASE_INSENSITIVE_ORDER
                );
                break;
        }

        if (!sortAscending) {
            comparator = comparator.reversed();
        }

        Collections.sort(filteredStudents, comparator);

        studentAdapter.remplacerEtudiants(filteredStudents);

        int count = filteredStudents.size();

        if (studentsCount != null) {
            studentsCount.setText(
                    count + (count > 1
                            ? " étudiants affichés"
                            : " étudiant affiché")
            );
        }
    }

    private String safe(@Nullable String value) {
        return value == null ? "" : value;
    }

    private void onStudentClicked(Student student) {
        if (!isSelectionMode()) {
            openStudentDetail(student);
            return;
        }
        Bundle result = new Bundle();
        result.putString(RESULT_STUDENT_ID, student.getStudentId());
        String matricule = student.getMatricule();
        result.putString(RESULT_STUDENT_LABEL,
                student.getFullName() + (matricule == null || matricule.trim().isEmpty() ? "" : " · " + matricule));
        getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
        getParentFragmentManager().popBackStack();
    }

    private void openStudentDetail(Student student) {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.mainContainer,
                        StudentDetailFragment.newInstance(
                                student.getStudentId()
                        )
                )
                .addToBackStack("student_detail")
                .commit();
    }

    private void hideKeyboard() {
        if (!isAdded() || searchEditText == null) {
            return;
        }

        InputMethodManager imm =
                (InputMethodManager) requireContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(
                    searchEditText.getWindowToken(),
                    0
            );
        }
    }

    private void showMessage(String message) {
        if (!isAdded()) {
            return;
        }

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (viewModel != null) {
            viewModel.loadStudents();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        rootView = null;
        searchLayout = null;
        searchEditText = null;
        rvStudents = null;
        studentsCount = null;
        filterSortContainer = null;
        studentAdapter = null;
    }
}


