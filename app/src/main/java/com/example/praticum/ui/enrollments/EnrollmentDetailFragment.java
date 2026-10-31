package com.example.praticum.ui.enrollments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.example.praticum.core.common.DeletionConfirmationDialog;
import com.example.praticum.data.entity.EnrollmentListItem;
import java.util.Locale;

public class EnrollmentDetailFragment extends Fragment {
    private static final String ARG_ENROLLMENT_ID = "enrollment_id";
    private EnrollmentViewModel viewModel;
    private int enrollmentId;
    private boolean lookupCompleted;
    private TextView initials, name, nameArabic, matricule, year, specialization, level, group, groupOrder, status, date;

    public static EnrollmentDetailFragment newInstance(int enrollmentId) {
        EnrollmentDetailFragment fragment = new EnrollmentDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ENROLLMENT_ID, enrollmentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        return inflater.inflate(R.layout.fragment_enrollment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        enrollmentId = getArguments() == null ? -1 : getArguments().getInt(ARG_ENROLLMENT_ID, -1);
        viewModel = new ViewModelProvider(this).get(EnrollmentViewModel.class);

        CommonHeaderHelper.configurerEnteteSousFenetre(
                view, "Détail de l’inscription", "Parcours académique annuel", requireActivity());

        initials = view.findViewById(R.id.tvEnrollmentDetailInitials);
        name = view.findViewById(R.id.tvEnrollmentDetailName);
        nameArabic = view.findViewById(R.id.tvEnrollmentDetailNameArabic);
        matricule = view.findViewById(R.id.tvEnrollmentDetailMatricule);
        year = view.findViewById(R.id.tvEnrollmentDetailYear);
        specialization = view.findViewById(R.id.tvEnrollmentDetailSpecialization);
        level = view.findViewById(R.id.tvEnrollmentDetailLevel);
        group = view.findViewById(R.id.tvEnrollmentDetailGroup);
        groupOrder = view.findViewById(R.id.tvEnrollmentDetailOrder);
        status = view.findViewById(R.id.tvEnrollmentDetailStatus);
        date = view.findViewById(R.id.tvEnrollmentDetailDate);

        view.findViewById(R.id.btnEditEnrollmentDetail).setOnClickListener(v -> openEdit());
        view.findViewById(R.id.btnDeleteEnrollmentDetail).setOnClickListener(v -> confirmDelete());

        viewModel.getEnrollmentDetails().observe(getViewLifecycleOwner(), item -> {
            if (item == null) {
                if (lookupCompleted) {
                    Toast.makeText(requireContext(), "Inscription introuvable.", Toast.LENGTH_LONG).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
                return;
            }
            lookupCompleted = true;
            render(item);
        });
        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.trim().isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
        lookupCompleted = true;
        viewModel.loadEnrollmentDetails(enrollmentId);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null && enrollmentId > 0) viewModel.loadEnrollmentDetails(enrollmentId);
    }

    private void render(EnrollmentListItem item) {
        String fullName = ((item.firstName == null ? "" : item.firstName) + " " +
                (item.lastName == null ? "" : item.lastName)).trim();
        name.setText(fullName);
        initials.setText(getInitials(fullName));
        String arabicName = ((item.firstNameArabic == null ? "" : item.firstNameArabic) + " " +
                (item.lastNameArabic == null ? "" : item.lastNameArabic)).trim();
        nameArabic.setText(arabicName);
        nameArabic.setVisibility(arabicName.isEmpty() ? View.GONE : View.VISIBLE);
        matricule.setText(item.matricule == null || item.matricule.trim().isEmpty()
                ? "Matricule non renseigné" : "Matricule · " + item.matricule);
        year.setText(item.academicYearLabel);
        specialization.setText(item.specializationName);
        level.setText(item.levelName);
        group.setText(item.groupCode);
        groupOrder.setText("N° " + item.groupOrder);
        status.setText(item.status);
        date.setText(item.enrollmentDate == null || item.enrollmentDate.trim().isEmpty()
                ? "Non renseignée" : item.enrollmentDate);
    }

    private String getInitials(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) return "?";
        String value = parts[0].substring(0, 1);
        if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
            value += parts[parts.length - 1].substring(0, 1);
        }
        return value.toUpperCase(Locale.ROOT);
    }

    private void openEdit() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainContainer, EnrollmentFormFragment.newInstance(enrollmentId))
                .addToBackStack("enrollment_edit")
                .commit();
    }

    private void confirmDelete() {
        DeletionConfirmationDialog.show(
                requireContext(),
                "Supprimer l’inscription ?",
                "Seule cette inscription annuelle sera supprimée. "
                        + "Le dossier étudiant sera conservé.",
                () -> viewModel.deleteEnrollment(enrollmentId)
        );
    }
}
