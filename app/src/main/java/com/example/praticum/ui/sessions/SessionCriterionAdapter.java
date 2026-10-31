package com.example.praticum.ui.sessions;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;
import com.example.praticum.data.entity.SessionCriterion;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SessionCriterionAdapter
        extends RecyclerView.Adapter<
        SessionCriterionAdapter.CriterionViewHolder> {

    public interface OnCriteriaChangedListener {
        void onCriteriaChanged();
    }

    private final List<SessionCriterion> criteria = new ArrayList<>();

    private final OnCriteriaChangedListener listener;

    public SessionCriterionAdapter(
            OnCriteriaChangedListener listener) {
        this.listener = listener;
    }

    public void setCriteria(List<SessionCriterion> items) {
        criteria.clear();

        if (items != null) {
            criteria.addAll(items);
        }

        normaliserPositions();
        notifyDataSetChanged();
        notifyChanged();
    }

    public List<SessionCriterion> getCriteria() {
        return new ArrayList<>(criteria);
    }

    public void addCriterion() {
        SessionCriterion criterion = new SessionCriterion(
                0,
                "",
                0,
                criteria.size()
        );

        criteria.add(criterion);

        notifyItemInserted(criteria.size() - 1);
        notifyChanged();
    }

    public void addCriteria(List<SessionCriterion> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        int start = criteria.size();

        for (SessionCriterion item : items) {
            if (item == null) {
                continue;
            }

            item.setPosition(criteria.size());
            criteria.add(item);
        }

        normaliserPositions();

        if (criteria.size() > start) {
            notifyItemRangeInserted(
                    start,
                    criteria.size() - start
            );
        }

        notifyChanged();
    }

    private void removeCriterion(int position) {
        if (position < 0 || position >= criteria.size()) {
            return;
        }

        criteria.remove(position);

        normaliserPositions();

        notifyItemRemoved(position);

        if (position < criteria.size()) {
            notifyItemRangeChanged(
                    position,
                    criteria.size() - position
            );
        }

        notifyChanged();
    }

    private void normaliserPositions() {
        for (int i = 0; i < criteria.size(); i++) {
            criteria.get(i).setPosition(i);
        }
    }

    public double getTotalScore() {
        double total = 0;

        for (SessionCriterion criterion : criteria) {
            total += Math.max(0, criterion.getMaxScore());
        }

        return total;
    }

    private void notifyChanged() {
        if (listener != null) {
            listener.onCriteriaChanged();
        }
    }

    @NonNull
    @Override
    public CriterionViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_session_criterion,
                        parent,
                        false
                );

        return new CriterionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CriterionViewHolder holder,
            int position) {

        holder.bind(
                criteria.get(position),
                position + 1
        );
    }

    @Override
    public int getItemCount() {
        return criteria.size();
    }

    class CriterionViewHolder extends RecyclerView.ViewHolder {

        private final TextView number;
        private final TextView heading;
        private final View deleteButton;

        private final EditText titleInput;
        private final EditText descriptionInput;
        private final EditText maxScoreInput;

        private final TextInputLayout titleLayout;
        private final TextInputLayout descriptionLayout;
        private final TextInputLayout maxScoreLayout;

        private TextWatcher titleWatcher;
        private TextWatcher descriptionWatcher;
        private TextWatcher scoreWatcher;

        CriterionViewHolder(@NonNull View itemView) {
            super(itemView);

            number = itemView.findViewById(
                    R.id.tvCriterionNumber
            );

            heading = itemView.findViewById(
                    R.id.tvCriterionHeading
            );

            deleteButton = itemView.findViewById(
                    R.id.btnDeleteCriterion
            );

            titleLayout = itemView.findViewById(
                    R.id.tilCriterionTitle
            );

            titleInput = itemView.findViewById(
                    R.id.etCriterionTitle
            );

            descriptionLayout = itemView.findViewById(
                    R.id.tilCriterionDescription
            );

            descriptionInput = itemView.findViewById(
                    R.id.etCriterionDescription
            );

            maxScoreLayout = itemView.findViewById(
                    R.id.tilCriterionMaxScore
            );

            maxScoreInput = itemView.findViewById(
                    R.id.etCriterionMaxScore
            );
        }

        void bind(
                SessionCriterion criterion,
                int displayNumber) {

            removeWatchers();

            number.setText(
                    String.valueOf(displayNumber)
            );

            String title = criterion.getTitle();

            heading.setText(
                    title == null || title.trim().isEmpty()
                            ? "Nouveau critère"
                            : title
            );

            titleInput.setText(title);

            descriptionInput.setText(
                    criterion.getDescription()
            );

            double score = criterion.getMaxScore();

            maxScoreInput.setText(
                    score == 0
                            ? ""
                            : String.format(
                            Locale.getDefault(),
                            "%s",
                            score == Math.floor(score)
                                    ? String.valueOf((int) score)
                                    : String.valueOf(score)
                    )
            );

            titleWatcher = new SimpleWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    criterion.setTitle(s.toString());

                    heading.setText(
                            s.toString().trim().isEmpty()
                                    ? "Nouveau critère"
                                    : s.toString()
                    );
                }
            };

            descriptionWatcher = new SimpleWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    criterion.setDescription(s.toString());
                }
            };

            scoreWatcher = new SimpleWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    String value = s.toString()
                            .trim()
                            .replace(',', '.');

                    try {
                        double parsed = value.isEmpty()
                                ? 0
                                : Double.parseDouble(value);

                        criterion.setMaxScore(
                                Double.isFinite(parsed) && parsed >= 0
                                        ? parsed
                                        : 0
                        );

                    } catch (NumberFormatException e) {
                        criterion.setMaxScore(0);
                    }

                    notifyChanged();
                }
            };

            titleInput.addTextChangedListener(titleWatcher);

            descriptionInput.addTextChangedListener(
                    descriptionWatcher
            );

            maxScoreInput.addTextChangedListener(scoreWatcher);

            deleteButton.setOnClickListener(v -> {
                int currentPosition = getBindingAdapterPosition();

                if (currentPosition != RecyclerView.NO_POSITION) {
                    SessionCriterion critereSelectionne = criteria.get(currentPosition);
                    new MaterialAlertDialogBuilder(itemView.getContext())
                            .setTitle("Supprimer le critère ?")
                            .setMessage("Ce critère d’évaluation sera retiré de la séance.")
                            .setNegativeButton("Annuler", null)
                            .setPositiveButton("Supprimer", (dialog, which) -> {
                                int position = criteria.indexOf(critereSelectionne);
                                if (position != -1) {
                                    removeCriterion(position);
                                }
                            })
                            .show();
                }
            });
        }

        private void removeWatchers() {
            if (titleWatcher != null) {
                titleInput.removeTextChangedListener(titleWatcher);
            }

            if (descriptionWatcher != null) {
                descriptionInput.removeTextChangedListener(
                        descriptionWatcher
                );
            }

            if (scoreWatcher != null) {
                maxScoreInput.removeTextChangedListener(scoreWatcher);
            }
        }
    }

    private abstract static class SimpleWatcher
            implements TextWatcher {

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
        }
    }
}