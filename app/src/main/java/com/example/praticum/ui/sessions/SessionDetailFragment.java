
package com.example.praticum.ui.sessions;

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

import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.example.praticum.ui.attendance.AttendanceSheetFragment;
import com.example.praticum.ui.evaluation.EvaluationSheetFragment;
import com.example.praticum.ui.evaluation.SessionEvaluationsFragment;

public class SessionDetailFragment extends Fragment {

    private SessionRepositoryMemory repository;
    private SessionMemory session;

    private TextView titleView;
    private TextView statusView;
    private TextView typeView;
    private TextView moduleView;
    private TextView descriptionView;
    private TextView attendanceSummaryView;

    private TextView tvSessionDetailCriteriaCount;

    public SessionDetailFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_session_detail,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        repository = SessionRepositoryMemory.getInstance();

        titleView = view.findViewById(R.id.tvSessionDetailTitle);
        statusView = view.findViewById(R.id.tvSessionDetailStatus);
        typeView = view.findViewById(R.id.tvSessionDetailType);
        moduleView = view.findViewById(R.id.tvSessionDetailModule);
        descriptionView =
                view.findViewById(R.id.tvSessionDetailDescription);
        attendanceSummaryView =
                view.findViewById(R.id.tvSessionDetailAttendanceSummary);
        tvSessionDetailCriteriaCount =
                view.findViewById(R.id.tvSessionDetailCriteriaCount);


        Bundle args = getArguments();
        int sessionId = args == null
                ? -1
                : args.getInt("session_id", -1);

        session = repository.getSessionById(sessionId);

        if (session == null) {
            Toast.makeText(
                    requireContext(),
                    "Séance introuvable",
                    Toast.LENGTH_SHORT
            ).show();

            getParentFragmentManager().popBackStack();
            return;
        }

        configurerEntete(view);
        configurerActions(view);
        displaySession();
    }

    private void configurerEntete(@NonNull View view) {
        CommonHeaderHelper.configurerEnteteSousFenetre(
                view,
                "Détails de la séance",
                session.getTitle(),
                requireActivity()
        );
    }

    private void configurerActions(@NonNull View view) {
        View editButton =
                view.findViewById(R.id.btnEditSessionDetail);
        View deleteButton =
                view.findViewById(R.id.btnDeleteSessionDetail);
        View archiveButton =
                view.findViewById(R.id.btnArchiveSession);
        View attendanceButton =
                view.findViewById(R.id.btnOpenSessionAttendance);
        View startEvaluationButton =
                view.findViewById(R.id.btnStartEvaluation);
        View viewEvaluationButton =
                view.findViewById(R.id.btnViewEvaluation);

        if (editButton != null) {
            editButton.setOnClickListener(v -> openEditForm());
        }

        if (deleteButton != null) {
            deleteButton.setOnClickListener(v -> confirmDelete());
        }

        if (archiveButton != null) {
            archiveButton.setOnClickListener(v -> archiveSession());
        }

        if (attendanceButton != null) {
            attendanceButton.setOnClickListener(v -> ouvrirFeuillePresence());
        }

        if (startEvaluationButton != null) {
            startEvaluationButton.setOnClickListener(v -> ouvrirFicheEvaluation());
        }

        if (viewEvaluationButton != null) {
            viewEvaluationButton.setOnClickListener(v -> ouvrirConsultationEvaluations());
        }
    }


    private void ouvrirConsultationEvaluations() {
        if (session == null) {
            return;
        }

        SessionEvaluationsFragment fragment = new SessionEvaluationsFragment();

        Bundle args = new Bundle();
        args.putInt("session_id", session.getId());
        args.putString("session_title", session.getTitle());
        fragment.setArguments(args);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.mainContainer, fragment)
                .addToBackStack("session_evaluations")
                .commit();
    }


    private void displaySession() {
        if (session == null) {
            return;
        }

        titleView.setText(valeurOu(session.getTitle(), "Séance sans titre"));
        typeView.setText(valeurOu(session.getActivityType(), "Type non renseigné"));
        moduleView.setText(valeurOu(session.getModule(), "Module non renseigné"));

        statusView.setText(
                session.isArchived()
                        ? "Archivée"
                        : valeurOu(session.getStatus(), "À venir")
        );

        String description = session.getDescription();
        descriptionView.setText(
                description == null || description.trim().isEmpty()
                        ? "Aucune description renseignée."
                        : description
        );

        if (attendanceSummaryView != null) {
            attendanceSummaryView.setText(
                    "Suivez les présences des étudiants pour cette séance."
            );
        }

        int nombreCriteres = session.getCriteria().size();

        tvSessionDetailCriteriaCount.setText(String.valueOf(nombreCriteres));
    }

    private String valeurOu(String value, String fallback) {
        return value == null || value.trim().isEmpty()
                ? fallback
                : value;
    }

    private void ouvrirFicheEvaluation() {
        EvaluationSheetFragment fragment = new EvaluationSheetFragment();

        Bundle args = new Bundle();
        args.putInt("session_id", session.getId());
        args.putString("session_title", session.getTitle());
        fragment.setArguments(args);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.mainContainer, fragment)
                .addToBackStack("session_evaluation")
                .commit();
    }

    private void ouvrirFeuillePresence() {
        AttendanceSheetFragment fragment = new AttendanceSheetFragment();

        Bundle args = new Bundle();
        args.putInt("session_id", session.getId());
        args.putString("session_title", session.getTitle());
        fragment.setArguments(args);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.mainContainer, fragment)
                .addToBackStack("session_attendance")
                .commit();
    }

    private void openEditForm() {
        SessionFormFragment fragment = new SessionFormFragment();

        Bundle args = new Bundle();
        args.putInt("session_id", session.getId());
        fragment.setArguments(args);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.mainContainer, fragment)
                .addToBackStack("session_form")
                .commit();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Supprimer la séance")
                .setMessage("Voulez-vous vraiment supprimer cette séance ?")
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    repository.deleteSession(session.getId());

                    Toast.makeText(
                            requireContext(),
                            "Séance supprimée",
                            Toast.LENGTH_SHORT
                    ).show();

                    getParentFragmentManager().popBackStack();
                })
                .show();
    }

    private void archiveSession() {
        if (session.isArchived()) {
            showTemporaryMessage("Cette séance est déjà archivée");
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Archiver la séance")
                .setMessage("Voulez-vous archiver cette séance ?")
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Archiver", (dialog, which) -> {
                    repository.archiveSession(session.getId());

                    Toast.makeText(
                            requireContext(),
                            "Séance archivée",
                            Toast.LENGTH_SHORT
                    ).show();

                    getParentFragmentManager().popBackStack();
                })
                .show();
    }

    private void showTemporaryMessage(String message) {
        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}
