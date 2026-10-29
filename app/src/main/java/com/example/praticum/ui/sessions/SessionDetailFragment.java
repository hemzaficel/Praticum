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
import androidx.lifecycle.ViewModelProvider;

import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.example.praticum.data.entity.Session;
import com.example.praticum.ui.attendance.AttendanceSheetFragment;
import com.example.praticum.ui.evaluation.EvaluationSheetFragment;
import com.example.praticum.ui.evaluation.SessionEvaluationsFragment;

public class SessionDetailFragment extends Fragment {

    private SessionViewModel viewModel;

    private SessionViewModel.SessionDetails sessionDetails;

    private View rootView;

    private TextView titleView;
    private TextView statusView;
    private TextView typeView;
    private TextView moduleView;
    private TextView descriptionView;
    private TextView attendanceSummaryView;
    private TextView tvSessionDetailCriteriaCount;

    private int sessionId = -1;
    private boolean alreadyResumed = false;

    public SessionDetailFragment() {
        // Constructeur public requis par Android.
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

        rootView = view;

        viewModel = new ViewModelProvider(this)
                .get(SessionViewModel.class);

        titleView = view.findViewById(
                R.id.tvSessionDetailTitle
        );

        statusView = view.findViewById(
                R.id.tvSessionDetailStatus
        );

        typeView = view.findViewById(
                R.id.tvSessionDetailType
        );

        moduleView = view.findViewById(
                R.id.tvSessionDetailModule
        );

        descriptionView = view.findViewById(
                R.id.tvSessionDetailDescription
        );

        attendanceSummaryView = view.findViewById(
                R.id.tvSessionDetailAttendanceSummary
        );

        tvSessionDetailCriteriaCount = view.findViewById(
                R.id.tvSessionDetailCriteriaCount
        );

        Bundle args = getArguments();

        sessionId = args == null
                ? -1
                : args.getInt("session_id", -1);

        configurerActions(view);
        observerDonnees();

        if (sessionId > 0) {
            viewModel.loadSession(sessionId);
        } else {
            afficherSeanceIntrouvable();
        }
    }

    private void observerDonnees() {
        viewModel.getSessionDetails().observe(
                getViewLifecycleOwner(),
                details -> {
                    if (!isAdded()) {
                        return;
                    }

                    if (details == null) {
                        afficherSeanceIntrouvable();
                        return;
                    }

                    sessionDetails = details;

                    configurerEntete();
                    afficherSeance();
                }
        );

        viewModel.getOperationResult().observe(
                getViewLifecycleOwner(),
                resultat -> {
                    if (resultat == null) {
                        return;
                    }

                    viewModel.clearOperationResult();

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            resultat.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();

                    if (resultat.isSuccess()) {
                        getParentFragmentManager().popBackStack();
                    }
                }
        );
    }

    private void configurerEntete() {
        if (rootView == null || sessionDetails == null) {
            return;
        }

        CommonHeaderHelper.configurerEnteteSousFenetre(
                rootView,
                "Détails de la séance",
                sessionDetails.getSession().getTitle(),
                requireActivity()
        );
    }

    private void configurerActions(@NonNull View view) {
        View editButton = view.findViewById(
                R.id.btnEditSessionDetail
        );

        View deleteButton = view.findViewById(
                R.id.btnDeleteSessionDetail
        );

        View archiveButton = view.findViewById(
                R.id.btnArchiveSession
        );

        View attendanceButton = view.findViewById(
                R.id.btnOpenSessionAttendance
        );

        View startEvaluationButton = view.findViewById(
                R.id.btnStartEvaluation
        );

        View viewEvaluationButton = view.findViewById(
                R.id.btnViewEvaluation
        );

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
            attendanceButton.setOnClickListener(
                    v -> ouvrirFeuillePresence()
            );
        }

        if (startEvaluationButton != null) {
            startEvaluationButton.setOnClickListener(
                    v -> ouvrirFicheEvaluation()
            );
        }

