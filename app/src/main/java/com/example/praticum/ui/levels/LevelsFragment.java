package com.example.praticum.ui.levels;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.MainActivity;
import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.example.praticum.core.common.DeletionConfirmationDialog;
import com.example.praticum.data.entity.Level;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class LevelsFragment extends Fragment {

    private static final String FILTRE_TOUS = "Tous";

    private RecyclerView recyclerLevels;
    private LevelAdapter adapter;
    private TextView tvCount;
    private TextView tvFilteredCount;
    private View emptyState;
    private EditText etSearch;
    private ChipGroup chipGroupCycle;

    private LevelViewModel viewModel;

    private String recherche = "";
    private String filtreCycle = FILTRE_TOUS;

    private List<Level> niveaux = new ArrayList<>();

    public LevelsFragment() {
        // Constructeur obligatoire
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_levels,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        recyclerLevels = view.findViewById(R.id.containerLevels);
        tvCount = view.findViewById(R.id.tvLevelsCount);
        tvFilteredCount = view.findViewById(R.id.tvLevelsFilteredCount);
        emptyState = view.findViewById(R.id.layoutLevelsEmpty);
        etSearch = view.findViewById(R.id.etLevelsSearch);
        chipGroupCycle = view.findViewById(R.id.chipGroupLevelsCycle);

        viewModel = new ViewModelProvider(this)
                .get(LevelViewModel.class);

        recyclerLevels.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        recyclerLevels.setNestedScrollingEnabled(true);

        adapter = new LevelAdapter(
                new LevelAdapter.OnLevelActionListener() {
                    @Override
                    public void onDetails(Level level) {
                        afficherDetails(level);
                    }

                    @Override
                    public void onEdit(Level level) {
                        ouvrirFormulaire(level.getId());
                    }

                    @Override
                    public void onDelete(Level level) {
                        confirmerSuppression(level);
                    }
                }
        );

        recyclerLevels.setAdapter(adapter);

        configurerEntete(view);
        configurerRecherche();
        configurerFiltres();
        observerDonnees();

        viewModel.loadLevels();
    }

    private void configurerEntete(View view) {

        CommonHeaderHelper.configurerEnteteSousFenetre(
                view,
                "Niveaux d’étude",
                "",
                requireActivity()
        );

        View btnAdd = view.findViewById(R.id.btnLevelAdd);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> ouvrirFormulaire(-1));
        }

        View btnEmptyAdd = view.findViewById(R.id.btnLevelsEmptyAdd);
        if (btnEmptyAdd != null) {
            btnEmptyAdd.setOnClickListener(v -> ouvrirFormulaire(-1));
        }
    }

    private void configurerRecherche() {

        if (etSearch == null) {
            return;
        }

        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {

                recherche = s == null ? "" : s.toString().trim();
                actualiserListe();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    private void configurerFiltres() {

        if (chipGroupCycle == null) {
            return;
        }

        chipGroupCycle.setOnCheckedStateChangeListener(
                (group, checkedIds) -> {

                    if (checkedIds.isEmpty()) {
                        filtreCycle = FILTRE_TOUS;
                    } else {
                        int id = checkedIds.get(0);

                        if (id == R.id.chipLevelsLicence) {
                            filtreCycle = Level.CYCLE_LICENCE;

                        } else if (id == R.id.chipLevelsMaster) {
                            filtreCycle = Level.CYCLE_MASTER;

                        } else if (id == R.id.chipLevelsDoctorat) {
                            filtreCycle = Level.CYCLE_DOCTORAT;

                        } else if (id == R.id.chipLevelsIngenieur) {
                            filtreCycle = Level.CYCLE_INGENIEUR;

                        } else {
                            filtreCycle = FILTRE_TOUS;
                        }
                    }

                    actualiserListe();
                }
        );
    }

    private void observerDonnees() {

        viewModel.getLevels().observe(getViewLifecycleOwner(), resultats -> {
            niveaux = resultats == null
                    ? new ArrayList<>()
                    : new ArrayList<>(resultats);

            actualiserListe();
        });

        viewModel.getError().observe(getViewLifecycleOwner(), erreur -> {
            if (erreur != null && !erreur.trim().isEmpty()) {
                Toast.makeText(
                        requireContext(),
                        erreur,
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(
                        requireContext(),
                        message,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void actualiserListe() {

        if (adapter == null || !isAdded()) {
            return;
        }

        List<Level> resultats = new ArrayList<>();
        String requete = recherche.toLowerCase(Locale.ROOT);

        for (Level level : niveaux) {

            if (level == null) {
                continue;
            }

            boolean correspondCycle =
                    FILTRE_TOUS.equals(filtreCycle)
                            || valeur(level.getCycle())
                            .equalsIgnoreCase(filtreCycle);

            String texte = (
                    valeur(level.getCode()) + " "
                            + valeur(level.getName()) + " "
                            + valeur(level.getCycle()) + " "
                            + valeur(level.getDescription())
            ).toLowerCase(Locale.ROOT);

            boolean correspondRecherche = texte.contains(requete);

            if (correspondCycle && correspondRecherche) {
                resultats.add(level);
            }
        }

        resultats.sort(Comparator.comparingInt(Level::getDisplayOrder));

        adapter.remplacerNiveaux(resultats);

        if (tvCount != null) {
            tvCount.setText(String.valueOf(niveaux.size()));
        }

        if (tvFilteredCount != null) {
            tvFilteredCount.setText(
                    resultats.size()
                            + (resultats.size() > 1
                            ? " résultats"
                            : " résultat")
            );
        }

        boolean listeVide = resultats.isEmpty();

        if (emptyState != null) {
            emptyState.setVisibility(
                    listeVide ? View.VISIBLE : View.GONE
            );
        }

        if (recyclerLevels != null) {
            recyclerLevels.setVisibility(
                    listeVide ? View.GONE : View.VISIBLE
            );
        }
    }

    private void ouvrirFormulaire(int levelId) {

        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).ouvrirEcranSecondaire(
                    LevelFormFragment.newInstance(levelId)
            );
        }
    }

    private void afficherDetails(Level level) {

        String description = valeur(level.getDescription()).isEmpty()
                ? "—"
                : level.getDescription();

        String message =
                "Code : " + valeur(level.getCode()) + "\n"
                        + "Intitulé : " + valeur(level.getName()) + "\n"
                        + "Cycle : " + valeur(level.getCycle()) + "\n"
                        + "Ordre d’affichage : " + level.getDisplayOrder() + "\n\n"
                        + "Description : " + description;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(level.getName())
                .setMessage(message)
                .setPositiveButton("Fermer", null)
                .show();
    }

    private void confirmerSuppression(Level level) {

        if (level == null) {
            return;
        }

        DeletionConfirmationDialog.show(
                requireContext(),
                "Supprimer le niveau ?",
                "Voulez-vous vraiment supprimer le niveau « "
                        + level.getName() + " » ?",
                () -> viewModel.deleteLevel(level)
        );
    }

    private String valeur(@Nullable String texte) {
        return texte == null ? "" : texte;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (viewModel != null) {
            viewModel.loadLevels();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        recyclerLevels = null;
        adapter = null;
        tvCount = null;
        tvFilteredCount = null;
        emptyState = null;
        etSearch = null;
        chipGroupCycle = null;
    }
}