package com.example.praticum.ui.sessions;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.example.praticum.data.entity.Module;
import com.example.praticum.data.entity.Session;
import com.example.praticum.data.entity.SessionCriterion;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class SessionFormFragment extends Fragment {

    private SessionViewModel viewModel;

    private Session sessionEnEdition;

    private SessionViewModel.SessionDetails detailsEnEdition;

    private int sessionId = -1;

    private boolean modeEdition = false;
    private boolean modulesCharges = false;
    private boolean formulaireRempli = false;

    private EditText titleInput;
    private AutoCompleteTextView typeInput;
    private AutoCompleteTextView moduleInput;
    private EditText descriptionInput;

    private TextInputLayout titleLayout;
    private TextInputLayout typeLayout;
    private TextInputLayout moduleLayout;

    private SessionCriterionAdapter criterionAdapter;
    private TextView totalView;

    private ArrayAdapter<String> moduleAdapter;

    private List<Module> availableModules = new ArrayList<>();

    public SessionFormFragment() {
        // Constructeur public requis par Android.
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

        viewModel = new ViewModelProvider(this)
                .get(SessionViewModel.class);

        titleInput = view.findViewById(
                R.id.etSessionTitle
        );

        typeInput = view.findViewById(
                R.id.actActivityType
        );

        moduleInput = view.findViewById(
                R.id.actSessionModule
        );

        descriptionInput = view.findViewById(
                R.id.etSessionDescription
        );

        titleLayout = view.findViewById(
                R.id.tilSessionTitle
        );

        typeLayout = view.findViewById(
                R.id.tilActivityType
        );

        moduleLayout = view.findViewById(
                R.id.tilSessionModule
        );

        totalView = view.findViewById(
                R.id.tvSessionCriteriaTotal
        );

        Bundle args = getArguments();

        sessionId = args == null
                ? -1
                : args.getInt("session_id", -1);

        modeEdition = sessionId > 0;

        setupDropdowns();
        configurerEntete(view);
        configurerCriteres(view);
        configurerBoutons(view);
        observerDonnees();

        viewModel.loadModules();

        if (modeEdition) {
            viewModel.loadSession(sessionId);
        }
    }

    private void configurerEntete(@NonNull View view) {
        CommonHeaderHelper.configurerEnteteSousFenetre(
                view,
                modeEdition
                        ? "Modifier la séance"
                        : "Nouvelle séance",
                modeEdition
                        ? "Mettre à jour les informations de la séance"
                        : "Renseignez les informations de la séance",
                requireActivity()
        );
    }

    private void setupDropdowns() {
        String[] types = {
                Session.TYPE_COURS,
                Session.TYPE_TD,
                Session.TYPE_TP,
                Session.TYPE_ATELIER,
                Session.TYPE_EVALUATION
        };

        typeInput.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        types
                )
        );

        moduleAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );

        moduleInput.setAdapter(moduleAdapter);
    }

    private void configurerCriteres(View view) {
        RecyclerView recyclerView = view.findViewById(
                R.id.rvSessionCriteria
        );

        if (recyclerView == null) {
            Toast.makeText(
                    requireContext(),
                    "Le composant rvSessionCriteria est absent du XML",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        recyclerView.setNestedScrollingEnabled(false);

        criterionAdapter = new SessionCriterionAdapter(
                this::actualiserTotal
        );

        recyclerView.setAdapter(criterionAdapter);

        View addButton = view.findViewById(
                R.id.btnAddSessionCriterion
        );

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

        View templatesButton = view.findViewById(
                R.id.btnLoadCriterionTemplates
        );

        if (templatesButton != null) {
            templatesButton.setOnClickListener(
                    v -> afficherModelesCriteres()
            );
        }

        actualiserTotal();
    }

    private void configurerBoutons(View view) {
        View saveButton = view.findViewById(
                R.id.btnSaveSession
        );

        if (saveButton != null) {
            saveButton.setOnClickListener(
                    v -> saveSession()
            );
        }

        View cancelButton = view.findViewById(
                R.id.btnCancelSessionForm
        );

        if (cancelButton != null) {
            cancelButton.setOnClickListener(
                    v -> getParentFragmentManager().popBackStack()
            );
        }
    }

    private void observerDonnees() {
        viewModel.getModules().observe(
                getViewLifecycleOwner(),
                modules -> {
                    availableModules = modules == null
                            ? new ArrayList<>()
                            : new ArrayList<>(modules);

                    moduleAdapter.clear();

                    for (Module module : availableModules) {
                        moduleAdapter.add(
                                libelleModule(module)
                        );
                    }

                    moduleAdapter.notifyDataSetChanged();

                    modulesCharges = true;

                    remplirFormulaireSiPret();
                }
        );

        viewModel.getSessionDetails().observe(
                getViewLifecycleOwner(),
                details -> {
                    if (!modeEdition || !isAdded()) {
                        return;
                    }

                    if (details == null) {
                        Toast.makeText(
                                requireContext(),
                                "Séance introuvable",
                                Toast.LENGTH_SHORT
                        ).show();

                        getParentFragmentManager().popBackStack();
                        return;
                    }

                    detailsEnEdition = details;

                    remplirFormulaireSiPret();
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

    /**
     * Le chargement de la séance et celui des modules sont asynchrones.
     * En édition, le formulaire attend donc que les deux soient disponibles.
     */
    private void remplirFormulaireSiPret() {
        if (!modeEdition
                || formulaireRempli
                || !modulesCharges
                || detailsEnEdition == null
                || criterionAdapter == null) {

            return;
        }

        sessionEnEdition = detailsEnEdition.getSession();

        titleInput.setText(
                sessionEnEdition.getTitle()
        );

        typeInput.setText(
                sessionEnEdition.getActivityType(),
                false
        );

        Module module = trouverModuleParId(
                sessionEnEdition.getModuleId()
        );

        moduleInput.setText(
                module == null
                        ? ""
                        : libelleModule(module),
                false
        );

        descriptionInput.setText(
                sessionEnEdition.getDescription()
        );

        /*
         * On conserve les id, sessionId et position des entités existantes.
         * Ces identifiants seront utilisés par le ViewModel pour effectuer
         * des mises à jour au lieu de supprimer et recréer tous les critères.
         */
        List<SessionCriterion> criteresFormulaire =
                new ArrayList<>();

        for (SessionCriterion criterion
                : detailsEnEdition.getCriteria()) {

            SessionCriterion copie = new SessionCriterion(
                    criterion.getSessionId(),
                    criterion.getTitle(),
                    criterion.getMaxScore(),
                    criterion.getPosition()
            );

            copie.setId(criterion.getId());
            copie.setDescription(criterion.getDescription());

            criteresFormulaire.add(copie);
        }

        criterionAdapter.setCriteria(criteresFormulaire);

        formulaireRempli = true;

        actualiserTotal();
    }

    private Module trouverModuleParId(int moduleId) {
        for (Module module : availableModules) {
            if (module != null && module.getId() == moduleId) {
                return module;
            }
        }

        return null;
    }

    private Module trouverModuleParLibelle(String libelle) {
        if (libelle == null) {
            return null;
        }

        for (Module module : availableModules) {
            if (module != null
                    && libelleModule(module).equals(libelle)) {
                return module;
            }
        }

        return null;
    }

    private String libelleModule(Module module) {
        String nom = module.getName() == null
                ? ""
                : module.getName().trim();

        String code = module.getCode() == null
                ? ""
                : module.getCode().trim();

        String codeAffiche = code.isEmpty()
                ? ""
                : " (" + code + ")";

        return nom
                + codeAffiche
                + " — niveau "
                + module.getLevelId();
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
                .setPositiveButton(
                        "Ajouter",
                        (dialog, which) -> {
                            if (criterionAdapter == null) {
                                return;
                            }

                            List<SessionCriterion> nouveaux =
                                    new ArrayList<>();

                            int positionInitiale =
                                    criterionAdapter.getItemCount();

                            for (int i = 0; i < noms.length; i++) {
                                if (!selection[i]) {
                                    continue;
                                }

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

                                /*
                                 * id = 0 : nouveau critère non persisté.
                                 * sessionId = 0 : la séance peut ne pas
                                 * encore exister en base.
                                 */
                                nouveaux.add(
                                        new SessionCriterion(
                                                0,
                                                noms[i],
                                                score,
                                                positionInitiale
                                                        + nouveaux.size()
                                        )
                                );
                            }

                            criterionAdapter.addCriteria(nouveaux);
                        }
                )
                .show();
    }

    private void saveSession() {
        if (modeEdition && sessionEnEdition == null) {
            Toast.makeText(
                    requireContext(),
                    "Le chargement de la séance n'est pas terminé.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (criterionAdapter == null) {
            Toast.makeText(
                    requireContext(),
                    "Impossible de charger les critères du formulaire.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String title = titleInput.getText()
                .toString()
                .trim();

        String type = typeInput.getText()
                .toString()
                .trim();

        String libelleModuleSaisi = moduleInput.getText()
                .toString()
                .trim();

        String description = descriptionInput.getText()
                .toString()
                .trim();

        titleLayout.setError(null);
        typeLayout.setError(null);
        moduleLayout.setError(null);

        if (TextUtils.isEmpty(title)) {
            titleLayout.setError(
                    "Le titre est obligatoire"
            );

            titleInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(type)) {
            typeLayout.setError(
                    "Choisissez un type d'activité"
            );

            typeInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(libelleModuleSaisi)) {
            moduleLayout.setError("Choisissez un module");

            moduleInput.requestFocus();
            return;
        }

        Module moduleSelectionne = trouverModuleParLibelle(
                libelleModuleSaisi
        );

        if (moduleSelectionne == null) {
            moduleLayout.setError(
                    "Choisissez un module existant dans la liste"
            );

            moduleInput.requestFocus();
            return;
        }

        List<SessionCriterion> criteria =
                criterionAdapter.getCriteria();

        for (int i = 0; i < criteria.size(); i++) {
            SessionCriterion criterion = criteria.get(i);

            if (criterion.getTitle() == null
                    || criterion.getTitle().trim().isEmpty()) {

                Toast.makeText(
                        requireContext(),
                        "Veuillez saisir le titre du critère "
                                + (i + 1),
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            if (!Double.isFinite(criterion.getMaxScore())
                    || criterion.getMaxScore() <= 0) {

                Toast.makeText(
                        requireContext(),
                        "Le score maximal du critère "
                                + (i + 1)
                                + " doit être supérieur à zéro",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }
        }

        Session session;

        if (modeEdition) {
            session = sessionEnEdition;

            session.setTitle(title);
            session.setActivityType(type);
            session.setModuleId(moduleSelectionne.getId());

        } else {
            session = new Session(
                    title,
                    type,
                    moduleSelectionne.getId()
            );
        }

        session.setDescription(description);

        viewModel.saveSession(session, criteria);
    }

    @Override
    public void onDestroyView() {
        titleInput = null;
        typeInput = null;
        moduleInput = null;
        descriptionInput = null;

        titleLayout = null;
        typeLayout = null;
        moduleLayout = null;

        totalView = null;
        criterionAdapter = null;
        moduleAdapter = null;

        super.onDestroyView();
    }
}