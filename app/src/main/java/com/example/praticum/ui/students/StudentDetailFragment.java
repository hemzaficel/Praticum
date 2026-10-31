package com.example.praticum.ui.students;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.example.praticum.core.common.DeletionConfirmationDialog;
import com.example.praticum.data.entity.Student;
import com.example.praticum.ui.enrollments.EnrollmentsFragment;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class StudentDetailFragment extends Fragment {

    private static final String ARG_STUDENT_ID = "student_id";

    private StudentViewModel viewModel;

    private String studentId = "";
    private Student student;
    private boolean deleting = false;

    public StudentDetailFragment() {
        // Constructeur obligatoire.
    }

    public static StudentDetailFragment newInstance(String studentId) {
        StudentDetailFragment fragment = new StudentDetailFragment();

        Bundle args = new Bundle();
        args.putString(ARG_STUDENT_ID, studentId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            studentId = getArguments().getString(ARG_STUDENT_ID, "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_student_detail,
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

        CommonHeaderHelper.configurerEnteteSousFenetre(
                view,
                "Fiche étudiant",
                "Informations et suivi pédagogique",
                requireActivity()
        );

        displayEmptyStatistics(view);
        configureButtons(view);
        observeData(view);

        if (isEmpty(studentId)) {
            Toast.makeText(
                    requireContext(),
                    "Identifiant d'étudiant invalide.",
                    Toast.LENGTH_SHORT
            ).show();

            goBack();
            return;
        }

        viewModel.loadStudentById(studentId);
    }

    private void observeData(View view) {
        viewModel.getSelectedStudent().observe(
                getViewLifecycleOwner(),
                loaded -> {
                    if (loaded == null) {
                        return;
                    }

                    student = loaded;

                    displayIdentity(view, loaded);
                    displayUniversityInformation(view);
                    displayContactInformation(view, loaded);
                    displayStudentCard(view, loaded);
                }
        );

        viewModel.getError().observe(
                getViewLifecycleOwner(),
                error -> {
                    if (error != null && !error.trim().isEmpty()) {
                        deleting = false;

                        Toast.makeText(
                                requireContext(),
                                error,
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
                            || !deleting) {
                        return;
                    }

                    deleting = false;

                    if (isAdded()) {
                        Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    goBack();
                }
        );
    }

    // --------------------------------------------------
    // IDENTITÉ
    // --------------------------------------------------

    private void displayIdentity(View view, Student s) {
        TextView initials = view.findViewById(
                R.id.tvStudentDetailInitials
        );

        TextView fullName = view.findViewById(
                R.id.tvStudentDetailFullName
        );

        TextView lastNameAr = view.findViewById(
                R.id.tvStudentDetailLastNameArabic
        );

        TextView firstNameAr = view.findViewById(
                R.id.tvStudentDetailFirstNameArabic
        );

        TextView studentMatricule = view.findViewById(
                R.id.tvStudentDetailMatricule
        );

        String fullNameValue = joinNames(
                s.getFirstName(),
                s.getLastName()
        );

        fullName.setText(
                fullNameValue.isEmpty() ? "Étudiant" : fullNameValue
        );

        initials.setText(getInitials(s.getFirstName(), s.getLastName()));

        studentMatricule.setText(
                isEmpty(s.getMatricule())
                        ? "Non renseigné"
                        : s.getMatricule()
        );

        setOptionalText(lastNameAr, s.getLastNameArabic());
        setOptionalText(firstNameAr, s.getFirstNameArabic());
    }

    // --------------------------------------------------
    // PARCOURS UNIVERSITAIRE ET COORDONNÉES
    // --------------------------------------------------

    private void displayUniversityInformation(View view) {
        MaterialButton viewEnrollments = view.findViewById(
                R.id.btnStudentViewEnrollments
        );
        viewEnrollments.setOnClickListener(v -> {
            if (!isAdded() || isEmpty(studentId)) return;
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(
                            R.id.mainContainer,
                            EnrollmentsFragment.newInstanceForStudent(studentId)
                    )
                    .addToBackStack("student_enrollments")
                    .commit();
        });
    }

    private void displayContactInformation(View view, Student s) {
        TextView studentEmail = view.findViewById(
                R.id.tvStudentDetailEmail
        );
        TextView studentNotes = view.findViewById(
                R.id.tvStudentDetailNotes
        );
        View emptyContact = view.findViewById(
                R.id.tvStudentDetailContactEmpty
        );

        boolean hasEmail = !isEmpty(s.getEmail());
        boolean hasNotes = !isEmpty(s.getNotes());
        setOptionalText(
                studentEmail,
                hasEmail ? "E-mail : " + s.getEmail() : ""
        );
        setOptionalText(
                studentNotes,
                hasNotes ? "Remarques : " + s.getNotes() : ""
        );
        emptyContact.setVisibility(hasEmail || hasNotes ? View.GONE : View.VISIBLE);
    }

    // --------------------------------------------------
    // CARTE ÉTUDIANT
    // --------------------------------------------------

    private void showStudentCardZoom() {
        String cardImageUri =
                student == null ? null : student.getCardImageUri();

        if (isEmpty(cardImageUri)) {
            Toast.makeText(
                    requireContext(),
                    "Aucune carte d'étudiant disponible.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        PhotoView photoView = new PhotoView(requireContext());
        photoView.setImageURI(Uri.parse(cardImageUri));
        photoView.setMinimumScale(1f);
        photoView.setMediumScale(2f);
        photoView.setMaximumScale(5f);
        photoView.setBackgroundColor(Color.BLACK);

        FrameLayout container = new FrameLayout(requireContext());
        container.setBackgroundColor(Color.BLACK);

        int padding = (int) (
                8 * getResources().getDisplayMetrics().density
        );
        container.setPadding(padding, padding, padding, padding);

        container.addView(
                photoView,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Carte d’étudiant")
                .setView(container)
                .setPositiveButton("Fermer", null)
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

    private void displayStudentCard(View view, Student s) {
        ImageView image = view.findViewById(
                R.id.ivStudentCard
        );

        View emptyLayout = view.findViewById(
                R.id.layoutStudentCardEmpty
        );

        image.setOnClickListener(v -> showStudentCardZoom());

        String cardImageUri = s.getCardImageUri();

        if (isEmpty(cardImageUri)) {
            image.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
            return;
        }

        try {
            image.setImageURI(Uri.parse(cardImageUri));

            if (image.getDrawable() != null) {
                image.setVisibility(View.VISIBLE);
                emptyLayout.setVisibility(View.GONE);
            } else {
                image.setVisibility(View.GONE);
                emptyLayout.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            image.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        }
    }

    // --------------------------------------------------
    // STATISTIQUES PAR DÉFAUT (modules non encore connectés)
    // --------------------------------------------------

    private void displayEmptyStatistics(View view) {
        TextView attendanceRate = view.findViewById(
                R.id.tvStudentAttendanceRate
        );

        TextView presentCount = view.findViewById(
                R.id.tvStudentPresentCount
        );

        TextView absenceCount = view.findViewById(
                R.id.tvStudentAbsenceCount
        );

        TextView lateCount = view.findViewById(
                R.id.tvStudentLateCount
        );

        ProgressBar attendanceProgress = view.findViewById(
                R.id.progressStudentAttendance
        );

        TextView average = view.findViewById(
                R.id.tvStudentAverage
        );

        TextView resultsSummary = view.findViewById(
                R.id.tvStudentResultsSummary
        );

        TextView sessionsProgress = view.findViewById(
                R.id.tvStudentSessionsProgress
        );

        ProgressBar sessionsBar = view.findViewById(
                R.id.progressStudentSessions
        );

        attendanceRate.setText("— %");
        presentCount.setText("—");
        absenceCount.setText("—");
        lateCount.setText("—");
        attendanceProgress.setProgress(0);

        average.setText("Moyenne générale : — / 20");
        resultsSummary.setText("Aucun résultat disponible");

        sessionsProgress.setText("Séances réalisées : — / —");
        sessionsBar.setProgress(0);
    }

    // --------------------------------------------------
    // BOUTONS
    // --------------------------------------------------

    private void configureButtons(View view) {
        MaterialButton btnEditStudent = view.findViewById(
                R.id.btnEditStudentDetails
        );

        MaterialButton btnDeleteStudent = view.findViewById(
                R.id.btnDeleteStudentDetails
        );

        MaterialButton attendanceButton = view.findViewById(
                R.id.btnViewStudentAttendance
        );

        MaterialButton resultsButton = view.findViewById(
                R.id.btnViewStudentResults
        );

        MaterialButton statisticsButton = view.findViewById(
                R.id.btnViewStudentStatistics
        );

        btnEditStudent.setOnClickListener(v -> openEditForm());

        if (btnDeleteStudent != null) {
            btnDeleteStudent.setOnClickListener(
                    v -> confirmDelete()
            );
        }

        attendanceButton.setOnClickListener(v ->
                showModuleNotAvailable("Présences")
        );

        resultsButton.setOnClickListener(v ->
                showModuleNotAvailable("Résultats")
        );

        statisticsButton.setOnClickListener(v ->
                showModuleNotAvailable("Statistiques")
        );
    }

    // --------------------------------------------------
    // MODIFICATION / SUPPRESSION
    // --------------------------------------------------

    private void openEditForm() {
        if (student == null || !isAdded()) {
            Toast.makeText(
                    requireContext(),
                    "Impossible de récupérer les données de l'étudiant.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.mainContainer,
                        StudentFormFragment.newInstance(
                                student.getStudentId()
                        )
                )
                .addToBackStack("student_edit")
                .commit();
    }

    private void confirmDelete() {
        if (student == null || deleting) {
            return;
        }

        DeletionConfirmationDialog.show(
                requireContext(),
                "Supprimer l'étudiant ?",
                "Voulez-vous vraiment supprimer « "
                        + student.getFullName() + " » ?\n\n"
                        + "Ses présences et ses participations aux équipes "
                        + "d'évaluation seront également supprimées.",
                () -> {
                    deleting = true;
                    viewModel.deleteStudent(student);
                }
        );
    }

    private void showModuleNotAvailable(String module) {
        Toast.makeText(
                requireContext(),
                module + " : le module correspondant n'est pas encore connecté.",
                Toast.LENGTH_LONG
        ).show();
    }

    private void goBack() {
        if (isAdded()) {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        }
    }

    // --------------------------------------------------
    // MÉTHODES UTILITAIRES
    // --------------------------------------------------

    private void setOptionalText(TextView textView, @Nullable String value) {
        if (isEmpty(value)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(value);
            textView.setVisibility(View.VISIBLE);
        }
    }

    private boolean isEmpty(@Nullable String value) {
        return value == null || value.trim().isEmpty();
    }

    private String joinNames(String first, String last) {
        String firstValue = first == null ? "" : first.trim();
        String lastValue = last == null ? "" : last.trim();

        return (firstValue + " " + lastValue).trim();
    }

    private String getInitials(String first, String last) {
        String firstValue = first == null ? "" : first.trim();
        String lastValue = last == null ? "" : last.trim();

        StringBuilder result = new StringBuilder();

        if (!firstValue.isEmpty()) {
            result.append(firstValue.charAt(0));
        }

        if (!lastValue.isEmpty()) {
            result.append(lastValue.charAt(0));
        }

        return result.length() == 0
                ? "?"
                : result.toString().toUpperCase(Locale.getDefault());
    }
}



