package com.example.praticum.ui.groups;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import com.example.praticum.core.common.FilterSortFragment;
import com.example.praticum.data.entity.StudentGroup;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GroupsFragment extends Fragment
        implements FilterSortFragment.OnFilterSortListener {

    private static final String FILTRE_TOUS = "Tous";

    private RecyclerView rvGroups;
    private GroupAdapter adapter;

    private TextInputLayout tilGroupsSearch;
    private EditText etGroupsSearch;
    private TextView tvGroupsCount;

    private FrameLayout filterSortContainer;
    private View btnSearch;
    private MaterialButton btnSort;

    private GroupViewModel viewModel;

    private List<StudentGroup> groups = new ArrayList<>();
    private Map<Integer, Integer> effectifs = new HashMap<>();
    private Map<Integer, Integer> seances = new HashMap<>();

    private String recherche = "";
    private String triPar = "Nom";
    private boolean ordreCroissant = true;
    private String filtreAvance = FILTRE_TOUS;
    private boolean rechercheVisible = false;

    public GroupsFragment() {
        // Constructeur public requis
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_groups,
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

        initialiserVues(view);
        configurerEntete(view);
        configurerListe();
        configurerRecherche();
        configurerCreation(view);
        configurerTriEtFiltrage();
        observerDonnees();
    }

    private void initialiserVues(@NonNull View view) {
        tilGroupsSearch = view.findViewById(R.id.tilGroupsSearch);
        etGroupsSearch = view.findViewById(R.id.etGroupsSearch);
        tvGroupsCount = view.findViewById(R.id.tvGroupsCount);
        rvGroups = view.findViewById(R.id.rvGroups);

        filterSortContainer =
                view.findViewById(R.id.filterSortContainer);

        btnSort = view.findViewById(R.id.btnGroupsSort);

        if (tilGroupsSearch != null) {
            tilGroupsSearch.setVisibility(View.GONE);
        }
    }

    private void configurerEntete(@NonNull View view) {
        CommonHeaderHelper.configurerEntetePrincipal(
                view,
                "Groupes",
                "Gérez vos groupes et leurs membres",
                v -> {
                    if (requireActivity() instanceof MainActivity) {
                        ((MainActivity) requireActivity())
                                .ouvrirMenuLateral();
                    }
                }
        );

        btnSearch = view.findViewById(
                R.id.btnCommonHeaderSearch
        );

        if (btnSearch != null) {
            btnSearch.setOnClickListener(
                    v -> basculerRecherche()
            );
        }
    }

    private void configurerListe() {
        if (rvGroups == null) {
            return;
        }

        rvGroups.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        rvGroups.setNestedScrollingEnabled(false);

        adapter = new GroupAdapter(this::ouvrirDetail);

        rvGroups.setAdapter(adapter);
    }

    private void observerDonnees() {
        viewModel.getStudentCounts().observe(
                getViewLifecycleOwner(),
                valeurs -> {
                    effectifs = valeurs == null
                            ? new HashMap<>()
                            : new HashMap<>(valeurs);

                    actualiserListe();
                }
        );

        viewModel.getSessionCounts().observe(
                getViewLifecycleOwner(),
                valeurs -> {
                    seances = valeurs == null
                            ? new HashMap<>()
                            : new HashMap<>(valeurs);

                    actualiserListe();
                }
        );

        viewModel.getGroups().observe(
                getViewLifecycleOwner(),
                resultats -> {
                    groups = resultats == null
                            ? new ArrayList<>()
                            : new ArrayList<>(resultats);

                    actualiserListe();
                }
        );

        viewModel.getError().observe(
                getViewLifecycleOwner(),
                erreur -> {
                    if (erreur != null && !erreur.trim().isEmpty()) {
                        Toast.makeText(
                                requireContext(),
                                erreur,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void configurerRecherche() {
        if (etGroupsSearch == null) {
            return;
        }

        etGroupsSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
                // Rien à faire
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                recherche = s == null
                        ? ""
                        : s.toString().trim();

                actualiserListe();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Rien à faire
            }
        });
    }

    private void basculerRecherche() {
        if (tilGroupsSearch == null) {
            return;
        }

        rechercheVisible = !rechercheVisible;

        tilGroupsSearch.setVisibility(
                rechercheVisible ? View.VISIBLE : View.GONE
        );

        if (rechercheVisible) {
            if (etGroupsSearch != null) {
                etGroupsSearch.requestFocus();

                InputMethodManager imm =
                        (InputMethodManager) requireContext()
                                .getSystemService(
                                        Context.INPUT_METHOD_SERVICE
                                );

                if (imm != null) {
                    imm.showSoftInput(
                            etGroupsSearch,
                            InputMethodManager.SHOW_IMPLICIT
                    );
                }
            }
        } else {
            masquerClavier();

            if (etGroupsSearch != null) {
                etGroupsSearch.setText("");
                etGroupsSearch.clearFocus();
            }

            recherche = "";
            actualiserListe();
        }
    }

    private void masquerClavier() {
        if (etGroupsSearch == null) {
            return;
        }

        InputMethodManager imm =
                (InputMethodManager) requireContext()
                        .getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (imm != null) {
            imm.hideSoftInputFromWindow(
                    etGroupsSearch.getWindowToken(),
                    0
            );
        }
    }

    private void configurerCreation(@NonNull View view) {
        MaterialButton btnCreateGroup =
                view.findViewById(R.id.btnCreateGroup);

        if (btnCreateGroup != null) {
            btnCreateGroup.setOnClickListener(
                    v -> ouvrirFormulaire()
            );
        }
    }

    private void configurerTriEtFiltrage() {
        if (btnSort == null || filterSortContainer == null) {
            return;
        }

        btnSort.setOnClickListener(v -> {
            if (filterSortContainer.getVisibility() == View.VISIBLE) {
                fermerPanneauTri();
            } else {
                ouvrirPanneauTri();
            }
        });
    }

    private void ouvrirPanneauTri() {
        if (filterSortContainer == null) {
            return;
        }

        filterSortContainer.setVisibility(View.VISIBLE);

        Fragment existant = getChildFragmentManager()
                .findFragmentById(R.id.filterSortContainer);

        if (existant instanceof FilterSortFragment) {
            return;
        }

        String[] optionsFiltre =
                new String[GroupViewModel.SUBJECTS.length + 1];

        optionsFiltre[0] = FILTRE_TOUS;
        System.arraycopy(
                GroupViewModel.SUBJECTS,
                0,
                optionsFiltre,
                1,
                GroupViewModel.SUBJECTS.length
        );

        FilterSortFragment fragment =
                FilterSortFragment.newInstance(
                        new String[]{
                                "Nom",
                                "Code",
                                "Matière",
                                "Nombre d'étudiants"
                        },
                        optionsFiltre
                );

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.filterSortContainer, fragment)
                .commit();
    }

    private void fermerPanneauTri() {
        if (filterSortContainer == null) {
            return;
        }

        filterSortContainer.setVisibility(View.GONE);

        Fragment fragment = getChildFragmentManager()
                .findFragmentById(R.id.filterSortContainer);

        if (fragment != null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commit();
        }
    }

    @Override
    public void onApplyFilterSort(
            String sortBy,
            boolean ascending,
            String filterBy) {

        triPar = sortBy == null ? "Nom" : sortBy;
        ordreCroissant = ascending;
        filtreAvance = filterBy == null ? FILTRE_TOUS : filterBy;

        fermerPanneauTri();
        actualiserListe();
    }

    private void actualiserListe() {
        if (rvGroups == null || adapter == null || !isAdded()) {
            return;
        }

        String query = recherche.toLowerCase(Locale.ROOT);
        List<StudentGroup> resultats = new ArrayList<>();

        for (StudentGroup group : groups) {
            if (group == null) {
                continue;
            }

            String contenu = (
                    valeur(group.getName()) + " " +
                            valeur(group.getCode()) + " " +
                            valeur(group.getSubject()) + " " +
                            compter(effectifs, group.getId()) + " " +
                            compter(seances, group.getId())
            ).toLowerCase(Locale.ROOT);

            boolean correspondRecherche =
                    contenu.contains(query);

            boolean correspondFiltre =
                    correspondAuFiltre(group);

            if (correspondRecherche && correspondFiltre) {
                resultats.add(group);
            }
        }

        appliquerTri(resultats);

        adapter.remplacerGroupes(resultats, effectifs, seances);

        if (tvGroupsCount != null) {
            int nombre = resultats.size();

            tvGroupsCount.setText(
                    nombre + (nombre > 1
                            ? " groupes affichés"
                            : " groupe affiché")
            );
        }

        rvGroups.setVisibility(
                resultats.isEmpty() ? View.GONE : View.VISIBLE
        );
    }

    private boolean correspondAuFiltre(StudentGroup group) {
        if (FILTRE_TOUS.equals(filtreAvance)) {
            return true;
        }

        return valeur(group.getSubject())
                .equalsIgnoreCase(filtreAvance);
    }

    private void appliquerTri(List<StudentGroup> liste) {
        Comparator<StudentGroup> comparator;

        switch (triPar) {
            case "Code":
                comparator = Comparator.comparing(
                        group -> valeur(group.getCode()),
                        String.CASE_INSENSITIVE_ORDER
                );
                break;

            case "Matière":
                comparator = Comparator.comparing(
                        group -> valeur(group.getSubject()),
                        String.CASE_INSENSITIVE_ORDER
                );
                break;

            case "Nombre d'étudiants":
                comparator = Comparator.comparingInt(
                        group -> compter(effectifs, group.getId())
                );
                break;

            case "Nom":
            default:
                comparator = Comparator.comparing(
                        group -> valeur(group.getName()),
                        String.CASE_INSENSITIVE_ORDER
                );
                break;
        }

        if (!ordreCroissant) {
            comparator = comparator.reversed();
        }

        liste.sort(comparator);
    }

    private int compter(Map<Integer, Integer> valeurs, int groupId) {
        Integer nombre = valeurs.get(groupId);
        return nombre == null ? 0 : nombre;
    }

    private String valeur(@Nullable String texte) {
        return texte == null ? "" : texte;
    }

    private void ouvrirFormulaire() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.mainContainer,
                        GroupFormFragment.newInstance(-1)
                )
                .addToBackStack("group_form")
                .commit();
    }

    private void ouvrirDetail(@NonNull StudentGroup group) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.mainContainer,
                        GroupDetailFragment.newInstance(group.getId())
                )
                .addToBackStack("group_detail")
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (viewModel != null) {
            viewModel.loadGroups();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        rvGroups = null;
        adapter = null;
        tilGroupsSearch = null;
        etGroupsSearch = null;
        tvGroupsCount = null;
        filterSortContainer = null;
        btnSearch = null;
        btnSort = null;
    }
}