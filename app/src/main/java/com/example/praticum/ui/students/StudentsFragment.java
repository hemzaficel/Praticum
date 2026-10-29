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
import com.example.praticum.data.entity.StudentGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StudentsFragment extends Fragment
        implements FilterSortFragment.OnFilterSortListener {

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
    private final Map<Integer, String> codesGroupes = new HashMap<>();

    private boolean sortAscending = true;
    private String sortBy = "Nom";
    private String filterBy = FILTRE_TOUS;

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
            searchLayout.setVisibility(View.GONE);
        }
    }

    private void initializeHeader() {
        CommonHeaderHelper.configurerEntetePrincipal(
                rootView,
                "Étudiants",
                "Gestion des étudiants",
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

        studentAdapter = new StudentAdapter(this::openStudentDetail);

        rvStudents.setAdapter(studentAdapter);
    }

    private void observeData() {
        // Les groupes sont publiés avant les étudiants.
        viewModel.getGroups().observe(
                getViewLifecycleOwner(),
                groupes -> {
                    codesGroupes.clear();

                    if (groupes != null) {
                        for (StudentGroup groupe : groupes) {
                            codesGroupes.put(
                                    groupe.getId(),
                                    safe(groupe.getCode())
                            );
                        }
                    }

                    filterStudents(currentQuery());
                }
        );

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
            addButton.setOnClickListener(v -> openStudentForm());
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

        // Groupes auxquels appartient au moins un étudiant.
        Set<String> groupes = new LinkedHashSet<>();
        groupes.add(FILTRE_TOUS);

        for (Student student : allStudents) {
            String groupe = groupCode(student).trim();

            if (!groupe.isEmpty()) {
                groupes.add(groupe);
            }
        }

        filterSortContainer.setVisibility(View.VISIBLE);

        FilterSortFragment fragment =
                FilterSortFragment.newInstance(
                        new String[]{
                                "Nom",
                                "Matricule",
                                "Groupe",
                                "Ordre du groupe"
                        },
                        groupes.toArray(new String[0])
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
        filterBy = selectedFilterBy == null
                ? FILTRE_TOUS
                : selectedFilterBy;

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
            String group = groupCode(student);
            String email = safe(student.getEmail());

            boolean matchesSearch =
                    fullName.toLowerCase(Locale.ROOT)
                            .contains(normalizedQuery)
                            || fullNameArabic.contains(trimmedQuery)
                            || matricule.toLowerCase(Locale.ROOT)
                            .contains(normalizedQuery)
                            || group.toLowerCase(Locale.ROOT)
                            .contains(normalizedQuery)
                            || email.toLowerCase(Locale.ROOT)
                            .contains(normalizedQuery);

            boolean matchesGroup =
                    filterBy.equals(FILTRE_TOUS)
                            || group.equalsIgnoreCase(filterBy);

            if (matchesSearch && matchesGroup) {
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

            case "Groupe":
                comparator = Comparator.comparing(
                        this::groupCode,
                        String.CASE_INSENSITIVE_ORDER
                );
                break;

            case "Ordre du groupe":
                comparator = Comparator
                        .comparing(
                                (Student student) -> groupCode(student),
                                String.CASE_INSENSITIVE_ORDER
                        )
                        .thenComparingInt(Student::getGroupOrder);
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

        studentAdapter.remplacerEtudiants(
                filteredStudents,
                codesGroupes
        );

        int count = filteredStudents.size();

        if (studentsCount != null) {
            studentsCount.setText(
                    count + (count > 1
                            ? " étudiants affichés"
                            : " étudiant affiché")
            );
        }
    }

    private String groupCode(Student student) {
        String code = codesGroupes.get(student.getGroupId());
        return code == null ? "" : code;
    }

    private String safe(@Nullable String value) {
        return value == null ? "" : value;
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