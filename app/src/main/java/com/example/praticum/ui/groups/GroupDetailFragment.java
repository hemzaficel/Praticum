package com.example.praticum.ui.groups;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.example.praticum.core.common.DeletionConfirmationDialog;
import com.example.praticum.data.entity.AcademicYear;
import com.example.praticum.data.entity.StudentGroup;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class GroupDetailFragment extends Fragment {

    private static final String ARG_GROUP_ID = "group_id";

    private static final String NON_RENSEIGNE = "Non renseigné";

    private GroupViewModel viewModel;

    private int groupId = -1;
    private StudentGroup groupe;
    private boolean suppressionEnCours = false;

    public GroupDetailFragment() {
        // Constructeur obligatoire
    }

    public static GroupDetailFragment newInstance(int groupId) {
        GroupDetailFragment fragment = new GroupDetailFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_GROUP_ID, groupId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            groupId = getArguments().getInt(ARG_GROUP_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_group_detail,
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
                .get(GroupViewModel.class);

        CommonHeaderHelper.configurerEnteteSousFenetre(
                view,
                "Détail du groupe",
                "Informations et suivi pédagogique",
                requireActivity()
        );

        configurerBoutons(view);
        configurerObservateurs(view);

        if (groupId <= 0) {
            Toast.makeText(
                    requireContext(),
                    "Identifiant de groupe invalide.",
                    Toast.LENGTH_SHORT
            ).show();

            revenir();
            return;
        }

        viewModel.loadGroupById(groupId);
    }

    private void configurerBoutons(@NonNull View view) {

        MaterialButton btnEdit =
                view.findViewById(R.id.btnEditGroupDetails);

        MaterialButton btnDelete =
                view.findViewById(R.id.btnDeleteGroupDetails);

        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> ouvrirModification());
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> confirmerSuppression());
        }
    }

    private void configurerObservateurs(@NonNull View view) {

        // Les valeurs annexes (année, effectifs) sont publiées par le
        // ViewModel avant le groupe : elles sont donc déjà à jour ici.
        viewModel.getSelectedGroup().observe(
                getViewLifecycleOwner(),
                group -> {
                    if (group == null) {
                        return;
                    }

                    groupe = group;
                    afficherGroupe(view, group);
                }
        );

        viewModel.getError().observe(
                getViewLifecycleOwner(),
                erreur -> {
                    if (erreur != null && !erreur.trim().isEmpty()) {
                        suppressionEnCours = false;

                        Toast.makeText(
                                requireContext(),
                                erreur,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );

        viewModel.getMessage().observe(
                getViewLifecycleOwner(),
                message -> {
                    if (message == null
                            || message.trim().isEmpty()
                            || !suppressionEnCours) {
                        return;
                    }

                    suppressionEnCours = false;

                    if (isAdded()) {
                        Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    revenir();
                }
        );
    }

    private void afficherGroupe(
            @NonNull View view,
            @NonNull StudentGroup group) {

        String nom = group.getName();

        definirTexte(view, R.id.tvGroupDetailName, nom);

        definirTexte(
                view,
                R.id.tvGroupDetailCode,
                "Code : " + valeur(group.getCode())
        );

        definirTexte(
                view,
                R.id.tvGroupDetailSubject,
                group.getSubject()
        );

        AcademicYear annee =
                viewModel.getSelectedAcademicYear().getValue();

        definirTexte(
                view,
                R.id.tvGroupDetailAcademicYear,
                annee != null ? annee.getLabel() : null
        );

        definirTexte(
                view,
                R.id.tvGroupDetailDescription,
                group.getDescription()
        );

        Integer nbEtudiants =
                viewModel.getSelectedStudentCount().getValue();

        TextView tvEtudiants =
                view.findViewById(R.id.tvGroupDetailStudentCount);

        if (tvEtudiants != null) {
            tvEtudiants.setText(
                    String.valueOf(nbEtudiants == null ? 0 : nbEtudiants)
            );
        }

        Integer nbSeances =
                viewModel.getSelectedSessionCount().getValue();

        TextView tvSeances =
                view.findViewById(R.id.tvGroupDetailSessionCount);

        if (tvSeances != null) {
            tvSeances.setText(
                    String.valueOf(nbSeances == null ? 0 : nbSeances)
            );
        }

        TextView initials =
                view.findViewById(R.id.tvGroupDetailInitials);

        if (initials != null) {
            initials.setText(initiales(nom));
        }
    }

    private void definirTexte(
            @NonNull View root,
            @IdRes int id,
            @Nullable String valeur) {

        TextView cible = root.findViewById(id);

        if (cible != null) {
            cible.setText(
                    valeur == null || valeur.trim().isEmpty()
                            ? NON_RENSEIGNE
                            : valeur
            );
        }
    }

    private String valeur(@Nullable String texte) {
        return texte == null ? "" : texte;
    }

    private String initiales(@Nullable String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            return "G";
        }

        String[] mots = nom.trim().split("\\s+");

        if (mots.length == 1) {
            return mots[0].substring(0, 1).toUpperCase();
        }

        return (
                mots[0].substring(0, 1)
                        + mots[mots.length - 1].substring(0, 1)
        ).toUpperCase();
    }

    private void ouvrirModification() {

        if (groupe == null || !isAdded()) {
            return;
        }

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.mainContainer,
                        GroupFormFragment.newInstance(groupe.getId())
                )
                .addToBackStack("group_edit")
                .commit();
    }

    private void confirmerSuppression() {

        if (groupe == null || suppressionEnCours) {
            return;
        }

        DeletionConfirmationDialog.show(
                requireContext(),
                "Supprimer le groupe ?",
                "Voulez-vous vraiment supprimer le groupe « "
                        + valeur(groupe.getName()) + " » ?",
                () -> {
                    suppressionEnCours = true;
                    viewModel.deleteGroup(groupe);
                }
        );
    }

    private void revenir() {
        if (isAdded()) {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        }
    }
}