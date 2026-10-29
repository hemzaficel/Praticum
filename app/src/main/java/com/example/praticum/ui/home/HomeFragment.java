
package com.example.praticum.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.praticum.MainActivity;
import com.example.praticum.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextInputLayout tilHomeSearch;
    private TextInputEditText etHomeSearch;

    private final List<SearchableItem> searchableItems = new ArrayList<>();

    private static class SearchableItem {
        final View view;
        final String searchableText;

        SearchableItem(View view, String searchableText) {
            this.view = view;
            this.searchableText = searchableText.toLowerCase(Locale.ROOT);
        }
    }

    public HomeFragment() {
        // Constructeur requis
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_home,
                container,
                false
        );

        initializeHeader(view);
        initializeSearch(view);

        return view;
    }

    private void initializeHeader(View root) {
        View header = root.findViewById(R.id.includeHomeHeader);

        if (header == null) {
            return;
        }

        TextView title = header.findViewById(R.id.tvCommonHeaderTitle);
        TextView subtitle = header.findViewById(R.id.tvCommonHeaderSubtitle);
        ImageButton menuButton = header.findViewById(R.id.btnCommonMenu);
        ImageButton searchButton = header.findViewById(
                R.id.btnCommonHeaderSearch
        );

        if (title != null) {
            title.setText("Bonjour, Enseignant");
        }

        if (subtitle != null) {
            subtitle.setText("Voici votre espace de travail");
        }

        if (menuButton != null) {
            menuButton.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).ouvrirMenuLateral();
                }
            });
        }

        if (searchButton != null) {
            searchButton.setOnClickListener(v -> toggleSearch());
        }
    }

    private void initializeSearch(View root) {
        tilHomeSearch = root.findViewById(R.id.tilHomeSearch);
        etHomeSearch = root.findViewById(R.id.etHomeSearch);

        if (etHomeSearch == null) {
            return;
        }

        // Éléments que la recherche peut filtrer.
        addSearchableItem(
                root,
                R.id.cardDashboardGroups,
                "Groupes et étudiants gérer groupes membres"
        );

        addSearchableItem(
                root,
                R.id.cardDashboardSession,
                "Séances de TP planifier planning"
        );

        addSearchableItem(
                root,
                R.id.cardDashboardEvaluations,
                "Évaluations notes étudiants"
        );

        addSearchableItem(
                root,
                R.id.cardDashboardRecentActivity,
                "Activité récente groupe créé séance planifiée"
        );

        etHomeSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                filterItems(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        etHomeSearch.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    private void addSearchableItem(
            View root,
            int viewId,
            String searchableText) {

        View item = root.findViewById(viewId);

        if (item != null) {
            searchableItems.add(new SearchableItem(item, searchableText));
        }
    }

    private void toggleSearch() {
        if (tilHomeSearch == null) {
            return;
        }

        if (tilHomeSearch.getVisibility() == View.VISIBLE) {
            tilHomeSearch.setVisibility(View.GONE);

            if (etHomeSearch != null) {
                etHomeSearch.setText("");
            }

            filterItems("");
            hideKeyboard();

        } else {
            tilHomeSearch.setVisibility(View.VISIBLE);

            if (etHomeSearch != null) {
                etHomeSearch.requestFocus();
            }
        }
    }

    private void filterItems(String query) {
        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);

        for (SearchableItem item : searchableItems) {
            boolean matches = normalizedQuery.isEmpty()
                    || item.searchableText.contains(normalizedQuery);

            item.view.setVisibility(matches ? View.VISIBLE : View.GONE);
        }
    }

    private void hideKeyboard() {
        if (getContext() == null || etHomeSearch == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager)
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(etHomeSearch.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroyView() {
        searchableItems.clear();
        tilHomeSearch = null;
        etHomeSearch = null;
        super.onDestroyView();
    }
}
