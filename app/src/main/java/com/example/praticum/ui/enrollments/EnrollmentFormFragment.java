package com.example.praticum.ui.enrollments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.example.praticum.core.common.FormExitConfirmation;
import com.example.praticum.data.entity.AcademicYear;
import com.example.praticum.data.entity.Enrollment;
import com.example.praticum.data.entity.Level;
import com.example.praticum.data.entity.Student;
import com.example.praticum.data.entity.StudentGroup;
import com.example.praticum.ui.students.StudentsFragment;
import com.example.praticum.ui.groups.GroupsFragment;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentFormFragment extends Fragment {
    private static final String ARG_ENROLLMENT_ID = "enrollment_id";
    private static final String ARG_STUDENT_ID = "student_id";
    private EnrollmentViewModel viewModel;
    private AutoCompleteTextView studentInput, groupInput, statusInput;
    private TextInputLayout studentLayout, groupLayout;
    private View saveButton;
    private String selectedStudentId;
    private int selectedGroupId = -1;
    private int enrollmentId = -1;
    private Enrollment enrollmentForEdit;
    private boolean editLookupCompleted;
    private final List<Student> students = new ArrayList<>();
    private final List<StudentGroup> groups = new ArrayList<>();
    private final List<AcademicYear> years = new ArrayList<>();
    private final List<Level> levels = new ArrayList<>();

    public static EnrollmentFormFragment newInstanceForStudent(String studentId) {
        EnrollmentFormFragment fragment = new EnrollmentFormFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STUDENT_ID, studentId);
        fragment.setArguments(args);
        return fragment;
    }

    public static EnrollmentFormFragment newInstance(int enrollmentId) {
        EnrollmentFormFragment fragment = new EnrollmentFormFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ENROLLMENT_ID, enrollmentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        return inflater.inflate(R.layout.fragment_enrollment_form, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        viewModel = new ViewModelProvider(this).get(EnrollmentViewModel.class);
        enrollmentId = getArguments() == null ? -1 : getArguments().getInt(ARG_ENROLLMENT_ID, -1);
        boolean editing = enrollmentId > 0;
        if (!editing && getArguments() != null) selectedStudentId = getArguments().getString(ARG_STUDENT_ID);
        CommonHeaderHelper.configurerEnteteSousFenetre(view,
                editing ? "Modifier l’inscription" : "Nouvelle inscription",
                "Affecter un étudiant pour une année académique", requireActivity());

        studentInput = view.findViewById(R.id.actEnrollmentStudent);
        groupInput = view.findViewById(R.id.actEnrollmentGroup);
        statusInput = view.findViewById(R.id.actEnrollmentStatus);
        studentLayout = view.findViewById(R.id.tilEnrollmentStudent);
        groupLayout = view.findViewById(R.id.tilEnrollmentGroup);
        saveButton = view.findViewById(R.id.btnSaveEnrollment);
        if (editing) ((com.google.android.material.button.MaterialButton) saveButton).setText("Enregistrer les modifications");

        view.findViewById(R.id.btnCancelEnrollment).setOnClickListener(v ->
                FormExitConfirmation.show(this,
                        () -> requireActivity().getSupportFragmentManager().popBackStack()));
        view.findViewById(R.id.btnChooseEnrollmentStudent).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainContainer, StudentsFragment.newInstanceForSelection())
                        .addToBackStack("enrollment_choose_student")
                        .commit());
        view.findViewById(R.id.btnChooseEnrollmentGroup).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainContainer, GroupsFragment.newInstanceForSelection())
                        .addToBackStack("enrollment_choose_group")
                        .commit());

        statusInput.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line,
                new String[]{Enrollment.STATUS_IN_PROGRESS, Enrollment.STATUS_VALIDATED, Enrollment.STATUS_REPEAT, Enrollment.STATUS_WITHDRAWN}));
        statusInput.setText(Enrollment.STATUS_IN_PROGRESS, false);
        statusInput.setOnClickListener(v -> statusInput.showDropDown());
        saveButton.setOnClickListener(v -> save());

        getParentFragmentManager().setFragmentResultListener(
                GroupsFragment.RESULT_KEY,
                getViewLifecycleOwner(),
                (key, result) -> {
                    selectedGroupId = result.getInt(
                            GroupsFragment.RESULT_GROUP_ID,
                            -1
                    );
                    String selectedLabel = result.getString(
                            GroupsFragment.RESULT_GROUP_LABEL,
                            ""
                    );
                    StudentGroup selectedGroup = findGroup(selectedGroupId);
                    groupInput.setText(
                            selectedGroup == null
                                    ? selectedLabel
                                    : groupLabel(selectedGroup),
                            false
                    );
                    groupLayout.setError(null);
                }
        );

        getParentFragmentManager().setFragmentResultListener(StudentsFragment.RESULT_KEY, getViewLifecycleOwner(), (key, result) -> {
            selectedStudentId = result.getString(StudentsFragment.RESULT_STUDENT_ID);
            String label = result.getString(StudentsFragment.RESULT_STUDENT_LABEL, "");
            studentInput.setText(label, false);
            studentLayout.setError(null);
        });

        viewModel.getStudents().observe(getViewLifecycleOwner(), values -> {
            students.clear();
            if (values != null) students.addAll(values);
            applyEnrollmentForEdit();
        });
        viewModel.getGroups().observe(getViewLifecycleOwner(), values -> {
            groups.clear();
            if (values != null) groups.addAll(values);
            refreshSelectedGroupLabel();
            applyEnrollmentForEdit();
        });
        viewModel.getYears().observe(getViewLifecycleOwner(), values -> {
            years.clear();
            if (values != null) years.addAll(values);
            refreshSelectedGroupLabel();
        });
        viewModel.getLevels().observe(getViewLifecycleOwner(), values -> {
            levels.clear();
            if (values != null) levels.addAll(values);
            refreshSelectedGroupLabel();
        });
        viewModel.getEnrollmentForEdit().observe(getViewLifecycleOwner(), enrollment -> {
            if (enrollment == null) {
                if (editing && editLookupCompleted) {
                    Toast.makeText(requireContext(), "Inscription introuvable.", Toast.LENGTH_LONG).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
                return;
            }
            editLookupCompleted = true;
            enrollmentForEdit = enrollment;
            applyEnrollmentForEdit();
        });
        viewModel.getError().observe(getViewLifecycleOwner(), text -> {
            if (text != null && !text.trim().isEmpty()) Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show();
        });
        viewModel.getMessage().observe(getViewLifecycleOwner(), text -> {
            if (text != null && !text.trim().isEmpty()) {
                Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
        viewModel.isSaving().observe(getViewLifecycleOwner(), saving -> saveButton.setEnabled(!Boolean.TRUE.equals(saving)));
        viewModel.loadData();
        if (editing) {
            editLookupCompleted = true;
            viewModel.loadEnrollmentForEdit(enrollmentId);
        }
    }

    private void refreshSelectedGroupLabel() {
        StudentGroup group = findGroup(selectedGroupId);
        if (group != null) {
            groupInput.setText(groupLabel(group), false);
        }
    }

    @Nullable
    private StudentGroup findGroup(int groupId) {
        for (StudentGroup group : groups) {
            if (group.getId() == groupId) {
                return group;
            }
        }
        return null;
    }

    private String groupLabel(StudentGroup group) {
        String year = "Année inconnue";
        for (AcademicYear academicYear : years) {
            if (academicYear.getId() == group.getAcademicYearId()) {
                year = academicYear.getLabel();
                break;
            }
        }
        String code = group.getCode() == null
                ? group.getName()
                : group.getCode();
        String subject = group.getSubject() == null
                ? "Spécialité manquante"
                : group.getSubject();
        String levelLabel = "Niveau manquant";
        if (group.getLevelId() != null) {
            for (Level level : levels) {
                if (level.getId() == group.getLevelId()) {
                    levelLabel = level.getCode() == null
                            || level.getCode().trim().isEmpty()
                            ? level.getName()
                            : level.getCode() + " — " + level.getName();
                    break;
                }
            }
        }
        return code + " · " + subject + " · " + levelLabel + " · " + year;
    }

    private void applyEnrollmentForEdit() {
        if (enrollmentForEdit == null) {
            if (selectedStudentId != null) {
                for (Student student : students) {
                    if (student.getStudentId().equals(selectedStudentId)) {
                        studentInput.setText(studentLabel(student), false);
                        break;
                    }
                }
            }
            return;
        }
        selectedStudentId = enrollmentForEdit.getStudentId();
        selectedGroupId = enrollmentForEdit.getGroupId();
        statusInput.setText(enrollmentForEdit.getStatus(), false);
        for (Student student : students) {
            if (student.getStudentId().equals(selectedStudentId)) {
                studentInput.setText(studentLabel(student), false);
                break;
            }
        }
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getId() == selectedGroupId) {
                groupInput.setText(groupLabel(groups.get(i)), false);
                break;
            }
        }
    }

    private String studentLabel(Student student) {
        String matricule = student.getMatricule();
        return student.getFullName() + (matricule == null || matricule.trim().isEmpty() ? "" : " · " + matricule);
    }

    private void save() {
        studentLayout.setError(null);
        groupLayout.setError(null);
        if (selectedStudentId == null) { studentLayout.setError("Sélectionnez un étudiant."); return; }
        if (selectedGroupId <= 0) { groupLayout.setError("Sélectionnez un groupe."); return; }
        String status = statusInput.getText().toString().trim();
        if (enrollmentId > 0) viewModel.updateEnrollment(enrollmentId, selectedStudentId, selectedGroupId, status);
        else viewModel.createEnrollment(selectedStudentId, selectedGroupId, status);
    }
}
