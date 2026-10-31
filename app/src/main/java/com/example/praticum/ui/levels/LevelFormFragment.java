package com.example.praticum.ui.levels;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.example.praticum.core.common.FormExitConfirmation;
import com.example.praticum.data.entity.Level;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.Locale;

public class LevelFormFragment extends Fragment {

    private static final String ARG_LEVEL_ID = "level_id";

    private int levelId = -1;
    private boolean modeEdition = false;
    private boolean chargementTermine = false;
    private boolean enregistrementEnCours = false;

    private LevelViewModel viewModel;

    private TextInputLayout tilCode;
    private TextInputLayout tilName;
    private TextInputLayout tilCycle;
    private TextInputLayout tilOrder;

    private TextInputEditText etCode;
    private TextInputEditText etName;
    private TextInputEditText etOrder;
    private TextInputEditText etDescription;

    private MaterialAutoCompleteTextView actCycle;

    private MaterialCardView cardValidation;
    private TextView tvValidationMessage;
    private View btnSave;
    private View btnCancel;

    public LevelFormFragment() {
        // Constructeur obligatoire
    }

    public static LevelFormFragment newInstance(int levelId) {
        LevelFormFragment fragment = new LevelFormFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_LEVEL_ID, levelId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            levelId = getArguments().getInt(ARG_LEVEL_ID, -1);
        }

        modeEdition = levelId > 0;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_level_form,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        initialiserVues(view);
        configurerListeCycle();

        viewModel = new ViewModelProvider(this)
                .get(LevelViewModel.class);

        CommonHeaderHelper.configurerEnteteSousFenetre(
                view,
                modeEdition ? "Modifier le niveau" : "Ajouter un niveau",
                modeEdition
                        ? "Chargement du niveau…"
                        : "Renseignez les informations du niveau",
                requireActivity()
        );

        btnCancel = view.findViewById(R.id.btnCancelLevelForm);
        btnSave = view.findViewById(R.id.btnSaveLevel);

        configurerBoutons();
        configurerObservateurs(view);

