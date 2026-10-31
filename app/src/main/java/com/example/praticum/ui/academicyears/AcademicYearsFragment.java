package com.example.praticum.ui.academicyears;

import android.os.Bundle;
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

import com.example.praticum.MainActivity;
import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.example.praticum.core.common.DeletionConfirmationDialog;
import com.example.praticum.data.entity.AcademicYear;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class AcademicYearsFragment extends Fragment {

    private static final int FILTER_ALL = 0;
    private static final int FILTER_OPEN = 1;
    private static final int FILTER_CLOSED = 2;

    private int filter = FILTER_ALL;

    private AcademicYearViewModel viewModel;

    private TextView tvActive;
    private TextView tvActiveDescription;
    private TextView tvCount;

    private View emptyCard;
    private RecyclerView rvAcademicYears;
    private ChipGroup chipGroup;

    private AcademicYearAdapter academicYearAdapter;

    private List<AcademicYear> allYears = new ArrayList<>();
    private AcademicYear activeYear;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_academic_years,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        CommonHeaderHelper.configurerEnteteSousFenetre(
                view,
                "Paramètres",
                "Personnalisez votre application",
                requireActivity()
        );

        tvActive = view.findViewById(
                R.id.tvManagedActiveAcademicYear);

        tvActiveDescription = view.findViewById(
                R.id.tvManagedActiveAcademicYearDescription);

        tvCount = view.findViewById(
                R.id.tvManagedAcademicYearsCount);

        emptyCard = view.findViewById(
                R.id.cardNoAcademicYears);

        chipGroup = view.findViewById(
                R.id.chipGroupAcademicYearFilters);

        rvAcademicYears = view.findViewById(
                R.id.rvManagedAcademicYears);

        rvAcademicYears.setLayoutManager(
                new LinearLayoutManager(requireContext()));

        academicYearAdapter = new AcademicYearAdapter(
                new AcademicYearAdapter.OnAcademicYearActionListener() {

                    @Override
                    public void onSetActive(AcademicYear year) {
                        confirmerActivation(year);
                    }

                    @Override
                    public void onEdit(AcademicYear year) {
                        ouvrirFormulaire(year.getId());
                    }

                    @Override
                    public void onDelete(AcademicYear year) {
                        confirmerSuppression(year);
                    }
                });

        rvAcademicYears.setAdapter(academicYearAdapter);

        view.findViewById(R.id.btnAddAcademicYear)
                .setOnClickListener(v -> ouvrirFormulaire(-1));

        view.findViewById(R.id.btnCreateFirstAcademicYear)
                .setOnClickListener(v -> ouvrirFormulaire(-1));

        chipGroup.setOnCheckedStateChangeListener(
                (group, checkedIds) -> {

                    if (checkedIds.isEmpty()) {
                        return;
                    }

                    int checkedId = checkedIds.get(0);

                    if (checkedId == R.id.chipOpenAcademicYears) {
                        filter = FILTER_OPEN;
                    } else if (checkedId == R.id.chipClosedAcademicYears) {
                        filter = FILTER_CLOSED;
                    } else {
                        filter = FILTER_ALL;
                    }

                    actualiserAffichage();
                });

        viewModel = new ViewModelProvider(this)
                .get(AcademicYearViewModel.class);

        observerLesDonnees();
    }

    private void observerLesDonnees() {

        viewModel.getAcademicYears().observe(
                getViewLifecycleOwner(),
                years -> {
                    allYears = years != null
                            ? new ArrayList<>(years)
                            : new ArrayList<>();

                    actualiserAffichage();
                });

        viewModel.getActiveAcademicYear().observe(
                getViewLifecycleOwner(),
                year -> {
                    activeYear = year;
                    afficherAnneeActive();
                });

        viewModel.getOperationMessage().observe(
                getViewLifecycleOwner(),
                message -> {
                    if (message != null && !message.trim().isEmpty()) {
                        Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void ouvrirFormulaire(int id) {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).ouvrirEcranSecondaire(
                    AcademicYearFormFragment.newInstance(id)
            );
        }
    }

    private void afficherAnneeActive() {

        if (tvActive == null || tvActiveDescription == null) {
            return;
        }

        if (activeYear == null) {
            tvActive.setText("Aucune année active");
            tvActiveDescription.setText(
                    "Créez une année universitaire pour commencer."
            );
            return;
        }

        tvActive.setText(activeYear.getLabel());

        tvActiveDescription.setText(
                activeYear.isActive()
                        ? "Cette année est définie comme contexte de travail."
                        : "Aucune année universitaire active."
        );
    }

    private void actualiserAffichage() {

        if (!isAdded()
                || tvCount == null
                || emptyCard == null
                || rvAcademicYears == null
                || academicYearAdapter == null) {
            return;
        }

        afficherAnneeActive();

        int total = allYears.size();

        tvCount.setText(
                total + (total > 1
                        ? " années universitaires"
                        : " année universitaire")
        );

        List<AcademicYear> filteredYears = new ArrayList<>();

        for (AcademicYear year : allYears) {

            if (filter == FILTER_OPEN && !year.isActive()) {
                continue;
            }

            if (filter == FILTER_CLOSED && year.isActive()) {
                continue;
            }

            filteredYears.add(year);
        }

        academicYearAdapter.submitList(filteredYears);

        boolean empty = filteredYears.isEmpty();

        emptyCard.setVisibility(
                empty ? View.VISIBLE : View.GONE);

        rvAcademicYears.setVisibility(
                empty ? View.GONE : View.VISIBLE);
    }

    private void confirmerActivation(AcademicYear year) {

        if (year == null) {
            return;
        }

        if (year.isActive()) {
            Toast.makeText(
                    requireContext(),
                    "Cette année universitaire est déjà active.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Définir l'année active")
                .setMessage(
                        "Définir " + year.getLabel()
                                + " comme année universitaire active ?"
                )
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Confirmer", (dialog, which) ->
                        viewModel.activateAcademicYear(year.getId()))
                .show();
    }

    private void confirmerSuppression(AcademicYear year) {

        if (year == null) {
            return;
        }

        DeletionConfirmationDialog.show(
                requireContext(),
                "Supprimer l'année universitaire ?",
                "L'année universitaire " + year.getLabel() + " sera supprimée.",
                () -> viewModel.deleteAcademicYear(year.getId())
        );
    }

    @Override
    public void onResume() {
        super.onResume();

        if (viewModel != null) {
            viewModel.loadAcademicYears();
        }
    }
}