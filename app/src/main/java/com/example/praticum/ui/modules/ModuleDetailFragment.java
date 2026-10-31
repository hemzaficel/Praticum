package com.example.praticum.ui.modules;

import android.app.AlertDialog;
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

import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.example.praticum.core.common.DeletionConfirmationDialog;
import com.example.praticum.data.entity.Course;
import com.example.praticum.data.entity.Level;
import com.example.praticum.ui.levels.LevelViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ModuleDetailFragment extends Fragment {

    private static final String ARG_MODULE_ID = "module_id";

    private ModuleViewModel viewModel;
    private LevelViewModel levelViewModel;

    private Course module;
    private View vueFragment;

    private boolean suppressionEnCours = false;

    public ModuleDetailFragment() {
        // Constructeur requis par Android.
    }

    public static ModuleDetailFragment newInstance(long moduleId) {
        ModuleDetailFragment fragment = new ModuleDetailFragment();

        Bundle args = new Bundle();
        args.putLong(ARG_MODULE_ID, moduleId);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_module_detail,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        vueFragment = view;

        viewModel = new ViewModelProvider(this)
                .get(ModuleViewModel.class);

        levelViewModel = new ViewModelProvider(this)
                .get(LevelViewModel.class);

        configurerEntete(view);
        configurerBoutons(view);
        observerResultats();

        long moduleId = getArguments() == null
                ? -1
                : getArguments().getLong(ARG_MODULE_ID, -1);

        if (moduleId < 0) {
            afficherErreurIntrouvable();
            return;
        }

        observerModules(moduleId);

        levelViewModel.getLevels().observe(
                getViewLifecycleOwner(),
                niveaux -> {
                    if (niveaux != null && module != null) {
                        afficherNiveau(niveaux);
                    }
                }
        );

        viewModel.getSpecializations().observe(
                getViewLifecycleOwner(),
                values -> {
                    if (values == null || module == null || vueFragment == null) return;
                    for (com.example.praticum.data.entity.Specialization item : values) {
                        if (item.getId() == module.getSpecializationId()) {
                            definirTexte(vueFragment, R.id.tvModuleDetailSpecialization, item.getName());
                            return;
                        }
                    }
                    definirTexte(vueFragment, R.id.tvModuleDetailSpecialization, "Spécialité introuvable");
                }
        );

        levelViewModel.loadLevels();
        viewModel.loadAll();
        viewModel.loadSpecializations();
    }

    private void observerModules(long moduleId) {
        viewModel.getModules().observe(
                getViewLifecycleOwner(),
                modules -> {
                    if (modules == null) {
                        return;
                    }

                    for (Course element : modules) {
                        if (element.getId() == moduleId) {
                            module = element;
                            afficherModule(vueFragment);

                            List<Level> niveaux =
                                    levelViewModel.getLevels().getValue();

                            if (niveaux != null) {
                                afficherNiveau(niveaux);
                            }

                            return;
                        }
                    }
                }
        );
    }

    private void observerResultats() {
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

        viewModel.getOperationSuccess().observe(
                getViewLifecycleOwner(),
                succes -> {
                    if (Boolean.TRUE.equals(succes)
                            && suppressionEnCours
                            && isAdded()) {

                        suppressionEnCours = false;

                        Toast.makeText(
                                requireContext(),
                                "Module supprimé.",
                                Toast.LENGTH_SHORT
                        ).show();

                        revenir();
                    }
                }
        );
    }

    private void configurerEntete(View view) {
        CommonHeaderHelper.configurerEnteteSousFenetre(
                view,
                "Détail du module",
                "",
                requireActivity()
        );
    }

    private void configurerBoutons(View view) {
        MaterialButton btnEdit =
                view.findViewById(R.id.btnModuleDetailEdit);

        MaterialButton btnDelete =
                view.findViewById(R.id.btnModuleDetailDelete);

        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> ouvrirModification());
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> confirmerSuppression());
        }
    }

    private void afficherModule(View view) {
        if (module == null || view == null) {
            return;
        }

        CommonHeaderHelper.configurerEnteteSousFenetre(
                view,
                "Détail du module",
                module.getName(),
                requireActivity()
        );

        definirTexte(
                view,
                R.id.tvModuleDetailCode,
                module.getCode()
        );

        definirTexte(
                view,
                R.id.tvModuleDetailName,
                module.getName()
        );

        definirTexte(
                view,
                R.id.tvModuleDetailSemester,
                "Semestre " + module.getSemester()
        );

        definirTexte(
                view,
                R.id.tvModuleDetailCoefficient,
                String.valueOf(module.getCoefficient())
        );

        String description = module.getDescription();

        definirTexte(
                view,
                R.id.tvModuleDetailDescription,
                description == null || description.trim().isEmpty()
                        ? "Aucune description"
                        : description
        );

        // Le niveau est affiché séparément, après son chargement.
        definirTexte(
                view,
                R.id.tvModuleDetailLevel,
                "Chargement du niveau…"
        );
    }

    private void afficherNiveau(List<Level> niveaux) {
        if (module == null || vueFragment == null) {
            return;
        }

        long niveauId = module.getLevelId();

        for (Level niveau : niveaux) {
            if (niveau.getId() == niveauId) {
                String code = niveau.getCode();
                String nom = niveau.getName();

                String libelle;

                if (code != null && !code.trim().isEmpty()) {
                    libelle = code + " — " + nom;
                } else {
                    libelle = nom;
                }

                definirTexte(
                        vueFragment,
                        R.id.tvModuleDetailLevel,
                        libelle
                );

                return;
            }
        }

        definirTexte(
                vueFragment,
                R.id.tvModuleDetailLevel,
                "Niveau introuvable"
        );
    }

    private void definirTexte(
            View view,
            int id,
            String value) {

        TextView textView = view.findViewById(id);

        if (textView != null) {
            textView.setText(value == null ? "" : value);
        }
    }

    private void ouvrirModification() {
        if (module == null || !isAdded()) {
            return;
        }

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.mainContainer,
                        ModuleFormFragment.newInstance(module.getId())
                )
                .addToBackStack("module_edit")
                .commit();
    }

    private void confirmerSuppression() {
        if (module == null || suppressionEnCours) {
            return;
        }

        DeletionConfirmationDialog.show(
                requireContext(),
                "Supprimer le module ?",
                "Voulez-vous vraiment supprimer le module « "
                        + module.getName() + " » ?",
                this::supprimerModule
        );
    }

    private void supprimerModule() {
        if (module == null || suppressionEnCours) {
            return;
        }

        suppressionEnCours = true;
        viewModel.delete(module);
    }

    private void afficherErreurIntrouvable() {
        Toast.makeText(
                requireContext(),
                "Identifiant de module invalide.",
                Toast.LENGTH_SHORT
        ).show();

        revenir();
    }

    private void revenir() {
        if (isAdded()) {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        }
    }
}
