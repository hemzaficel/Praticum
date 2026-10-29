
package com.example.praticum.ui.sessions;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.google.android.material.textfield.TextInputLayout;

import android.app.AlertDialog;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SessionFormFragment extends Fragment {

    private SessionRepositoryMemory repository;
    private SessionMemory editingSession;

    private EditText titleInput;
    private AutoCompleteTextView typeInput;
    private AutoCompleteTextView moduleInput;
    private EditText descriptionInput;

    private TextInputLayout titleLayout;
    private TextInputLayout typeLayout;
    private TextInputLayout moduleLayout;

    private SessionCriterionAdapter criterionAdapter;
    private TextView totalView;

    public SessionFormFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_session_form,
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

        titleInput = view.findViewById(R.id.etSessionTitle);
        typeInput = view.findViewById(R.id.actActivityType);
        moduleInput = view.findViewById(R.id.actSessionModule);
        descriptionInput =
                view.findViewById(R.id.etSessionDescription);

        titleLayout = view.findViewById(R.id.tilSessionTitle);
        typeLayout = view.findViewById(R.id.tilActivityType);
        moduleLayout = view.findViewById(R.id.tilSessionModule);

        totalView = view.findViewById(R.id.tvSessionCriteriaTotal);

        Bundle args = getArguments();
        int sessionId = args == null
                ? -1
                : args.getInt("session_id", -1);

        if (sessionId > 0) {
            editingSession = repository.getSessionById(sessionId);
        }

        setupDropdowns();
        configurerEntete(view);
        configurerCriteres(view);

        if (editingSession != null) {
            fillForm(editingSession);
            criterionAdapter.setCriteria(editingSession.getCriteria());
        }

        View saveButton = view.findViewById(R.id.btnSaveSession);
        if (saveButton != null) {
            saveButton.setOnClickListener(v -> saveSession());
        }

        View cancelButton =
                view.findViewById(R.id.btnCancelSessionForm);

        if (cancelButton != null) {
            cancelButton.setOnClickListener(
                    v -> getParentFragmentManager().popBackStack()
            );
        }
    }

    private void configurerEntete(@NonNull View view) {
        CommonHeaderHelper.configurerEnteteSousFenetre(
                view,
                editingSession == null
                        ? "Nouvelle séance"
                        : "Modifier la séance",
                editingSession == null
                        ? "Renseignez les informations de la séance"
                        : "Mettre à jour les informations de la séance",
                requireActivity()
        );
    }

    private void setupDropdowns() {
        String[] types = {
                "Cours", "TD", "TP", "Atelier", "Évaluation"
        };

        typeInput.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                types
        ));

        String[] modules = {
                "Informatique",
                "Programmation",
                "Algorithmique",
                "Mathématiques"
        };

        moduleInput.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                modules
        ));
    }

    private void configurerCriteres(View view) {
        RecyclerView recyclerView =
                view.findViewById(R.id.rvSessionCriteria);

        if (recyclerView == null) {
            Toast.makeText(
                    requireContext(),
                    "Le composant rvSessionCriteria est absent du XML",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setNestedScrollingEnabled(false);

        criterionAdapter = new SessionCriterionAdapter(
                this::actualiserTotal
        );
        recyclerView.setAdapter(criterionAdapter);

        View addButton = view.findViewById(R.id.btnAddSessionCriterion);
        if (addButton != null) {
            addButton.setOnClickListener(v -> {
                criterionAdapter.addCriterion();
                recyclerView.post(() ->
                        recyclerView.smoothScrollToPosition(
                                criterionAdapter.getItemCount() - 1
                        )
                );
            });
        }

        View templatesButton =
                view.findViewById(R.id.btnLoadCriterionTemplates);

        if (templatesButton != null) {
            templatesButton.setOnClickListener(
                    v -> afficherModelesCriteres()
            );
        }

        actualiserTotal();
    }

    private void actualiserTotal() {
        if (criterionAdapter == null || totalView == null) {
            return;
        }

        double total = criterionAdapter.getTotalScore();

        String totalTexte = total == Math.floor(total)
                ? String.valueOf((int) total)
                : String.valueOf(total);

        totalView.setText(totalTexte + " points");
    }

    private void afficherModelesCriteres() {
        String[] noms = {
                "Participation",
                "Maîtrise technique",
                "Travail en équipe",
                "Respect des consignes",
                "Qualité du résultat"
        };

        boolean[] selection = new boolean[noms.length];

        new AlertDialog.Builder(requireContext())
                .setTitle("Modèles de critères")
                .setMultiChoiceItems(
                        noms,
                        selection,
                        (dialog, which, isChecked) ->
                                selection[which] = isChecked
                )
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    List<SessionCriterion> nouveaux = new ArrayList<>();

                    for (int i = 0; i < noms.length; i++) {
                        if (selection[i]) {
                            double score;

                            switch (i) {
                                case 0:
                                    score = 2;
                                    break;
                                case 1:
                                    score = 4;
                                    break;
                                case 2:
                                    score = 2;
                                    break;
                                case 3:
                                    score = 1;
                                    break;
                                default:
                                    score = 1;
                                    break;
                            }

                            nouveaux.add(new SessionCriterion(
                                    noms[i],
                                    "",
                                    score
                            ));
                        }
                    }

                    criterionAdapter.addCriteria(nouveaux);
                })
                .show();
    }

    private void fillForm(@NonNull SessionMemory session) {
        titleInput.setText(session.getTitle());
        typeInput.setText(session.getActivityType(), false);
        moduleInput.setText(session.getModule(), false);
        descriptionInput.setText(session.getDescription());
    }

    private void saveSession() {
        String title = titleInput.getText().toString().trim();
        String type = typeInput.getText().toString().trim();
        String module = moduleInput.getText().toString().trim();
        String description =
                descriptionInput.getText().toString().trim();

        titleLayout.setError(null);
        typeLayout.setError(null);
        moduleLayout.setError(null);

        if (TextUtils.isEmpty(title)) {
            titleLayout.setError("Le titre est obligatoire");
            titleInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(type)) {
            typeLayout.setError("Choisissez un type d'activité");
            typeInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(module)) {
            moduleLayout.setError("Choisissez un module");
            moduleInput.requestFocus();
            return;
        }

        List<SessionCriterion> criteria =
                criterionAdapter == null
                        ? new ArrayList<>()
                        : criterionAdapter.getCriteria();

        for (int i = 0; i < criteria.size(); i++) {
            SessionCriterion criterion = criteria.get(i);

            if (criterion.getTitle() == null
                    || criterion.getTitle().trim().isEmpty()) {

                Toast.makeText(
                        requireContext(),
                        "Veuillez saisir le titre du critère " + (i + 1),
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            if (criterion.getMaxScore() <= 0) {
                Toast.makeText(
                        requireContext(),
                        "Le score maximal du critère "
                                + (i + 1) + " doit être supérieur à zéro",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }
        }

        if (editingSession == null) {
            SessionMemory session = new SessionMemory(
                    0,
                    title,
                    type,
                    module,
                    description,
                    "À venir",
                    false
            );

            session.setCriteria(criteria);
            repository.addSession(session);

            Toast.makeText(
                    requireContext(),
                    "Séance ajoutée",
                    Toast.LENGTH_SHORT
            ).show();

        } else {
            editingSession.setTitle(title);
            editingSession.setActivityType(type);
            editingSession.setModule(module);
            editingSession.setDescription(description);
            editingSession.setCriteria(criteria);

            repository.updateSession(editingSession);

            Toast.makeText(
                    requireContext(),
                    "Séance modifiée",
                    Toast.LENGTH_SHORT
            ).show();
        }

        getParentFragmentManager().popBackStack();
    }
}
