package com.example.praticum.ui.academicyears;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.example.praticum.core.common.FormExitConfirmation;
import com.example.praticum.data.entity.AcademicYear;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AcademicYearFormFragment extends Fragment {

    private static final String ARG_YEAR_ID = "academic_year_id";
    private static final int NEW_YEAR_ID = -1;

    private int yearId = NEW_YEAR_ID;

    private AcademicYearViewModel viewModel;

    private TextInputLayout tilStart;
    private TextInputLayout tilEnd;
    private TextInputLayout tilStartDate;
    private TextInputLayout tilEndDate;

    private TextInputEditText etStart;
    private TextInputEditText etEnd;
    private TextInputEditText etStartDate;
    private TextInputEditText etEndDate;

    private TextView tvPreview;
    private TextView tvError;

    private MaterialCardView cardError;
    private CheckBox cbActive;

    private MaterialButton btnSave;
    private MaterialButton btnCancel;

    private boolean yearLoaded = false;
    private boolean saveInProgress = false;
    private boolean pendingSaveResult = false;

    private String pendingSuccessMessage;

    private final SimpleDateFormat displayFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private final SimpleDateFormat storageFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);

    public AcademicYearFormFragment() {
        // Constructeur public requis par Android.
    }

    public static AcademicYearFormFragment newInstance(int id) {
        AcademicYearFormFragment fragment =
                new AcademicYearFormFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_YEAR_ID, id);
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
                R.layout.fragment_academic_year_form,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        yearId = requireArguments().getInt(
                ARG_YEAR_ID,
                NEW_YEAR_ID
        );

        initialiserVues(view);
        configurerEntete(view);

        viewModel = new ViewModelProvider(requireActivity())
                .get(AcademicYearViewModel.class);

        configurerApercu();
        configurerDates();
        configurerBoutons();
        observerViewModel();

        if (yearId == NEW_YEAR_ID) {
            initialiserNouvelleAnnee();
        } else {
            chargerAnneeExistante();
        }
    }

    private void initialiserVues(View view) {

        tilStart = view.findViewById(
                R.id.tilAcademicYearFormStart);
        tilEnd = view.findViewById(
                R.id.tilAcademicYearFormEnd);
        tilStartDate = view.findViewById(
                R.id.tilAcademicYearFormStartDate);
        tilEndDate = view.findViewById(
                R.id.tilAcademicYearFormEndDate);

        etStart = view.findViewById(
                R.id.etAcademicYearFormStart);
        etEnd = view.findViewById(
                R.id.etAcademicYearFormEnd);
        etStartDate = view.findViewById(
                R.id.etAcademicYearFormStartDate);
        etEndDate = view.findViewById(
                R.id.etAcademicYearFormEndDate);

        tvPreview = view.findViewById(
                R.id.tvAcademicYearFormPreview);

        cardError = view.findViewById(
                R.id.cardAcademicYearFormError);
        tvError = view.findViewById(
                R.id.tvAcademicYearFormError);

        cbActive = view.findViewById(
                R.id.cbAcademicYearFormSetActive);

        btnSave = view.findViewById(
                R.id.btnSaveAcademicYear);
        btnCancel = view.findViewById(
                R.id.btnCancelAcademicYear);
    }

    private void configurerEntete(View view) {

        String titre = yearId == NEW_YEAR_ID
                ? "Nouvelle année universitaire"
                : "Modifier l'année universitaire";

        CommonHeaderHelper.configurerEnteteSousFenetre(
                view,
                titre,
                "Gestion des années universitaires",
                requireActivity()
        );
    }

    private void configurerApercu() {

        TextWatcher watcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                actualiserApercu();
                masquerErreur();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        etStart.addTextChangedListener(watcher);
        etEnd.addTextChangedListener(watcher);
    }

    private void actualiserApercu() {

        String debut = texte(etStart);
        String fin = texte(etEnd);

        if (debut.length() == 4 && fin.length() == 4) {
            tvPreview.setText(
                    "Année académique : " + debut + "-" + fin
            );
        } else {
            tvPreview.setText("Année académique : —");
        }
    }

    private void configurerDates() {

        etStartDate.setOnClickListener(
                v -> afficherCalendrier(etStartDate));

        etEndDate.setOnClickListener(
                v -> afficherCalendrier(etEndDate));

        tilStartDate.setEndIconOnClickListener(
                v -> afficherCalendrier(etStartDate));

        tilEndDate.setEndIconOnClickListener(
                v -> afficherCalendrier(etEndDate));
    }

    private void afficherCalendrier(TextInputEditText cible) {

        Calendar calendar = Calendar.getInstance();
        Date dateExistante = parserDateAffichee(texte(cible));

        if (dateExistante != null) {
            calendar.setTime(dateExistante);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (picker, year, month, day) -> {

                    Calendar selection = Calendar.getInstance();
                    selection.set(year, month, day);

                    cible.setText(
                            displayFormat.format(selection.getTime())
                    );

                    masquerErreur();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void initialiserNouvelleAnnee() {

        int anneeActuelle = Calendar.getInstance()
                .get(Calendar.YEAR);

        etStart.setText(String.valueOf(anneeActuelle));
        etEnd.setText(String.valueOf(anneeActuelle + 1));

        actualiserApercu();
    }

    private void chargerAnneeExistante() {

        List<AcademicYear> years =
                viewModel.getAcademicYears().getValue();

        if (years != null && afficherAnneeParId(years)) {
            return;
        }

        viewModel.loadAcademicYears();
    }

    private boolean afficherAnneeParId(List<AcademicYear> years) {

        for (AcademicYear year : years) {
            if (year.getId() == yearId) {
                afficherAnnee(year);
                yearLoaded = true;
                return true;
            }
        }

        return false;
    }

    private void afficherAnnee(AcademicYear year) {

        etStart.setText(String.valueOf(year.getStartYear()));
        etEnd.setText(String.valueOf(year.getEndYear()));

        etStartDate.setText(
                convertirDate(year.getStartDate(), true));

        etEndDate.setText(
                convertirDate(year.getEndDate(), true));

        cbActive.setChecked(year.isActive());

        actualiserApercu();
    }

    private void observerViewModel() {

        viewModel.getAcademicYears().observe(
                getViewLifecycleOwner(),
                years -> {
                    if (yearId == NEW_YEAR_ID
                            || yearLoaded
                            || years == null) {
                        return;
                    }

                    if (!afficherAnneeParId(years)) {
                        afficherErreur(
                                "Cette année universitaire n'existe plus."
                        );
                    }
                });

        viewModel.getActiveAcademicYear().observe(
                getViewLifecycleOwner(),
                activeYear -> {
                    // L'année active est utilisée au moment
                    // où l'utilisateur confirme le remplacement.
                });

        viewModel.getOperationSuccess().observe(
                getViewLifecycleOwner(),
                success -> {
                    if (!saveInProgress || success == null) {
                        return;
                    }

                    pendingSaveResult = true;

                    if (pendingSuccessMessage != null) {
                        traiterResultatEnregistrement(success);
                    }
                });

        viewModel.getOperationMessage().observe(
                getViewLifecycleOwner(),
                message -> {
                    if (!saveInProgress
                            || message == null
                            || message.trim().isEmpty()) {
                        return;
                    }

                    pendingSuccessMessage = message;

                    Boolean success =
                            viewModel.getOperationSuccess().getValue();

                    if (success != null && pendingSaveResult) {
                        traiterResultatEnregistrement(success);
                    }
                });
    }

    private void traiterResultatEnregistrement(boolean success) {

        if (!saveInProgress) {
            return;
        }

        saveInProgress = false;
        pendingSaveResult = false;

        btnSave.setEnabled(true);

        if (success) {
            Toast.makeText(
                    requireContext(),
                    pendingSuccessMessage,
                    Toast.LENGTH_SHORT
            ).show();

            revenirEnArriere();
        } else {
            afficherErreur(pendingSuccessMessage);
        }

        pendingSuccessMessage = null;
    }

    private void enregistrer() {

        masquerErreur();

        String debutTexte = texte(etStart);
        String finTexte = texte(etEnd);

        if (debutTexte.length() != 4
                || finTexte.length() != 4) {
            afficherErreur(
                    "Saisissez une année de début et une année de fin sur quatre chiffres."
            );
            return;
        }

        int startYear;
        int endYear;

        try {
            startYear = Integer.parseInt(debutTexte);
            endYear = Integer.parseInt(finTexte);
        } catch (NumberFormatException e) {
            afficherErreur("Les années saisies sont invalides.");
            return;
        }

        if (startYear < 1900 || startYear > 2200) {
            afficherErreur(
                    "L'année de début doit être comprise entre 1900 et 2200."
            );
            return;
        }

        if (endYear != startYear + 1) {
            afficherErreur(
                    "L'année de fin doit suivre immédiatement l'année de début. Exemple : 2026-2027."
            );
            return;
        }

        String startDateText = texte(etStartDate);
        String endDateText = texte(etEndDate);

        String startDate = convertirDate(startDateText, false);
        String endDate = convertirDate(endDateText, false);

        if (!startDateText.isEmpty() && startDate == null) {
            afficherErreur("La date de début est invalide.");
            return;
        }

        if (!endDateText.isEmpty() && endDate == null) {
            afficherErreur("La date de fin est invalide.");
            return;
        }

        if (startDate != null && endDate != null
                && endDate.compareTo(startDate) < 0) {
            afficherErreur(
                    "La date de fin doit être postérieure ou égale à la date de début."
            );
            return;
        }

        boolean setActive = cbActive.isChecked();

        AcademicYear activeYear =
                viewModel.getActiveAcademicYear().getValue();

        if (setActive && activeYear != null
                && activeYear.getId() != yearId) {

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Changer l'année active ?")
                    .setMessage(
                            "L'année active actuelle sera remplacée. "
                                    + "Les données précédentes seront conservées."
                    )
                    .setNegativeButton("Annuler", null)
                    .setPositiveButton(
                            "Continuer",
                            (dialog, which) ->
                                    sauvegarder(
                                            startYear,
                                            endYear,
                                            startDate,
                                            endDate,
                                            true
                                    )
                    )
                    .show();

            return;
        }

        sauvegarder(
                startYear,
                endYear,
                startDate,
                endDate,
                setActive
        );
    }

    private void sauvegarder(
            int startYear,
            int endYear,
            String startDate,
            String endDate,
            boolean setActive) {

        AcademicYear year = new AcademicYear(
                startYear,
                endYear,
                startDate,
                endDate,
                setActive
        );

        if (yearId != NEW_YEAR_ID) {
            year.setId(yearId);
        }

        saveInProgress = true;
        pendingSaveResult = false;
        pendingSuccessMessage = null;

        btnSave.setEnabled(false);

        if (yearId == NEW_YEAR_ID) {
            viewModel.addAcademicYear(year);
        } else {
            viewModel.updateAcademicYear(year);
        }
    }

    private String convertirDate(
            String date,
            boolean versAffichage) {

        if (date == null || date.trim().isEmpty()) {
            return null;
        }

        SimpleDateFormat source = versAffichage
                ? storageFormat
                : displayFormat;

        SimpleDateFormat destination = versAffichage
                ? displayFormat
                : storageFormat;

        source.setLenient(false);
        destination.setLenient(false);

        try {
            Date parsed = source.parse(date);
            return parsed == null
                    ? null
                    : destination.format(parsed);
        } catch (ParseException e) {
            return null;
        }
    }

    private Date parserDateAffichee(String date) {

        if (date == null || date.trim().isEmpty()) {
            return null;
        }

        displayFormat.setLenient(false);

        try {
            return displayFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    private String texte(TextInputEditText editText) {

        return editText.getText() == null
                ? ""
                : editText.getText().toString().trim();
    }

    private void afficherErreur(String message) {

        tvError.setText(message);
        cardError.setVisibility(View.VISIBLE);
    }

    private void masquerErreur() {

        if (cardError != null) {
            cardError.setVisibility(View.GONE);
        }

        if (tvError != null) {
            tvError.setText("");
        }

        if (tilStart != null) tilStart.setError(null);
        if (tilEnd != null) tilEnd.setError(null);
        if (tilStartDate != null) tilStartDate.setError(null);
        if (tilEndDate != null) tilEndDate.setError(null);
    }

    private void configurerBoutons() {

        btnCancel.setOnClickListener(v -> FormExitConfirmation.show(this, this::revenirEnArriere));

        btnSave.setOnClickListener(v -> enregistrer());
    }

    private void revenirEnArriere() {

        getParentFragmentManager().popBackStack();
    }
}