package com.example.praticum.ui.evaluation;

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
import com.example.praticum.data.entity.EvaluationScore;
import com.example.praticum.data.entity.SessionCriterion;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EvaluationCriterionAdapter
        extends RecyclerView.Adapter<EvaluationCriterionAdapter.ViewHolder> {

    private final List<SessionCriterion> criteria;
    private final List<EvaluationScore> scores;
    private final Runnable onChanged;

    public EvaluationCriterionAdapter(
            List<SessionCriterion> criteria,
            List<EvaluationScore> scores,
            Runnable onChanged) {

        this.criteria = criteria == null
                ? new ArrayList<>()
                : new ArrayList<>(criteria);

        this.scores = scores == null
                ? new ArrayList<>()
                : new ArrayList<>(scores);

        this.onChanged = onChanged;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_evaluation_criterion,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        SessionCriterion criterion = criteria.get(position);

        EvaluationScore score = findScore(criterion.getId());

        holder.bind(criterion, score, onChanged);
    }

    private EvaluationScore findScore(long criterionId) {
        for (EvaluationScore score : scores) {
            if (score.getCriterionId() == criterionId) {
                return score;
            }
        }

        return null;
    }

    @Override
    public int getItemCount() {
        return criteria.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView details;
        private final EditText scoreInput;
        private final TextInputLayout scoreLayout;

        private TextWatcher watcher;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(
                    R.id.tvGradeCriterionTitle
            );

            details = itemView.findViewById(
                    R.id.tvGradeCriterionDetails
            );

            scoreInput = itemView.findViewById(
                    R.id.etGradeCriterion
            );

            scoreLayout = itemView.findViewById(
                    R.id.tilGradeCriterion
            );
        }

        void bind(
                SessionCriterion criterion,
                EvaluationScore score,
                Runnable onChanged) {

            retirerWatcher();

            title.setText(criterion.getTitle());

            double maximum = criterion.getMaxScore();

            String description = criterion.getDescription();

            if (description == null || description.trim().isEmpty()) {
                description = "Barème";
            }

            details.setText(
                    description
                            + " • Maximum : "
                            + format(maximum)
                            + " points"
            );

            scoreLayout.setSuffixText(
                    "/ " + format(maximum)
            );

            scoreInput.setHint("0");

            double currentScore = score == null
                    ? 0
                    : score.getScore();

            scoreInput.setText(
                    currentScore == 0
                            ? ""
                            : format(currentScore)
            );

            scoreInput.setEnabled(score != null);

            if (score == null) {
                return;
            }

            watcher = new TextWatcher() {

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

                @Override
                public void afterTextChanged(Editable editable) {
                    String value = editable.toString()
                            .trim()
                            .replace(',', '.');

                    if (value.isEmpty()) {
                        scoreLayout.setError(null);
                        score.setScore(0);

                        if (onChanged != null) {
                            onChanged.run();
                        }

                        return;
                    }

                    try {
                        double note = Double.parseDouble(value);

                        if (!Double.isFinite(note)
                                || note < 0
                                || note > criterion.getMaxScore()) {

                            scoreLayout.setError(
                                    "Note comprise entre 0 et "
                                            + format(
                                            criterion.getMaxScore()
                                    )
                            );

                            return;
                        }

                        scoreLayout.setError(null);
                        score.setScore(note);

                        if (onChanged != null) {
                            onChanged.run();
                        }

                    } catch (NumberFormatException e) {
                        scoreLayout.setError("Note invalide");
                    }
                }
            };

            scoreInput.addTextChangedListener(watcher);
        }

        private void retirerWatcher() {
            if (watcher != null) {
                scoreInput.removeTextChangedListener(watcher);
                watcher = null;
            }

            scoreLayout.setError(null);
        }

        private String format(double value) {
            return String.format(
                    Locale.getDefault(),
                    "%.2f",
                    value
            ).replaceAll("[,.]?0+$", "");
        }
    }
}