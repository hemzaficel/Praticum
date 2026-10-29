package com.example.praticum.ui.sessions;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.example.praticum.data.entity.Session;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class SessionsFragment extends Fragment {

    private SessionViewModel viewModel;
    private SessionAdapter adapter;

    private RecyclerView containerSessions;
    private View emptyState;
    private TextView countView;

    private TextInputLayout searchLayout;
    private EditText searchInput;
    private MaterialButton sortButton;
    private View btnSearch;

    private String searchQuery = "";
    private String selectedStatus = "Tous";

    private boolean rechercheVisible = false;

    public SessionsFragment() {
        // Constructeur public requis par Android.
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_sessions,
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

        initialiserVues(view);
        configurerEntete(view);
        configurerListe();
        configurerRecherche();
        configurerCreation(view);
        configurerTri();
        observerDonnees();

        viewModel.setSearchQuery(searchQuery);
        viewModel.setStatusFilter(selectedStatus);
    }

    private void initialiserVues(View view) {
        containerSessions = view.findViewById(
                R.id.containerSessions
        );

        emptyState = view.findViewById(
                R.id.layoutSessionsEmpty
        );

        countView = view.findViewById(
                R.id.tvSessionsCount
        );

        searchLayout = view.findViewById(
                R.id.tilSessionsSearch
        );

        searchInput = view.findViewById(
                R.id.etSessionsSearch
        );

        sortButton = view.findViewById(
                R.id.btnSessionsSort
        );

        if (searchLayout != null) {
            searchLayout.setVisibility(
                    rechercheVisible ? View.VISIBLE : View.GONE
            );
        }
    }

    private void configurerEntete(View view) {
        CommonHeaderHelper.configurerEntetePrincipal(
                view,
                "Séances",
                "Gérez vos séances et leurs activités",
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
        containerSessions.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        containerSessions.setNestedScrollingEnabled(false);

        adapter = new SessionAdapter(this::ouvrirDetail);

        containerSessions.setAdapter(adapter);
    }

    private void configurerRecherche() {
        if (searchInput == null) {
            return;
        }

        searchInput.setText(searchQuery);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
                // Rien à faire.
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                searchQuery = s == null
                        ? ""
                        : s.toString().trim();

                if (viewModel != null) {
                    viewModel.setSearchQuery(searchQuery);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Rien à faire.
            }
        });
    }

    private void basculerRecherche() {
        if (searchLayout == null || searchInput == null) {
            return;
        }

        rechercheVisible = !rechercheVisible;

        if (rechercheVisible) {
            searchLayout.setVisibility(View.VISIBLE);
            searchInput.requestFocus();

            InputMethodManager imm =
                    (InputMethodManager) requireContext()
                            .getSystemService(
                                    Context.INPUT_METHOD_SERVICE
                            );

            if (imm != null) {
                imm.showSoftInput(
                        searchInput,
                        InputMethodManager.SHOW_IMPLICIT
                );
            }

        } else {
            searchInput.setText("");
            searchLayout.setVisibility(View.GONE);

            InputMethodManager imm =
                    (InputMethodManager) requireContext()
                            .getSystemService(
                                    Context.INPUT_METHOD_SERVICE
                            );

            if (imm != null) {
                imm.hideSoftInputFromWindow(
                        searchInput.getWindowToken(),
                        0
                );
            }
        }
    }

    private void configurerCreation(View view) {
        View btnCreateSession = view.findViewById(
                R.id.btnCreateSession
        );

        if (btnCreateSession != null) {
            btnCreateSession.setOnClickListener(
                    v -> ouvrirFormulaire()
            );
        }
    }

    private void configurerTri() {
        if (sortButton == null) {
            return;
        }

        /*
         * Le bouton de tri existant ne définissait pas de mécanisme
         * de tri fonctionnel. Aucun nouveau comportement n'est ajouté.
         */
    }

    private void observerDonnees() {
        viewModel.getSessions().observe(
                getViewLifecycleOwner(),
                this::afficherListe
        );

        viewModel.getOperationResult().observe(
                getViewLifecycleOwner(),
                resultat -> {
                    if (resultat == null) {
                        return;
                    }

                    viewModel.clearOperationResult();

                    if (!resultat.isSuccess() && isAdded()) {
                        Toast.makeText(
                                requireContext(),
                                resultat.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void afficherListe(List<Session> sessions) {
        if (adapter == null) {
            return;
        }

        adapter.submitList(sessions);

        int nombre = sessions == null ? 0 : sessions.size();
        boolean listeVide = nombre == 0;

        if (emptyState != null) {
            emptyState.setVisibility(
                    listeVide ? View.VISIBLE : View.GONE
            );
        }

        if (containerSessions != null) {
            containerSessions.setVisibility(
                    listeVide ? View.GONE : View.VISIBLE
            );
        }

        if (countView != null) {
            countView.setText(
                    nombre
                            + (nombre > 1
                            ? " activités affichées"
                            : " activité affichée")
            );
        }
    }

    private void ouvrirDetail(Session session) {
        if (session == null) {
            return;
        }

        SessionDetailFragment fragment =
                new SessionDetailFragment();

        Bundle args = new Bundle();
        args.putInt("session_id", session.getId());

        fragment.setArguments(args);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainContainer, fragment)
                .addToBackStack("session_detail")
                .commit();
    }

    private void ouvrirFormulaire() {
        SessionFormFragment fragment =
                new SessionFormFragment();

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainContainer, fragment)
                .addToBackStack("session_form")
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();

        /*
         * Recharge les données quand on revient du formulaire
         * ou de l'écran de détail.
         */
        if (viewModel != null) {
            viewModel.loadModules();
            viewModel.loadSessions();
        }
    }

    @Override
    public void onDestroyView() {
        containerSessions = null;
        emptyState = null;
        countView = null;
        searchLayout = null;
        searchInput = null;
        sortButton = null;
        btnSearch = null;
        adapter = null;

        super.onDestroyView();
    }
}