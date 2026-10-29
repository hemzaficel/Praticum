package com.example.praticum.ui.students;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.example.praticum.data.entity.Level;
import com.example.praticum.data.entity.Student;
import com.example.praticum.data.entity.StudentGroup;
import com.example.praticum.ui.levels.LevelViewModel;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StudentFormFragment extends Fragment {

    private static final String ARG_STUDENT_ID = "student_id";

    private TextInputLayout tilLastName;
    private TextInputLayout tilFirstName;
    private TextInputLayout tilMatricule;
    private TextInputLayout tilGroup;
    private TextInputLayout tilEmail;

    private TextInputEditText etLastName;
    private TextInputEditText etFirstName;
    private TextInputEditText etLastNameArabic;
    private TextInputEditText etFirstNameArabic;
    private TextInputEditText etMatricule;
    private TextInputEditText etEmail;
    private TextInputEditText etNotes;

    private AutoCompleteTextView actGroup;

    private TextView tvGroupOrder;
    private ImageView ivStudentCard;
    private LinearLayout layoutCardEmpty;
    private LinearLayout layoutCardImageActions;
    private View btnSave;
    private View btnCancel;

    private Uri cardImageUri;

    private StudentViewModel viewModel;
    private LevelViewModel levelViewModel;

    private String studentId;
    private boolean isEditMode = false;
    private boolean loadingFinished = false;
    private boolean saving = false;

    // Étudiant chargé en mode modification (champs non éditables conservés)
    private Student existingStudent;

    private final List<StudentGroup> groups = new ArrayList<>();
    private final List<Level> levels = new ArrayList<>();
    private ArrayAdapter<String> groupAdapter;
    private Integer selectedGroupId;

    private ActivityResultLauncher<String[]> imagePickerLauncher;
    private ActivityResultLauncher<IntentSenderRequest> scannerLauncher;

    public StudentFormFragment() {
        // Constructeur obligatoire sans argument.
    }

    /**
     * @param studentId identifiant de l'étudiant à modifier,
     *                  ou null pour une création
     */
    public static StudentFormFragment newInstance(
            @Nullable String studentId) {

        StudentFormFragment fragment = new StudentFormFragment();

        Bundle args = new Bundle();
        args.putString(ARG_STUDENT_ID, studentId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            studentId = getArguments().getString(ARG_STUDENT_ID);
        }

        isEditMode = studentId != null && !studentId.trim().isEmpty();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) {
                        return;
                    }

                    try {
                        requireContext().getContentResolver()
                                .takePersistableUriPermission(
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                    } catch (SecurityException ignored) {
                        // L'aperçu peut tout de même être affiché.
                    }

                    cardImageUri = uri;
                    displayCardImage();
                }
        );

        scannerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() != Activity.RESULT_OK) {
                        return;
                    }

                    GmsDocumentScanningResult scanResult =
                            GmsDocumentScanningResult.fromActivityResultIntent(
                                    result.getData()
                            );

                    if (scanResult == null
                            || scanResult.getPages() == null
                            || scanResult.getPages().isEmpty()) {
                        Toast.makeText(
                                requireContext(),
                                "Aucune image numérisée.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    Uri scannedUri =
                            scanResult.getPages().get(0).getImageUri();
                    saveScannedImageLocally(scannedUri);
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_student_form,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this)
                .get(StudentViewModel.class);

        levelViewModel = new ViewModelProvider(this)
                .get(LevelViewModel.class);

        initializeViews(view);
        configureHeader(view);
        configureGroups();
        configureButtons(view);
        observeData();

        updateGroupOrderLabel();

        viewModel.loadStudents();

        if (isEditMode) {
            loadingFinished = false;
            setFormEnabled(false);
            viewModel.loadStudentById(studentId);
        } else {
            loadingFinished = true;
            setFormEnabled(true);
        }
    }

    private void initializeViews(View view) {
        tilLastName = view.findViewById(R.id.tilStudentLastName);
        tilFirstName = view.findViewById(R.id.tilStudentFirstName);
        tilMatricule = view.findViewById(R.id.tilStudentMatricule);
        tilGroup = view.findViewById(R.id.tilStudentGroup);
        tilEmail = view.findViewById(R.id.tilStudentEmail);

        etLastName = view.findViewById(R.id.etStudentLastName);
        etFirstName = view.findViewById(R.id.etStudentFirstName);
        etLastNameArabic = view.findViewById(R.id.etStudentLastNameArabic);
        etFirstNameArabic = view.findViewById(R.id.etStudentFirstNameArabic);
        etMatricule = view.findViewById(R.id.etStudentMatricule);
        etEmail = view.findViewById(R.id.etStudentEmail);
        etNotes = view.findViewById(R.id.etStudentNotes);

        actGroup = view.findViewById(R.id.actStudentGroup);
        tvGroupOrder = view.findViewById(R.id.tvStudentDetailGroupOrder);

        ivStudentCard = view.findViewById(R.id.ivStudentFormCard);
        layoutCardEmpty = view.findViewById(
                R.id.layoutStudentFormCardEmpty
        );
        layoutCardImageActions = view.findViewById(
                R.id.layoutStudentCardImageActions
        );

        btnSave = view.findViewById(R.id.btnSaveStudent);
        btnCancel = view.findViewById(R.id.btnCancelStudentForm);
    }

    private void configureHeader(View view) {
        CommonHeaderHelper.configurerEnteteSousFenetre(
                view,
                isEditMode ? "Modifier l’étudiant" : "Nouvel étudiant",
                isEditMode
                        ? "Mettre à jour les informations"
                        : "Ajouter un étudiant",
                requireActivity()
        );
    }

    private void configureGroups() {
        groupAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );

        actGroup.setAdapter(groupAdapter);

        actGroup.setOnClickListener(v -> {
            if (canEdit()) {
                actGroup.showDropDown();
            }
        });

        actGroup.setOnItemClickListener(
                (parent, itemView, position, id) -> {
                    if (position >= 0 && position < groups.size()) {
                        selectedGroupId = groups.get(position).getId();
                    }

                    tilGroup.setError(null);
                    updateGroupOrderLabel();
                }
        );
    }

    private boolean canEdit() {
        return loadingFinished && !saving;
    }

    private void configureButtons(View view) {
        btnCancel.setOnClickListener(v -> closeForm());

        btnSave.setOnClickListener(v -> saveStudent());

        view.findViewById(R.id.btnChooseStudentCardImage)
                .setOnClickListener(v ->
                        imagePickerLauncher.launch(new String[]{"image/*"})
                );

        view.findViewById(R.id.btnCaptureStudentCard)
                .setOnClickListener(v -> startDocumentScanner());

        view.findViewById(R.id.btnViewStudentCardImage)
                .setOnClickListener(v -> showCardImage());

        view.findViewById(R.id.btnRemoveStudentCardImage)
                .setOnClickListener(v -> removeCardImage());
    }

    private void observeData() {
        viewModel.getGroups().observe(
                getViewLifecycleOwner(),
                liste -> {
                    groups.clear();

                    if (liste != null) {
                        groups.addAll(liste);
                    }

                    refreshGroupLabels();
                }
        );

        levelViewModel.getLevels().observe(
                getViewLifecycleOwner(),
                liste -> {
                    levels.clear();

                    if (liste != null) {
                        levels.addAll(liste);
                    }

                    refreshGroupLabels();
                }
        );

        viewModel.getSelectedStudent().observe(
                getViewLifecycleOwner(),
                student -> {
                    if (!isEditMode || student == null || loadingFinished) {
                        return;
                    }

                    existingStudent = student;
                    prefill(student);

                    loadingFinished = true;
                    setFormEnabled(true);
                }
        );

        viewModel.getError().observe(
                getViewLifecycleOwner(),
                erreur -> {
                    if (erreur == null || erreur.trim().isEmpty()) {
                        return;
                    }

                    if (saving) {
                        saving = false;
                        setFormEnabled(loadingFinished);
                    }

                    Toast.makeText(
                            requireContext(),
                            erreur,
                            Toast.LENGTH_LONG
                    ).show();
                }
        );

        viewModel.getMessage().observe(
                getViewLifecycleOwner(),
                message -> {
                    if (message == null
                            || message.trim().isEmpty()
                            || !saving) {
                        return;
                    }

                    saving = false;

                    if (isAdded()) {
                        Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    closeForm();
                }
        );
    }

    /**
     * Reconstruit la liste déroulante : « code - niveau »
     * (par exemple « G1 - Licence 1 »). Appelée à l'arrivée des groupes
     * et à l'arrivée des niveaux, quel que soit l'ordre.
     */
    private void refreshGroupLabels() {
        if (groupAdapter == null) {
            return;
        }

        List<String> libelles = new ArrayList<>();

        for (StudentGroup group : groups) {
            libelles.add(groupLabel(group));
        }

        groupAdapter.clear();
        groupAdapter.addAll(libelles);
        groupAdapter.notifyDataSetChanged();

        showSelectedGroup();
    }

    private String groupLabel(@NonNull StudentGroup group) {
        String code = group.getCode() == null ? "" : group.getCode();
        Integer levelId = group.getLevelId();

        if (levelId != null) {
            for (Level level : levels) {
                if (level.getId() == levelId
                        && level.getName() != null
                        && !level.getName().trim().isEmpty()) {
                    return code + " - " + level.getName();
                }
            }
        }

        return code;
    }

    private void prefill(@NonNull Student student) {
        etLastName.setText(student.getLastName());
        etFirstName.setText(student.getFirstName());
        etLastNameArabic.setText(student.getLastNameArabic());
        etFirstNameArabic.setText(student.getFirstNameArabic());
        etMatricule.setText(student.getMatricule());
        etEmail.setText(student.getEmail());
        etNotes.setText(student.getNotes());

        selectedGroupId = student.getGroupId();
        showSelectedGroup();

        String imageUri = student.getCardImageUri();

        if (imageUri != null && !imageUri.trim().isEmpty()) {
            cardImageUri = Uri.parse(imageUri);
            displayCardImage();
        }

        updateGroupOrderLabel();
    }

    private void showSelectedGroup() {
        if (selectedGroupId == null
                || actGroup == null
                || groupAdapter == null) {
            return;
        }

        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getId() == selectedGroupId) {
                actGroup.setText(groupAdapter.getItem(i), false);
                return;
            }
        }
    }

    private void updateGroupOrderLabel() {
        if (tvGroupOrder == null) {
            return;
        }

        boolean sameGroup = isEditMode
                && existingStudent != null
                && selectedGroupId != null
                && selectedGroupId == existingStudent.getGroupId();

        if (sameGroup) {
            tvGroupOrder.setText(
                    "Ordre dans le groupe : "
                            + existingStudent.getGroupOrder()
            );
        } else {
            tvGroupOrder.setText(
                    "Ordre dans le groupe : attribué automatiquement"
            );
        }
    }

    private void saveStudent() {
        if (!loadingFinished || saving) {
            return;
        }

        clearErrors();

        String lastName = getText(etLastName);
        String firstName = getText(etFirstName);
        String lastNameArabic = getText(etLastNameArabic);
        String firstNameArabic = getText(etFirstNameArabic);
        String matricule = getText(etMatricule);
        String email = getText(etEmail);
        String notes = getText(etNotes);
        String groupText = getText(actGroup);

        boolean valid = true;

        if (lastName.isEmpty()) {
            tilLastName.setError("Le nom est obligatoire.");
            valid = false;
        }

        if (firstName.isEmpty()) {
            tilFirstName.setError("Le prénom est obligatoire.");
            valid = false;
        }

        if (groupText.isEmpty()) {
            tilGroup.setError("Sélectionnez un groupe.");
            valid = false;
        } else if (selectedGroupId == null) {
            tilGroup.setError("Sélectionnez un groupe dans la liste.");
            valid = false;
        }

        if (!email.isEmpty()
                && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Adresse électronique invalide.");
            valid = false;
        }

        if (!valid) {
            Toast.makeText(
                    requireContext(),
                    "Veuillez corriger les champs signalés.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Student student;

        if (isEditMode) {
            if (existingStudent == null) {
                return;
            }

            // On repart de l'étudiant chargé : identifiant, ordre et
            // autres données conservés.
            student = existingStudent;
            student.setLastName(lastName);
            student.setFirstName(firstName);
            student.setGroupId(selectedGroupId);

        } else {
            // L'ordre dans le groupe est attribué par le repository.
            student = new Student(
                    lastName,
                    firstName,
                    selectedGroupId,
                    0
            );
        }

        student.setLastNameArabic(
                lastNameArabic.isEmpty() ? null : lastNameArabic);
        student.setFirstNameArabic(
                firstNameArabic.isEmpty() ? null : firstNameArabic);
        student.setMatricule(matricule.isEmpty() ? null : matricule);
        student.setEmail(email.isEmpty() ? null : email);
        student.setNotes(notes.isEmpty() ? null : notes);
        student.setCardImageUri(
                cardImageUri == null ? null : cardImageUri.toString());

        saving = true;
        setFormEnabled(false);

        if (isEditMode) {
            viewModel.updateStudent(student);
        } else {
            viewModel.addStudent(student);
        }
    }

    private void clearErrors() {
        tilLastName.setError(null);
        tilFirstName.setError(null);
        tilMatricule.setError(null);
        tilGroup.setError(null);
        tilEmail.setError(null);
    }

    private void setFormEnabled(boolean enabled) {
        if (etLastName != null) etLastName.setEnabled(enabled);
        if (etFirstName != null) etFirstName.setEnabled(enabled);
        if (etLastNameArabic != null) etLastNameArabic.setEnabled(enabled);
        if (etFirstNameArabic != null) etFirstNameArabic.setEnabled(enabled);
        if (etMatricule != null) etMatricule.setEnabled(enabled);
        if (etEmail != null) etEmail.setEnabled(enabled);
        if (etNotes != null) etNotes.setEnabled(enabled);
        if (actGroup != null) actGroup.setEnabled(enabled);

        if (btnSave != null) {
            btnSave.setEnabled(enabled && !saving);
        }

        if (btnCancel != null) {
            btnCancel.setEnabled(!saving);
        }
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null
                ? ""
                : editText.getText().toString().trim();
    }

    private String getText(AutoCompleteTextView editText) {
        return editText.getText() == null
                ? ""
                : editText.getText().toString().trim();
    }

    // --------------------------------------------------
    // CARTE ÉTUDIANT (inchangé)
    // --------------------------------------------------

    private void showCardImage() {
        if (cardImageUri == null) {
            Toast.makeText(
                    requireContext(),
                    "Aucune image de carte à afficher.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Context context = requireContext();

        PhotoView photoView = new PhotoView(context);
        photoView.setImageURI(cardImageUri);
        photoView.setMinimumScale(1f);
        photoView.setMediumScale(2f);
        photoView.setMaximumScale(5f);
        photoView.setBackgroundColor(Color.BLACK);

        photoView.setLayoutParams(
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        FrameLayout container = new FrameLayout(context);
        container.setBackgroundColor(Color.BLACK);

        int padding = (int) (
                8 * getResources().getDisplayMetrics().density
        );

        container.setPadding(padding, padding, padding, padding);
        container.addView(photoView);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Carte d’étudiant")
                .setView(container)
                .setPositiveButton("Fermer", (d, which) -> d.dismiss())
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.BLACK)
            );

            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
    }

    private void startDocumentScanner() {
        GmsDocumentScannerOptions options =
                new GmsDocumentScannerOptions.Builder()
                        .setGalleryImportAllowed(true)
                        .setPageLimit(1)
                        .setResultFormats(
                                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
                        )
                        .setScannerMode(
                                GmsDocumentScannerOptions.SCANNER_MODE_FULL
                        )
                        .build();

        GmsDocumentScanner scanner =
                GmsDocumentScanning.getClient(options);

        scanner.getStartScanIntent(requireActivity())
                .addOnSuccessListener(intentSender ->
                        scannerLauncher.launch(
                                new IntentSenderRequest.Builder(intentSender)
                                        .build()
                        )
                )
                .addOnFailureListener(error ->
                        Toast.makeText(
                                requireContext(),
                                "Impossible de démarrer le scanner : "
                                        + error.getLocalizedMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    private void saveScannedImageLocally(Uri sourceUri) {
        if (sourceUri == null) {
            return;
        }

        File imageDirectory = new File(
                requireContext().getFilesDir(),
                "student_cards"
        );

        if (!imageDirectory.exists() && !imageDirectory.mkdirs()) {
            Toast.makeText(
                    requireContext(),
                    "Impossible de créer le dossier des cartes.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        File destination = new File(
                imageDirectory,
                "card_" + System.currentTimeMillis() + ".jpg"
        );

        try (
                InputStream input = requireContext()
                        .getContentResolver()
                        .openInputStream(sourceUri);
                FileOutputStream output = new FileOutputStream(destination)
        ) {
            if (input == null) {
                throw new IOException("Image source inaccessible.");
            }

            byte[] buffer = new byte[8192];
            int count;

            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }

            output.flush();

            cardImageUri = Uri.fromFile(destination);
            displayCardImage();

            Toast.makeText(
                    requireContext(),
                    "Carte numérisée et enregistrée.",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (IOException | SecurityException error) {
            if (destination.exists()) {
                // Nettoyer le fichier incomplet en cas d'échec.
                //noinspection ResultOfMethodCallIgnored
                destination.delete();
            }

            Toast.makeText(
                    requireContext(),
                    "Erreur lors de l'enregistrement de la carte.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void displayCardImage() {
        if (cardImageUri == null || ivStudentCard == null) {
            return;
        }

        try {
            ivStudentCard.setImageURI(null);
            ivStudentCard.setImageURI(cardImageUri);

            ivStudentCard.setVisibility(View.VISIBLE);
            layoutCardEmpty.setVisibility(View.GONE);
            layoutCardImageActions.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Toast.makeText(
                    requireContext(),
                    "Impossible d'afficher cette image.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void removeCardImage() {
        cardImageUri = null;

        ivStudentCard.setImageDrawable(null);
        ivStudentCard.setVisibility(View.GONE);
        layoutCardEmpty.setVisibility(View.VISIBLE);
        layoutCardImageActions.setVisibility(View.GONE);
    }

    private void closeForm() {
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

        tilLastName = null;
        tilFirstName = null;
        tilMatricule = null;
        tilGroup = null;
        tilEmail = null;

        etLastName = null;
        etFirstName = null;
        etLastNameArabic = null;
        etFirstNameArabic = null;
        etMatricule = null;
        etEmail = null;
        etNotes = null;

        actGroup = null;
        tvGroupOrder = null;
        ivStudentCard = null;
        layoutCardEmpty = null;
        layoutCardImageActions = null;
        btnSave = null;
        btnCancel = null;
        groupAdapter = null;
    }
}