        if (viewEvaluationButton != null) {
            viewEvaluationButton.setOnClickListener(
                    v -> ouvrirConsultationEvaluations()
            );
        }
    }

    private void afficherSeance() {
        if (sessionDetails == null) {
            return;
        }

        Session session = sessionDetails.getSession();

        titleView.setText(
                valeurOu(session.getTitle(), "Séance sans titre")
        );

        typeView.setText(
                valeurOu(
                        session.getActivityType(),
                        "Type non renseigné"
                )
        );

        moduleView.setText(
                valeurOu(
                        sessionDetails.getModuleName(),
                        "Module non renseigné"
                )
        );

        statusView.setText(
                session.isArchived()
                        ? "Archivée"
                        : valeurOu(session.getStatus(), "À venir")
        );

        descriptionView.setText(
                valeurOu(
                        session.getDescription(),
                        "Aucune description renseignée."
                )
        );

        if (attendanceSummaryView != null) {
            attendanceSummaryView.setText(
                    "Suivez les présences des étudiants pour cette séance."
            );
        }

        if (tvSessionDetailCriteriaCount != null) {
            tvSessionDetailCriteriaCount.setText(
                    String.valueOf(
                            sessionDetails.getCriteria().size()
                    )
            );
        }
    }

    private String valeurOu(String value, String fallback) {
        return value == null || value.trim().isEmpty()
                ? fallback
                : value;
    }

    private Session sessionActuelle() {
        return sessionDetails == null
                ? null
                : sessionDetails.getSession();
    }

    private void ouvrirFicheEvaluation() {
        Session session = sessionActuelle();

        if (session == null) {
            return;
        }

        EvaluationSheetFragment fragment =
                new EvaluationSheetFragment();

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
        Session session = sessionActuelle();

        if (session == null) {
            return;
        }

        AttendanceSheetFragment fragment =
                new AttendanceSheetFragment();

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

    private void ouvrirConsultationEvaluations() {
        Session session = sessionActuelle();

        if (session == null) {
            return;
        }

        SessionEvaluationsFragment fragment =
                new SessionEvaluationsFragment();

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

    private void openEditForm() {
        Session session = sessionActuelle();

        if (session == null) {
            return;
        }

        SessionFormFragment fragment =
                new SessionFormFragment();

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
        Session session = sessionActuelle();

        if (session == null) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Supprimer la séance")
                .setMessage(
                        "Voulez-vous vraiment supprimer cette séance ?"
                )
                .setNegativeButton(
                        "Annuler",
                        (dialog, which) -> dialog.dismiss()
                )
                .setPositiveButton(
                        "Supprimer",
                        (dialog, which) ->
                                viewModel.deleteSession(session.getId())
                )
                .show();
    }

    private void archiveSession() {
        Session session = sessionActuelle();

        if (session == null) {
            return;
        }

        if (session.isArchived()) {
            afficherMessage(
                    "Cette séance est déjà archivée"
            );
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Archiver la séance")
                .setMessage(
                        "Voulez-vous archiver cette séance ?"
                )
                .setNegativeButton(
                        "Annuler",
                        (dialog, which) -> dialog.dismiss()
                )
                .setPositiveButton(
                        "Archiver",
                        (dialog, which) ->
                                viewModel.archiveSession(session.getId())
                )
                .show();
    }

    private void afficherMessage(String message) {
        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void afficherSeanceIntrouvable() {
        if (!isAdded()) {
            return;
        }

        Toast.makeText(
                requireContext(),
                "Séance introuvable",
                Toast.LENGTH_SHORT
        ).show();

        getParentFragmentManager().popBackStack();
    }

    @Override
    public void onResume() {
        super.onResume();

        /*
         * Le premier chargement est effectué dans onViewCreated().
         * Lorsqu'on revient du formulaire, on relit les dernières données.
         */
        if (alreadyResumed && viewModel != null && sessionId > 0) {
            viewModel.loadSession(sessionId);
        }

        alreadyResumed = true;
    }

    @Override
    public void onDestroyView() {
        rootView = null;

        titleView = null;
        statusView = null;
        typeView = null;
        moduleView = null;
        descriptionView = null;
        attendanceSummaryView = null;
        tvSessionDetailCriteriaCount = null;

        super.onDestroyView();
    }
}