
package com.example.praticum.ui.reports;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

public class ReportsFragment extends Fragment {

    private TextInputLayout tilReportsSearch;
    private TextInputEditText etReportsSearch;

    private LinearLayout layoutReportsList;
    private LinearLayout layoutReportsEmpty;

    private TextView tvReportsCount;
    private TextView tvReportsEmptyMessage;

    private final List<ReportItem> reportItems = new ArrayList<>();

    private static class ReportItem {
        final View card;
        final String title;
        final String description;

        ReportItem(View card, String title, String description) {
            this.card = card;
            this.title = title;
            this.description = description;
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_reports,
                container,
                false
        );

        initializeHeader(view);
        initializeSearch(view);
        initializeReports(view);

        return view;
    }

    private void initializeHeader(View view) {
        View header = view.findViewById(R.id.includeReportsHeader);

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
            title.setText("Bilans et statistiques");
        }

        if (subtitle != null) {
            subtitle.setText("Consultez et exportez vos rapports");
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

    private void initializeSearch(View view) {
        tilReportsSearch = view.findViewById(R.id.tilReportsSearch);
        etReportsSearch = view.findViewById(R.id.etReportsSearch);

        if (etReportsSearch != null) {
            etReportsSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(
                        CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(
                        CharSequence s, int start, int before, int count) {
                    filterReports(s == null ? "" : s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    private void toggleSearch() {
        if (tilReportsSearch == null) {
            return;
        }

        if (tilReportsSearch.getVisibility() == View.VISIBLE) {
            tilReportsSearch.setVisibility(View.GONE);

            if (etReportsSearch != null) {
                etReportsSearch.setText("");
            }

            filterReports("");
        } else {
            tilReportsSearch.setVisibility(View.VISIBLE);

            if (etReportsSearch != null) {
                etReportsSearch.requestFocus();
            }
        }
    }

    private void initializeReports(View view) {
        layoutReportsList = view.findViewById(R.id.layoutReportsList);
        layoutReportsEmpty = view.findViewById(R.id.layoutReportsEmpty);
        tvReportsCount = view.findViewById(R.id.tvReportsCount);
        tvReportsEmptyMessage = view.findViewById(
                R.id.tvReportsEmptyMessage
        );

        addReport(
                view,
                R.id.cardReportAttendance,
                R.id.tvReportAttendanceDescription,
                "Bilan des présences"
        );

        addReport(
                view,
                R.id.cardReportSessions,
                R.id.tvReportSessionsDescription,
                "Bilan des séances"
        );

        addReport(
                view,
                R.id.cardReportEvaluations,
                R.id.tvReportEvaluationsDescription,
                "Bilan des évaluations"
        );

        addReport(
                view,
                R.id.cardReportResults,
                R.id.tvReportResultsDescription,
                "Bilan des résultats"
        );

        addReport(
                view,
                R.id.cardReportIndividual,
                R.id.tvReportIndividualDescription,
                "Bilan individuel"
        );

        addReport(
                view,
                R.id.cardReportGroup,
                R.id.tvReportGroupDescription,
                "Bilan de groupe"
        );

        updateCount(reportItems.size());
    }

    private void addReport(
            View root,
            int cardId,
            int descriptionId,
            String title) {

        View card = root.findViewById(cardId);

        if (card == null) {
            return;
        }

        TextView descriptionView = root.findViewById(descriptionId);

        String description = "";

        if (descriptionView != null
                && descriptionView.getText() != null) {
            description = descriptionView.getText().toString();
        }

        reportItems.add(new ReportItem(card, title, description));
    }

    private void filterReports(String query) {
        if (!isAdded() || layoutReportsList == null) {
            return;
        }

        String normalizedQuery = query
                .trim()
                .toLowerCase(Locale.ROOT);

        int visibleCount = 0;

        for (ReportItem item : reportItems) {
            String searchableText = (
                    item.title + " " + item.description
            ).toLowerCase(Locale.ROOT);

            boolean matches = normalizedQuery.isEmpty()
                    || searchableText.contains(normalizedQuery);

            item.card.setVisibility(
                    matches ? View.VISIBLE : View.GONE
            );

            if (matches) {
                visibleCount++;
            }
        }

        updateCount(visibleCount);

        boolean isEmpty = visibleCount == 0;

        if (layoutReportsEmpty != null) {
            layoutReportsEmpty.setVisibility(
                    isEmpty ? View.VISIBLE : View.GONE
            );
        }

        if (layoutReportsList != null) {
            layoutReportsList.setVisibility(
                    isEmpty ? View.GONE : View.VISIBLE
            );
        }

        if (tvReportsEmptyMessage != null && isEmpty) {
            tvReportsEmptyMessage.setText(
                    normalizedQuery.isEmpty()
                            ? "Aucun bilan disponible."
                            : "Aucun bilan ne correspond à « "
                            + query.trim() + " »."
            );
        }
    }

    private void updateCount(int count) {
        if (tvReportsCount == null) {
            return;
        }

        tvReportsCount.setText(
                count + (count > 1 ? " bilans" : " bilan")
        );
    }
}