        if (modeEdition) {
            chargementTermine = false;
            activerFormulaire(false);
            viewModel.loadLevelById(levelId);
        } else {
            chargementTermine = true;
            activerFormulaire(true);
        }
    }

    private void initialiserVues(@NonNull View view) {

        tilCode = view.findViewById(R.id.tilLevelCode);
        tilName = view.findViewById(R.id.tilLevelName);
        tilCycle = view.findViewById(R.id.tilLevelCycle);
        tilOrder = view.findViewById(R.id.tilLevelOrder);

        etCode = view.findViewById(R.id.etLevelCode);
        etName = view.findViewById(R.id.etLevelName);
        etOrder = view.findViewById(R.id.etLevelOrder);
        etDescription = view.findViewById(R.id.etLevelDescription);
        actCycle = view.findViewById(R.id.actLevelCycle);

        cardValidation =
                view.findViewById(R.id.cardLevelFormValidation);

        tvValidationMessage =
                view.findViewById(R.id.tvLevelFormValidationMessage);
    }

    private void configurerListeCycle() {

        String[] cycles = getResources()
                .getStringArray(R.array.level_cycles);

        actCycle.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                cycles
        ));

        actCycle.setOnClickListener(v -> {
            if (chargementTermine && !enregistrementEnCours) {
                actCycle.showDropDown();
            }
        });

        actCycle.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && chargementTermine && !enregistrementEnCours) {
                actCycle.showDropDown();
            }
        });
    }

    private void configurerBoutons() {

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> FormExitConfirmation.show(this, this::retour));
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> enregistrer());
        }
    }

    private void configurerObservateurs(@NonNull View view) {

        viewModel.getSelectedLevel().observe(
                getViewLifecycleOwner(),
                level -> {
                    if (!modeEdition || level == null || chargementTermine) {
                        return;
                    }

                    preremplir(level);
                    chargementTermine = true;

                    activerFormulaire(true);
                    masquerErreur();

                    CommonHeaderHelper.configurerEnteteSousFenetre(
                            view,
                            "Modifier le niveau",
                            level.getName(),
                            requireActivity()
                    );
                }
        );

        viewModel.getError().observe(
                getViewLifecycleOwner(),
                erreur -> {
                    if (erreur == null || erreur.trim().isEmpty()) {
                        return;
                    }

                    if (enregistrementEnCours) {
                        enregistrementEnCours = false;
                        activerFormulaire(chargementTermine);
                    }

                    if (modeEdition && !chargementTermine) {
                        activerFormulaire(false);
                    }

                    afficherErreur(erreur);
                }
        );

        viewModel.getMessage().observe(
                getViewLifecycleOwner(),
                message -> {
                    if (message == null
                            || message.trim().isEmpty()
                            || !enregistrementEnCours) {
                        return;
                    }

                    enregistrementEnCours = false;

                    if (isAdded()) {
                        Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    retour();
                }
        );

        viewModel.isLoading().observe(
                getViewLifecycleOwner(),
                loading -> {
                    boolean enChargement =
                            Boolean.TRUE.equals(loading);

                    if (btnSave != null) {
                        btnSave.setEnabled(
                                chargementTermine
                                        && !enChargement
                                        && !enregistrementEnCours
                        );
                    }

                    if (btnCancel != null) {
                        btnCancel.setEnabled(!enregistrementEnCours);
                    }
                }
        );
    }

    private void preremplir(@NonNull Level level) {

        etCode.setText(level.getCode());
        etName.setText(level.getName());
        actCycle.setText(level.getCycle(), false);
        etOrder.setText(String.valueOf(level.getDisplayOrder()));
        etDescription.setText(level.getDescription());
    }

    private void enregistrer() {

        if (!chargementTermine || enregistrementEnCours) {
            return;
        }

        masquerErreur();

        if (!valider()) {
            return;
        }

        String code = texte(etCode).toUpperCase(Locale.ROOT);
        String nom = texte(etName);
        String cycle = texte(actCycle);
        int ordre = lireOrdre();
        String description = texte(etDescription);

        Level level = new Level(
                code,
                nom,
                cycle,
                ordre,
                description.isEmpty() ? null : description
        );

        if (modeEdition) {
            level.setId(levelId);
        }

        enregistrementEnCours = true;
        activerFormulaire(false);
        masquerClavier();

        if (modeEdition) {
            viewModel.updateLevel(level);
        } else {
            viewModel.addLevel(level);
        }
    }

    private boolean valider() {

        boolean valide = true;

        String code = texte(etCode);
        String nom = texte(etName);
        String cycle = texte(actCycle);
        String ordreTexte = texte(etOrder);
        int ordre = lireOrdre();

        effacerErreursChamps();

        if (code.isEmpty()) {
            tilCode.setError("Le code est obligatoire.");
            valide = false;
        }

        if (nom.isEmpty()) {
            tilName.setError("L'intitulé est obligatoire.");
            valide = false;
        }

        String[] cyclesAutorises = getResources()
                .getStringArray(R.array.level_cycles);

        if (cycle.isEmpty()
                || !Arrays.asList(cyclesAutorises).contains(cycle)) {
            tilCycle.setError("Sélectionnez un cycle valide.");
            valide = false;
        }

        if (ordreTexte.isEmpty()) {
            tilOrder.setError("L'ordre est obligatoire.");
            valide = false;
        } else if (ordre < 1) {
            tilOrder.setError(
                    "L'ordre doit être un nombre supérieur à zéro."
            );
            valide = false;
        }

        if (!valide) {
            afficherErreur("Veuillez corriger les champs en erreur.");
        }

        return valide;
    }

    private void effacerErreursChamps() {

        tilCode.setError(null);
        tilName.setError(null);
        tilCycle.setError(null);
        tilOrder.setError(null);
    }

    private int lireOrdre() {

        try {
            return Integer.parseInt(texte(etOrder));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String texte(@NonNull TextInputEditText champ) {

        return champ.getText() == null
                ? ""
                : champ.getText().toString().trim();
    }

    private String texte(@NonNull MaterialAutoCompleteTextView champ) {

        return champ.getText() == null
                ? ""
                : champ.getText().toString().trim();
    }

    private void afficherErreur(@NonNull String erreur) {

        if (cardValidation != null) {
            cardValidation.setVisibility(View.VISIBLE);
        }

        if (tvValidationMessage != null) {
            tvValidationMessage.setText(erreur);
        }
    }

    private void masquerErreur() {

        if (cardValidation != null) {
            cardValidation.setVisibility(View.GONE);
        }

        if (tvValidationMessage != null) {
            tvValidationMessage.setText("");
        }
    }

    private void activerFormulaire(boolean actif) {

        if (etCode != null) etCode.setEnabled(actif);
        if (etName != null) etName.setEnabled(actif);
        if (etOrder != null) etOrder.setEnabled(actif);
        if (etDescription != null) etDescription.setEnabled(actif);
        if (actCycle != null) actCycle.setEnabled(actif);

        if (btnSave != null) {
            btnSave.setEnabled(actif && !enregistrementEnCours);
        }

        if (btnCancel != null) {
            btnCancel.setEnabled(!enregistrementEnCours);
        }
    }

    private void masquerClavier() {

        View vue = getView();

        if (vue == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager)
                requireContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE
                );

        if (imm != null) {
            imm.hideSoftInputFromWindow(vue.getWindowToken(), 0);
        }
    }

    private void retour() {

        masquerClavier();

        if (!isAdded()) {
            return;
        }

        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        tilCode = null;
        tilName = null;
        tilCycle = null;
        tilOrder = null;

        etCode = null;
        etName = null;
        etOrder = null;
        etDescription = null;
        actCycle = null;

        cardValidation = null;
        tvValidationMessage = null;
        btnSave = null;
        btnCancel = null;
    }
}