
package com.example.praticum.ui.modules;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.example.praticum.data.entity.Level;
import com.example.praticum.data.entity.Module;
import com.example.praticum.ui.levels.LevelViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class ModuleFormFragment extends Fragment {

    private static final String ARG_MODULE_ID = "module_id";
    private static final String ARG_LEVEL_ID = "level_id";

    private ModuleViewModel viewModel;

    private long moduleId = -1;
    private int levelId = -1;

    private Module moduleExistant;
    private boolean sauvegardeEnCours = false;

    private TextInputLayout tilCode;
    private TextInputLayout tilName;
    private TextInputLayout tilSemester;
    private TextInputLayout tilCoefficient;
    private TextInputLayout tilDescription;

    private TextInputEditText etCode;
    private TextInputEditText etName;
    private TextInputEditText etCoefficient;
    private TextInputEditText etDescription;

    private MaterialAutoCompleteTextView actvSemester;

    private LevelViewModel levelViewModel;

    private TextInputLayout tilModuleLevel;
    private MaterialAutoCompleteTextView actvModuleLevel;

    private final List<Level> niveaux = new ArrayList<>();
    private Level niveauSelectionne;
    private ArrayAdapter<String> levelAdapter;


    public ModuleFormFragment() {
        // Constructeur requis par Android.
    }

    public static ModuleFormFragment newInstance() {
        return new ModuleFormFragment();
    }

    public static ModuleFormFragment newInstance(long moduleId) {
        ModuleFormFragment fragment = new ModuleFormFragment();

        Bundle args = new Bundle();
        args.putLong(ARG_MODULE_ID, moduleId);
        fragment.setArguments(args);

        return fragment;
    }

    public static ModuleFormFragment newInstanceForLevel(int levelId) {
        ModuleFormFragment fragment = new ModuleFormFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_LEVEL_ID, levelId);
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
                R.layout.fragment_module_form,
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
                .get(ModuleViewModel.class);

        moduleId = getArguments() == null
                ? -1
                : getArguments().getLong(ARG_MODULE_ID, -1);

        levelId = getArguments() == null
                ? -1
                : getArguments().getInt(ARG_LEVEL_ID, -1);

        initialiserVues(view);
        configurerEntete(view);
        configurerSemestres();
        configurerNiveaux();
        observerResultats();


        MaterialButton btnSave =
                view.findViewById(R.id.btnModuleFormSave);

        MaterialButton btnCancel =
                view.findViewById(R.id.btnModuleFormCancel);

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> enregistrer());
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> revenir());
        }

        if (moduleId >= 0) {
            chargerModuleExistant();
        } else {
            afficherSemestre(1);
        }
    }

    private void initialiserVues(View view) {
        tilCode = view.findViewById(R.id.tilModuleCode);
        tilName = view.findViewById(R.id.tilModuleName);
        tilSemester = view.findViewById(R.id.tilModuleSemester);
        tilCoefficient = view.findViewById(R.id.tilModuleCoefficient);
        tilDescription = view.findViewById(R.id.tilModuleDescription);

        etCode = view.findViewById(R.id.etModuleCode);
        etName = view.findViewById(R.id.etModuleName);
        actvSemester = view.findViewById(R.id.actvModuleSemester);
        etCoefficient = view.findViewById(R.id.etModuleCoefficient);
        etDescription = view.findViewById(R.id.etModuleDescription);

        tilModuleLevel = view.findViewById(R.id.tilModuleLevel);
        actvModuleLevel = view.findViewById(R.id.actvModuleLevel);
    }

    private void configurerEntete(View view) {
        String titre = moduleId >= 0
                ? "Modifier le module"
                : "Nouveau module";

        String sousTitre = moduleId >= 0
                ? "Modifiez les informations du module"
                : "Renseignez les informations du module";

        CommonHeaderHelper.configurerEnteteSousFenetre(
                view,
                titre,
                sousTitre,
                requireActivity()
        );
    }

    private void configurerSemestres() {
        String[] semestres = {
                "Semestre 1",
                "Semestre 2"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                semestres
        );

        actvSemester.setAdapter(adapter);
        actvSemester.setOnClickListener(
                v -> actvSemester.showDropDown()
        );
    }

    private void configurerNiveaux() {
        levelViewModel = new ViewModelProvider(this)
                .get(LevelViewModel.class);

        levelAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );

        actvModuleLevel.setAdapter(levelAdapter);

        actvModuleLevel.setOnClickListener(
                v -> actvModuleLevel.showDropDown()
        );

        actvModuleLevel.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < niveaux.size()) {
                niveauSelectionne = niveaux.get(position);
                levelId = niveauSelectionne.getId();

                tilModuleLevel.setError(null);
            }
        });

        levelViewModel.getLevels().observe(
                getViewLifecycleOwner(),
                liste -> {
                    if (liste == null) {
                        return;
                    }

                    niveaux.clear();
                    niveaux.addAll(liste);

                    List<String> libelles = new ArrayList<>();

                    for (Level niveau : niveaux) {
                        libelles.add(
                                niveau.getCode() + " — " + niveau.getName()
                        );
                    }

                    levelAdapter.clear();
                    levelAdapter.addAll(libelles);
                    levelAdapter.notifyDataSetChanged();

                    selectionnerNiveauParId(levelId);
                }
        );

        levelViewModel.loadLevels();
    }

    private void selectionnerNiveauParId(int id) {
        niveauSelectionne = null;

        for (int i = 0; i < niveaux.size(); i++) {
            Level niveau = niveaux.get(i);

            if (niveau.getId() == id) {
                niveauSelectionne = niveau;
                levelId = niveau.getId();

                actvModuleLevel.setText(
                        levelAdapter.getItem(i),
                        false
                );

                tilModuleLevel.setError(null);
                return;
            }
        }

        actvModuleLevel.setText("", false);

        // Ne pas remplacer levelId ici :
        // le module peut encore être en cours de chargement.
    }

    private void chargerModuleExistant() {
        viewModel.getModules().observe(
                getViewLifecycleOwner(),
                modules -> {
                    if (modules == null) {
                        return;
                    }

                    for (Module element : modules) {
                        if (element.getId() == moduleId) {
                            moduleExistant = element;
                            levelId = element.getLevelId();
                            remplirFormulaire(element);
                            return;
                        }
                    }
                }
        );

        viewModel.loadAll();
    }

    private void remplirFormulaire(Module module) {
        etCode.setText(module.getCode());
        etName.setText(module.getName());
        etCoefficient.setText(String.valueOf(module.getCoefficient()));
        etDescription.setText(module.getDescription());
        afficherSemestre(module.getSemester());
        levelId = module.getLevelId();
        selectionnerNiveauParId(levelId);
    }

    private void afficherSemestre(int semestre) {
        actvSemester.setText(
                semestre == 2 ? "Semestre 2" : "Semestre 1",
                false
        );
    }

    @Nullable
    private Integer lireSemestre() {
        String valeur = actvSemester.getText().toString().trim();

        if ("Semestre 1".equals(valeur)) {
            return 1;
        }

        if ("Semestre 2".equals(valeur)) {
            return 2;
        }

        return null;
    }

    private void enregistrer() {
        if (sauvegardeEnCours) {
            return;
        }

        effacerErreurs();

        String code = texte(etCode);
        String name = texte(etName);
        String coefficientText = texte(etCoefficient);
        String description = texte(etDescription);

        if (code.isEmpty()) {
            tilCode.setError("Le code est obligatoire");
            etCode.requestFocus();
            return;
        }

        if (name.isEmpty()) {
            tilName.setError("L'intitulé est obligatoire");
            etName.requestFocus();
            return;
        }

        Integer semester = lireSemestre();

        if (semester == null) {
            tilSemester.setError("Choisissez un semestre");
            actvSemester.requestFocus();
            return;
        }

        if (niveauSelectionne == null) {
            tilModuleLevel.setError("Choisissez un niveau d'étude");
            actvModuleLevel.requestFocus();
            return;
        }

        levelId = niveauSelectionne.getId();
        tilModuleLevel.setError(null);

        if (coefficientText.isEmpty()) {
            tilCoefficient.setError("Le coefficient est obligatoire");
            etCoefficient.requestFocus();
            return;
        }

        double coefficient;

        try {
            coefficient = Double.parseDouble(
                    coefficientText.replace(',', '.')
            );

            if (!Double.isFinite(coefficient) || coefficient <= 0) {
                tilCoefficient.setError(
                        "Le coefficient doit être positif"
                );
                etCoefficient.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            tilCoefficient.setError("Coefficient invalide");
            etCoefficient.requestFocus();
            return;
        }

        Module module = new Module(
                code,
                name,
                levelId,
                semester,
                coefficient,
                description
        );

        if (moduleId >= 0) {
            module.setId((int) moduleId);
        }

        sauvegardeEnCours = true;

        if (moduleId < 0) {
            viewModel.add(module);
        } else {
            viewModel.update(module);
        }
    }

    private void observerResultats() {
        viewModel.getError().observe(
                getViewLifecycleOwner(),
                erreur -> {
                    if (erreur != null && !erreur.trim().isEmpty()) {
                        sauvegardeEnCours = false;

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
                            && sauvegardeEnCours
                            && isAdded()) {

                        sauvegardeEnCours = false;

                        Toast.makeText(
                                requireContext(),
                                moduleId < 0
                                        ? "Module ajouté avec succès."
                                        : "Module modifié avec succès.",
                                Toast.LENGTH_SHORT
                        ).show();

                        revenir();
                    }
                }
        );
    }

    private String texte(TextInputEditText editText) {
        return editText.getText() == null
                ? ""
                : editText.getText().toString().trim();
    }

    private void effacerErreurs() {
        tilCode.setError(null);
        tilName.setError(null);
        tilSemester.setError(null);
        tilCoefficient.setError(null);
        tilDescription.setError(null);
        tilModuleLevel.setError(null);
    }

    private void revenir() {
        if (isAdded()) {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        }
    }
}
