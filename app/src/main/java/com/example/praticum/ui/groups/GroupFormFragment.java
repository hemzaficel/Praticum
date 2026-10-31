package com.example.praticum.ui.groups;

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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.example.praticum.data.entity.AcademicYear;
import com.example.praticum.data.entity.Level;
import com.example.praticum.data.entity.StudentGroup;
import com.example.praticum.ui.academicyears.AcademicYearViewModel;
import com.example.praticum.ui.levels.LevelViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class GroupFormFragment extends Fragment {

    private static final String ARG_GROUP_ID = "group_id";

    private int groupId = -1;
    private boolean modeEdition = false;
    private boolean chargementTermine = false;
    private boolean enregistrementEnCours = false;

    private GroupViewModel viewModel;
    private LevelViewModel levelViewModel;
    private AcademicYearViewModel academicYearViewModel;

    private TextInputLayout tilName;
    private TextInputLayout tilCode;
    private TextInputLayout tilLevel;
    private TextInputLayout tilYear;

    private TextInputEditText etName;
    private TextInputEditText etCode;
    private TextInputEditText etDescription;

    private MaterialAutoCompleteTextView actLevel;
    private MaterialAutoCompleteTextView actYear;
    private MaterialAutoCompleteTextView actSubject;

    private MaterialCardView cardValidation;
    private TextView tvValidationMessage;
    private View btnSave;
    private View btnCancel;

    private final List<Level> niveaux = new ArrayList<>();
    private final List<AcademicYear> annees = new ArrayList<>();

    private ArrayAdapter<String> levelAdapter;
    private ArrayAdapter<String> yearAdapter;

    private Integer niveauId;
    private Integer anneeId;

    public GroupFormFragment() {
        // Constructeur obligatoire
    }

    public static GroupFormFragment newInstance(int groupId) {
        GroupFormFragment fragment = new GroupFormFragment();

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

        modeEdition = groupId > 0;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_group_form,
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

        levelViewModel = new ViewModelProvider(this)
                .get(LevelViewModel.class);

        academicYearViewModel = new ViewModelProvider(this)
                .get(AcademicYearViewModel.class);

        initialiserVues(view);
        configurerEntete(view);
        configurerListes();
        configurerBoutons();
        configurerObservateurs();

        if (modeEdition) {
            chargementTermine = false;
            activerFormulaire(false);
            viewModel.loadGroupById(groupId);
        } else {
            chargementTermine = true;
            activerFormulaire(true);
        }
    }

    private void initialiserVues(@NonNull View view) {

        tilName = view.findViewById(R.id.tilGroupName);
        tilCode = view.findViewById(R.id.tilGroupCode);
        tilLevel = view.findViewById(R.id.tilGroupLevel);
        tilYear = view.findViewById(R.id.tilGroupAcademicYear);

        etName = view.findViewById(R.id.etGroupName);
        etCode = view.findViewById(R.id.etGroupCode);
        etDescription = view.findViewById(R.id.etGroupDescription);

        actLevel = view.findViewById(R.id.actGroupLevel);
        actYear = view.findViewById(R.id.actGroupAcademicYear);
        actSubject = view.findViewById(R.id.actGroupSubject);

        cardValidation =
                view.findViewById(R.id.cardGroupFormValidation);

        tvValidationMessage =
                view.findViewById(R.id.tvGroupFormValidationMessage);

        btnCancel = view.findViewById(R.id.btnCancelGroupForm);
        btnSave = view.findViewById(R.id.btnSaveGroup);
    }

    private void configurerEntete(@NonNull View view) {

        CommonHeaderHelper.configurerEnteteSousFenetre(
                view,
                modeEdition ? "Modifier le groupe" : "Nouveau groupe",
                modeEdition
                        ? "Mettre à jour les informations du groupe"
                        : "Renseignez les informations du groupe",
                requireActivity()
        );

        // Comme avant : la flèche retour demande confirmation.
        View btnBack = view.findViewById(R.id.btnSubscreenHeaderBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> confirmerRetour());
        }
    }

    private void configurerListes() {

        // Spécialités
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                viewModel.getSpecializations()
        );

        actSubject.setAdapter(adapter);


        actSubject.setOnClickListener(v -> {
            if (peutModifier()) {
                actSubject.showDropDown();
            }
        });

        // Niveaux d'étude
        levelAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );

        actLevel.setAdapter(levelAdapter);

        actLevel.setOnClickListener(v -> {
            if (peutModifier()) {
                actLevel.showDropDown();
            }
        });

        actLevel.setOnItemClickListener((parent, v, position, id) -> {
            if (position >= 0 && position < niveaux.size()) {
                niveauId = niveaux.get(position).getId();
                tilLevel.setError(null);
            }
        });

        // Années universitaires
        yearAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );

        actYear.setAdapter(yearAdapter);

        actYear.setOnClickListener(v -> {
            if (peutModifier()) {
                actYear.showDropDown();
            }
        });

        actYear.setOnItemClickListener((parent, v, position, id) -> {
            if (position >= 0 && position < annees.size()) {
                anneeId = annees.get(position).getId();
                tilYear.setError(null);
            }
        });
    }

    private boolean peutModifier() {
        return chargementTermine && !enregistrementEnCours;
    }

    private void configurerBoutons() {

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> confirmerRetour());
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> enregistrer());
        }
    }

    private void configurerObservateurs() {

        levelViewModel.getLevels().observe(
                getViewLifecycleOwner(),
                liste -> {
                    niveaux.clear();

                    if (liste != null) {
                        niveaux.addAll(liste);
                    }

                    List<String> libelles = new ArrayList<>();

                    for (Level niveau : niveaux) {
                        libelles.add(
                                niveau.getCode() + " — " + niveau.getName()
                        );
                    }

                    levelAdapter.clear();
                    levelAdapter.addAll(libelles);
                    levelAdapter.notifyDataSetChanged();

                    afficherNiveauSelectionne();
                }
        );

        academicYearViewModel.getAcademicYears().observe(
                getViewLifecycleOwner(),
                liste -> {
                    annees.clear();

                    if (liste != null) {
                        annees.addAll(liste);
                    }

                    List<String> libelles = new ArrayList<>();

                    for (AcademicYear annee : annees) {
                        libelles.add(annee.getLabel());
                    }

                    yearAdapter.clear();
                    yearAdapter.addAll(libelles);
                    yearAdapter.notifyDataSetChanged();

                    afficherAnneeSelectionnee();
                }
        );

        viewModel.getSelectedGroup().observe(
                getViewLifecycleOwner(),
                group -> {
                    if (!modeEdition || group == null || chargementTermine) {
                        return;
                    }

                    preremplir(group);
                    chargementTermine = true;

                    activerFormulaire(true);
                    masquerErreur();
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

    private void preremplir(@NonNull StudentGroup group) {

        etName.setText(group.getName());
        etCode.setText(group.getCode());
        etDescription.setText(group.getDescription());

        if (group.getSubject() != null) {
            actSubject.setText(group.getSubject(), false);
        }

        niveauId = group.getLevelId();
        anneeId = group.getAcademicYearId();

        afficherNiveauSelectionne();
        afficherAnneeSelectionnee();
    }

    private void afficherNiveauSelectionne() {

        if (niveauId == null || actLevel == null) {
            return;
        }

        for (int i = 0; i < niveaux.size(); i++) {
            if (niveaux.get(i).getId() == niveauId) {
                actLevel.setText(levelAdapter.getItem(i), false);
                return;
            }
        }
    }

    private void afficherAnneeSelectionnee() {

        if (anneeId == null || actYear == null) {
            return;
        }

        for (int i = 0; i < annees.size(); i++) {
            if (annees.get(i).getId() == anneeId) {
                actYear.setText(yearAdapter.getItem(i), false);
                return;
            }
        }
    }

    private void enregistrer() {

        if (!chargementTermine || enregistrementEnCours) {
            return;
        }

        masquerErreur();

        if (!valider()) {
            return;
        }

        String nom = texte(etName);
        String code = texte(etCode);
        String matiere = texte(actSubject);
        String description = texte(etDescription);

        StudentGroup group = new StudentGroup(
                nom,
                code,
                anneeId,
                niveauId,
                matiere.isEmpty() ? null : matiere,
                description.isEmpty() ? null : description
        );

        if (modeEdition) {
            group.setId(groupId);
        }

        enregistrementEnCours = true;
        activerFormulaire(false);
        masquerClavier();

        if (modeEdition) {
            viewModel.updateGroup(group);
        } else {
            viewModel.addGroup(group);
        }
    }

    private boolean valider() {

        boolean valide = true;

        effacerErreursChamps();

        if (texte(etName).isEmpty()) {
            tilName.setError("Le nom du groupe est obligatoire");
            valide = false;
        }

        if (texte(etCode).isEmpty()) {
            tilCode.setError("Le code du groupe est obligatoire");
            valide = false;
        }

        if (niveauId == null) {
            tilLevel.setError("Le niveau d'étude est obligatoire");
            valide = false;
        }

        if (anneeId == null) {
            tilYear.setError("L'année universitaire est obligatoire");
            valide = false;
        }

        if (!valide) {
            afficherErreur("Veuillez corriger les champs en erreur.");
        }

        return valide;
    }

    private void effacerErreursChamps() {

        tilName.setError(null);
        tilCode.setError(null);
        tilLevel.setError(null);
        tilYear.setError(null);
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

        if (etName != null) etName.setEnabled(actif);
        if (etCode != null) etCode.setEnabled(actif);
        if (etDescription != null) etDescription.setEnabled(actif);
        if (actLevel != null) actLevel.setEnabled(actif);
        if (actYear != null) actYear.setEnabled(actif);
        if (actSubject != null) actSubject.setEnabled(actif);

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

    private void confirmerRetour() {

        if (enregistrementEnCours) {
            return;
        }

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirmation, null);

        TextView title =
                dialogView.findViewById(R.id.tvConfirmationTitle);

        TextView message =
                dialogView.findViewById(R.id.tvConfirmationMessage);

        MaterialButton cancel =
                dialogView.findViewById(R.id.btnConfirmationCancel);

        MaterialButton confirm =
                dialogView.findViewById(R.id.btnConfirmationConfirm);

        title.setText("Abandonner le formulaire ?");
        message.setText(
                "Les informations saisies ne seront pas conservées."
        );

        cancel.setText("Continuer");
        confirm.setText("Quitter");

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(requireContext())
                        .setView(dialogView)
                        .create();

        cancel.setOnClickListener(v -> dialog.dismiss());

        confirm.setOnClickListener(v -> {
            dialog.dismiss();
            retour();
        });

        dialog.show();
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

        tilName = null;
        tilCode = null;
        tilLevel = null;
        tilYear = null;

        etName = null;
        etCode = null;
        etDescription = null;

        actLevel = null;
        actYear = null;
        actSubject = null;

        cardValidation = null;
        tvValidationMessage = null;
        btnSave = null;
        btnCancel = null;

        levelAdapter = null;
        yearAdapter = null;
    }
}