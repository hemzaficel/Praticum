package com.example.praticum.ui.enrollments;

import android.os.Bundle;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.example.praticum.core.common.DeletionConfirmationDialog;
import com.example.praticum.data.entity.AcademicYear;
import com.example.praticum.data.entity.EnrollmentListItem;
import com.example.praticum.data.entity.Level;
import com.example.praticum.data.entity.StudentGroup;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class EnrollmentsFragment extends Fragment {
    private static final String ARG_STUDENT_ID = "student_id";
    private static final String ARG_SELECTION_MODE = "selection_mode";
    private static final String ARG_LEVEL_ID = "selection_level_id";
    private static final String ARG_SPECIALIZATION_ID = "selection_specialization_id";
    public static final String RESULT_KEY = "evaluation_enrollment_selection";
    public static final String RESULT_STUDENT_ID = "selected_student_id";
    public static final String RESULT_STUDENT_LABEL = "selected_student_label";
    private EnrollmentViewModel viewModel;
    private EnrollmentAdapter adapter;
    private RecyclerView recyclerView;
    private TextView count;
    private View empty;
    private AutoCompleteTextView groupFilter;
    private TextView headerSubtitle;
    private TextInputLayout searchLayout;
    private TextInputEditText searchInput;
    private View searchButton;
    private String searchQuery = "";
    private final List<AcademicYear> years = new ArrayList<>();
    private final List<StudentGroup> groups = new ArrayList<>();
    private final List<Level> levels = new ArrayList<>();
    private final List<Integer> groupFilterIds = new ArrayList<>();
    private List<EnrollmentListItem> allRows = new ArrayList<>();
    private Integer activeAcademicYearId;
    private String activeAcademicYearLabel;
    private Integer selectedGroupId;
    private String studentIdFilter;
    private boolean selectionMode;
    private Integer selectionLevelId;
    private Integer selectionSpecializationId;

    public static EnrollmentsFragment newInstanceForSelection() {
        return newInstanceForSelection(null);
    }

    public static EnrollmentsFragment newInstanceForSelection(Integer levelId) {
        return newInstanceForSelection(levelId, null);
    }

    public static EnrollmentsFragment newInstanceForSelection(Integer levelId, Integer specializationId) {
        EnrollmentsFragment fragment = new EnrollmentsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SELECTION_MODE, true);
        if (levelId != null) args.putInt(ARG_LEVEL_ID, levelId);
        if (specializationId != null) args.putInt(ARG_SPECIALIZATION_ID, specializationId);
        fragment.setArguments(args);
        return fragment;
    }

    public static EnrollmentsFragment newInstanceForStudent(String studentId) {
        EnrollmentsFragment fragment = new EnrollmentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STUDENT_ID, studentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        return inflater.inflate(R.layout.fragment_enrollments, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        viewModel = new ViewModelProvider(this).get(EnrollmentViewModel.class);
        studentIdFilter = getArguments() == null ? null : getArguments().getString(ARG_STUDENT_ID);
        selectionMode = getArguments() != null
                && getArguments().getBoolean(ARG_SELECTION_MODE, false);
        selectionLevelId = getArguments() != null && getArguments().containsKey(ARG_LEVEL_ID)
                ? getArguments().getInt(ARG_LEVEL_ID) : null;
        selectionSpecializationId = getArguments() != null && getArguments().containsKey(ARG_SPECIALIZATION_ID)
                ? getArguments().getInt(ARG_SPECIALIZATION_ID) : null;
        boolean filteredByStudent = studentIdFilter != null && !studentIdFilter.trim().isEmpty();
        CommonHeaderHelper.configurerEntetePrincipal(view,
                selectionMode ? "Choisir une inscription" : filteredByStudent ? "Parcours universitaire" : "Inscriptions",
                selectionMode ? "Sélectionnez l’étudiant inscrit pour cette année" : filteredByStudent ? "Inscriptions annuelles de l’étudiant" : "Suivi des parcours académiques",
                v -> { if (requireActivity() instanceof MainActivity) ((MainActivity) requireActivity()).ouvrirMenuLateral(); });
        searchLayout = view.findViewById(R.id.tilEnrollmentSearch);
        searchInput = view.findViewById(R.id.etEnrollmentSearch);
        searchButton = view.findViewById(R.id.btnCommonHeaderSearch);
        if (searchButton != null) searchButton.setOnClickListener(v -> toggleSearch());
        configureSearch();

        recyclerView = view.findViewById(R.id.rvEnrollments);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EnrollmentAdapter(new EnrollmentAdapter.Listener() {
            @Override public void onDetails(EnrollmentListItem item) {
                if (selectionMode) returnEnrollment(item); else openDetails(item);
            }
            @Override public void onEdit(EnrollmentListItem item) { openEdit(item); }
            @Override public void onDelete(EnrollmentListItem item) { confirmDelete(item); }
        }, selectionMode);
        recyclerView.setAdapter(adapter);
        count = view.findViewById(R.id.tvEnrollmentsCount);
        empty = view.findViewById(R.id.layoutNoEnrollments);
        groupFilter = view.findViewById(R.id.actEnrollmentGroupFilter);
        headerSubtitle = view.findViewById(R.id.tvCommonHeaderSubtitle);
        configureGroupFilter();
        View addEnrollmentButton = view.findViewById(R.id.btnAddEnrollment);
        View importEnrollmentsButton = view.findViewById(R.id.btnImportEnrollments);
        if (selectionMode) importEnrollmentsButton.setVisibility(View.GONE);
        importEnrollmentsButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .beginTransaction().replace(R.id.mainContainer, new EnrollmentImportFragment())
                .addToBackStack("enrollment_import").commit());
        if (selectionMode) addEnrollmentButton.setVisibility(View.GONE);
        addEnrollmentButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainContainer, filteredByStudent
                                ? EnrollmentFormFragment.newInstanceForStudent(studentIdFilter)
                                : new EnrollmentFormFragment()).addToBackStack(null).commit());
        viewModel.getEnrollments().observe(getViewLifecycleOwner(), rows -> {
            allRows = rows == null ? new ArrayList<>() : new ArrayList<>(rows);
            applyFilter();
        });
        viewModel.getYears().observe(getViewLifecycleOwner(), this::updateAcademicYears);
        viewModel.getGroups().observe(getViewLifecycleOwner(), values -> {
            groups.clear();
            if (values != null) groups.addAll(values);
            populateGroupFilter();
        });
        viewModel.getLevels().observe(getViewLifecycleOwner(), values -> {
            levels.clear();
            if (values != null) levels.addAll(values);
            populateGroupFilter();
        });
        viewModel.getError().observe(getViewLifecycleOwner(), text -> {
            if (text != null && !text.trim().isEmpty()) {
                Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show();
            }
        });
        viewModel.getMessage().observe(getViewLifecycleOwner(), text -> {
            if (text != null && !text.trim().isEmpty()) {
                Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.loadData();
    }

    private void returnEnrollment(EnrollmentListItem item) {
        String name = ((item.firstName == null ? "" : item.firstName) + " "
                + (item.lastName == null ? "" : item.lastName)).trim();
        String arabicName = ((item.firstNameArabic == null ? "" : item.firstNameArabic) + " "
                + (item.lastNameArabic == null ? "" : item.lastNameArabic)).trim();
        String label = name;
        if (!arabicName.isEmpty()) label += " · " + arabicName;
        if (item.matricule != null && !item.matricule.trim().isEmpty()) {
            label += " · " + item.matricule;
        }
        Bundle result = new Bundle();
        result.putString(RESULT_STUDENT_ID, item.studentId);
        result.putString(RESULT_STUDENT_LABEL, label);
        getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
        getParentFragmentManager().popBackStack();
    }

    @Override public void onResume() {
        super.onResume();
        if (viewModel != null) viewModel.loadData();
    }

    private void openDetails(EnrollmentListItem item) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainContainer, EnrollmentDetailFragment.newInstance(item.enrollmentId))
                .addToBackStack("enrollment_detail")
                .commit();
    }

    private void openEdit(EnrollmentListItem item) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainContainer, EnrollmentFormFragment.newInstance(item.enrollmentId))
                .addToBackStack("enrollment_edit")
                .commit();
    }

    private void confirmDelete(EnrollmentListItem item) {
        String studentName = ((item.firstName == null ? "" : item.firstName) + " " +
                (item.lastName == null ? "" : item.lastName)).trim();
        DeletionConfirmationDialog.show(
                requireContext(),
                "Supprimer l’inscription ?",
                "L’inscription annuelle de " + studentName
                        + " sera supprimée. Le dossier étudiant sera conservé.",
                () -> viewModel.deleteEnrollment(item.enrollmentId)
        );
    }

    private void configureGroupFilter() {
        groupFilter.setOnItemClickListener((parent, row, position, id) -> {
            if (position <= 0) {
                selectedGroupId = null;
            } else if (position - 1 < groupFilterIds.size()) {
                selectedGroupId = groupFilterIds.get(position - 1);
            }
            applyFilter();
        });
    }

    private void updateAcademicYears(List<AcademicYear> values) {
        years.clear();
        if (values != null) years.addAll(values);

        AcademicYear activeYear = null;
        for (AcademicYear year : years) {
            if (year.isActive()) {
                activeYear = year;
                break;
            }
        }

        Integer newActiveYearId = activeYear == null ? null : activeYear.getId();
        boolean activeYearChanged = activeAcademicYearId == null
                ? newActiveYearId != null
                : !activeAcademicYearId.equals(newActiveYearId);
        activeAcademicYearId = newActiveYearId;
        activeAcademicYearLabel = activeYear == null ? null : activeYear.getLabel();

        if (activeYearChanged) selectedGroupId = null;
        if (headerSubtitle != null) {
            headerSubtitle.setText(activeYear == null
                    ? "Aucune année universitaire active"
                    : "Inscriptions de l’année active : " + activeYear.getLabel());
        }

        populateGroupFilter();
    }

    private void populateGroupFilter() {
        if (groupFilter == null || !isAdded()) return;

        List<StudentGroup> activeYearGroups = new ArrayList<>();
        if (activeAcademicYearId != null) {
            for (StudentGroup group : groups) {
                if (group.getAcademicYearId() == activeAcademicYearId) {
                    activeYearGroups.add(group);
                }
            }
        }
        activeYearGroups.sort(
                Comparator.comparing(this::groupFilterLabel, String.CASE_INSENSITIVE_ORDER)
        );

        List<String> labels = new ArrayList<>();
        labels.add("Tous les groupes");
        groupFilterIds.clear();
        for (StudentGroup group : activeYearGroups) {
            labels.add(groupFilterLabel(group));
            groupFilterIds.add(group.getId());
        }

        if (selectedGroupId != null && !groupFilterIds.contains(selectedGroupId)) {
            selectedGroupId = null;
        }

        groupFilter.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                labels
        ));

        int selectedPosition = selectedGroupId == null
                ? 0
                : groupFilterIds.indexOf(selectedGroupId) + 1;
        groupFilter.setText(labels.get(selectedPosition), false);
        applyFilter();
    }

    private String groupFilterLabel(StudentGroup group) {
        String code = group.getCode() == null || group.getCode().trim().isEmpty()
                ? safe(group.getName())
                : group.getCode();
        String specialization = group.getSubject() == null
                || group.getSubject().trim().isEmpty()
                ? "Spécialité manquante"
                : group.getSubject();
        String levelName = "Niveau manquant";
        if (group.getLevelId() != null) {
            for (Level level : levels) {
                if (level.getId() == group.getLevelId()) {
                    levelName = level.getCode() == null
                            || level.getCode().trim().isEmpty()
                            ? safe(level.getName())
                            : level.getCode() + " — " + safe(level.getName());
                    break;
                }
            }
        }
        return code + " · " + specialization + " · " + levelName;
    }

    private void configureSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s == null ? "" : s.toString().trim();
                applyFilter();
            }

            @Override public void afterTextChanged(Editable s) { }
        });
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    private void toggleSearch() {
        boolean show = searchLayout.getVisibility() != View.VISIBLE;
        searchLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        if (searchButton != null) {
            searchButton.setContentDescription(show ? "Masquer la recherche" : "Afficher la recherche");
        }
        if (show) {
            searchInput.requestFocus();
            searchInput.postDelayed(() -> {
                if (!isAdded()) return;
                InputMethodManager manager = (InputMethodManager)
                        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (manager != null) manager.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
            }, 150);
        } else {
            hideKeyboard();
            searchInput.setText("");
            searchInput.clearFocus();
            searchQuery = "";
            applyFilter();
        }
    }

    private void hideKeyboard() {
        if (!isAdded() || searchInput == null) return;
        InputMethodManager manager = (InputMethodManager)
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) manager.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
    }

    private boolean matchesSearch(EnrollmentListItem row, String query) {
        if (query == null || query.trim().isEmpty()) return true;
        String normalized = query.toLowerCase(Locale.ROOT);
        String searchable = safe(row.firstName) + " " + safe(row.lastName) + " "
                + safe(row.firstNameArabic) + " " + safe(row.lastNameArabic) + " "
                + safe(row.matricule) + " " + safe(row.specializationName) + " "
                + safe(row.levelName) + " " + safe(row.groupCode) + " "
                + row.groupOrder + " " + safe(row.academicYearLabel) + " "
                + safe(row.status);
        return searchable.toLowerCase(Locale.ROOT).contains(normalized);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void applyFilter() {
        List<EnrollmentListItem> visible = new ArrayList<>();
        for (EnrollmentListItem row : allRows) {
            boolean matchesActiveYear = activeAcademicYearId != null
                    && row.academicYearId == activeAcademicYearId;
            boolean matchesGroup = selectedGroupId == null
                    || row.groupId == selectedGroupId;
            boolean matchesStudent = studentIdFilter == null
                    || studentIdFilter.equals(row.studentId);
            boolean matchesLevel = selectionLevelId == null
                    || selectionLevelId == row.levelId;
            boolean matchesSpecialization = selectionSpecializationId == null
                    || selectionSpecializationId == row.specializationId;
            boolean matchesQuery = matchesSearch(row, searchQuery);
            if (matchesActiveYear && matchesGroup && matchesStudent && matchesLevel
                    && matchesSpecialization && matchesQuery) {
                visible.add(row);
            }
        }
        adapter.submitList(visible);
        count.setText(visible.size() + (visible.size() == 1 ? " inscription" : " inscriptions"));
        empty.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(visible.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
