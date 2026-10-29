
package com.example.praticum.ui.modules;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.example.praticum.data.entity.Course;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ModulesFragment extends Fragment {

    private ModuleViewModel viewModel;
    private ModuleAdapter adapter;

    private RecyclerView rvModules;
    private View layoutModulesEmpty;

    private TextView tvModulesCount;
    private TextView tvModulesFilteredCount;
    private TextView tvModulesAcademicYear;

    private TextInputLayout tilModulesSearch;
    private TextInputEditText etModulesSearch;

    private ChipGroup chipGroupModulesSemester;
    private Chip chipModulesAll;
    private Chip chipModulesSemester1;
    private Chip chipModulesSemester2;
    private Chip chipModulesUnspecified;

    private View btnSearch;

    private final List<Course> modules = new ArrayList<>();

    private String searchQuery = "";
    private Integer semesterFilter = null;
    private boolean unspecifiedSemesterFilter = false;
    private boolean rechercheVisible = false;

    public ModulesFragment() {
        // Constructeur requis par Android.
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_modules,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        initialiserVues(view);

        viewModel = new ViewModelProvider(this)
                .get(ModuleViewModel.class);

        configurerEntete(view);
        configurerListe();
        configurerRecherche();
        configurerFiltres();
        configurerCreation(view);
        observerDonnees();

        viewModel.loadAll();
    }

    private void initialiserVues(View view) {
        rvModules = view.findViewById(R.id.rvModules);
        layoutModulesEmpty =
                view.findViewById(R.id.layoutModulesEmpty);

        tvModulesCount =
                view.findViewById(R.id.tvModulesCount);
        tvModulesFilteredCount =
                view.findViewById(R.id.tvModulesFilteredCount);

        tilModulesSearch =
                view.findViewById(R.id.tilModulesSearch);
        etModulesSearch =
                view.findViewById(R.id.etModulesSearch);

        chipGroupModulesSemester =
                view.findViewById(R.id.chipGroupModulesSemester);
        chipModulesAll =
                view.findViewById(R.id.chipModulesAll);
        chipModulesSemester1 =
                view.findViewById(R.id.chipModulesSemester1);
        chipModulesSemester2 =
                view.findViewById(R.id.chipModulesSemester2);
        chipModulesUnspecified =
                view.findViewById(R.id.chipModulesUnspecified);

        if (tilModulesSearch != null) {
            tilModulesSearch.setVisibility(
                    rechercheVisible ? View.VISIBLE : View.GONE
            );
        }
    }

    private void configurerEntete(View view) {
        CommonHeaderHelper.configurerEntetePrincipal(
                view,
                "Modules",
                "Gérez vos modules et leurs coefficients",
                v -> {
                    if (getActivity() instanceof MainActivity) {
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
        rvModules.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        rvModules.setNestedScrollingEnabled(false);

        adapter = new ModuleAdapter(this::ouvrirDetail);
        rvModules.setAdapter(adapter);
    }

    private void configurerRecherche() {
        if (etModulesSearch == null) {
            return;
        }

        etModulesSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                searchQuery = s == null ? "" : s.toString();
                actualiserListe();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void basculerRecherche() {
        if (tilModulesSearch == null || etModulesSearch == null) {
            return;
        }

        rechercheVisible = !rechercheVisible;

        if (rechercheVisible) {
            tilModulesSearch.setVisibility(View.VISIBLE);
            etModulesSearch.requestFocus();

            InputMethodManager imm =
                    (InputMethodManager) requireContext()
                            .getSystemService(
                                    Context.INPUT_METHOD_SERVICE
                            );

            if (imm != null) {
                imm.showSoftInput(
                        etModulesSearch,
                        InputMethodManager.SHOW_IMPLICIT
                );
            }
        } else {
            etModulesSearch.setText("");
            searchQuery = "";
            tilModulesSearch.setVisibility(View.GONE);
            masquerClavier();
        }
    }

    private void masquerClavier() {
        if (etModulesSearch == null) {
            return;
        }

        InputMethodManager imm =
                (InputMethodManager) requireContext()
                        .getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (imm != null) {
            imm.hideSoftInputFromWindow(
                    etModulesSearch.getWindowToken(),
                    0
            );
        }
    }

    private void configurerFiltres() {
        if (chipGroupModulesSemester == null) {
            return;
        }

        chipGroupModulesSemester.setOnCheckedStateChangeListener(
                (group, checkedIds) -> {

                    int checkedId = group.getCheckedChipId();

                    if (checkedId == R.id.chipModulesAll) {
                        semesterFilter = null;
                        unspecifiedSemesterFilter = false;

                    } else if (
                            checkedId == R.id.chipModulesSemester1) {
                        semesterFilter = 1;
                        unspecifiedSemesterFilter = false;

                    } else if (
                            checkedId == R.id.chipModulesSemester2) {
                        semesterFilter = 2;
                        unspecifiedSemesterFilter = false;

                    } else if (
                            checkedId == R.id.chipModulesUnspecified) {
                        semesterFilter = null;
                        unspecifiedSemesterFilter = true;
                    }

                    actualiserListe();
                }
        );
    }

    private void configurerCreation(View view) {
        View btnModuleAdd =
                view.findViewById(R.id.btnModuleAdd);

        View btnModulesEmptyAdd =
                view.findViewById(R.id.btnModulesEmptyAdd);

        if (btnModuleAdd != null) {
            btnModuleAdd.setOnClickListener(
                    v -> ouvrirFormulaire(-1)
            );
        }

        if (btnModulesEmptyAdd != null) {
            btnModulesEmptyAdd.setOnClickListener(
                    v -> ouvrirFormulaire(-1)
            );
        }
    }

    private void observerDonnees() {
        viewModel.getModules().observe(
                getViewLifecycleOwner(),
                resultats -> {
                    modules.clear();

                    if (resultats != null) {
                        modules.addAll(resultats);
                    }

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

        viewModel.getOperationSuccess().observe(
                getViewLifecycleOwner(),
                succes -> {
                    if (succes == null) {
                        return;
                    }

                    if (succes) {
                        Toast.makeText(
                                requireContext(),
                                "Opération effectuée avec succès.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void actualiserListe() {
        if (adapter == null || rvModules == null || !isAdded()) {
            return;
        }

        List<Course> resultats = new ArrayList<>();
        String query = normaliser(searchQuery);

        for (Course module : modules) {
            if (module == null) {
                continue;
            }

            String contenu = normaliser(
                    valeur(module.getCode()) + " "
                            + valeur(module.getName()) + " "
                            + valeur(module.getDescription())
            );

            boolean correspondRecherche =
                    query.isEmpty() || contenu.contains(query);

            boolean correspondSemestre;

            if (unspecifiedSemesterFilter) {
                // Le modèle Module actuel utilise un semestre obligatoire.
                // Il n'existe donc pas de semestre non renseigné.
                correspondSemestre = false;

            } else if (semesterFilter != null) {
                correspondSemestre =
                        module.getSemester() == semesterFilter;

            } else {
                correspondSemestre = true;
            }

            if (correspondRecherche && correspondSemestre) {
                resultats.add(module);
            }
        }

        adapter.setModules(resultats);

        if (tvModulesCount != null) {
            tvModulesCount.setText(String.valueOf(modules.size()));
        }

        if (tvModulesFilteredCount != null) {
            tvModulesFilteredCount.setText(
                    resultats.size()
                            + (resultats.size() > 1
                            ? " résultats"
                            : " résultat")
            );
        }

        boolean vide = resultats.isEmpty();

        if (layoutModulesEmpty != null) {
            layoutModulesEmpty.setVisibility(
                    vide ? View.VISIBLE : View.GONE
            );
        }

        rvModules.setVisibility(
                vide ? View.GONE : View.VISIBLE
        );
    }

    private String valeur(String texte) {
        return texte == null ? "" : texte;
    }

    private String normaliser(String texte) {
        return texte == null
                ? ""
                : texte.trim().toLowerCase(Locale.ROOT);
    }

    private void ouvrirDetail(Course module) {
        if (module == null || !isAdded()) {
            return;
        }

        ModuleDetailFragment fragment =
                ModuleDetailFragment.newInstance(module.getId());

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainContainer, fragment)
                .addToBackStack("module_detail")
                .commit();
    }

    private void ouvrirFormulaire(long moduleId) {
        if (!isAdded()) {
            return;
        }

        masquerClavier();

        ModuleFormFragment fragment = moduleId < 0
                ? ModuleFormFragment.newInstance()
                : ModuleFormFragment.newInstance(moduleId);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainContainer, fragment)
                .addToBackStack(
                        moduleId < 0
                                ? "module_form"
                                : "module_edit"
                )
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (viewModel != null) {
            viewModel.loadAll();
        }
    }

    @Override
    public void onDestroyView() {
        rvModules = null;
        layoutModulesEmpty = null;

        tvModulesCount = null;
        tvModulesFilteredCount = null;
        tvModulesAcademicYear = null;

        tilModulesSearch = null;
        etModulesSearch = null;

        chipGroupModulesSemester = null;
        chipModulesAll = null;
        chipModulesSemester1 = null;
        chipModulesSemester2 = null;
        chipModulesUnspecified = null;

        btnSearch = null;
        adapter = null;

        super.onDestroyView();
    }
}
