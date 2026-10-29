
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

import java.util.List;
import java.util.Locale;

public class EvaluationCriterionAdapter
        extends RecyclerView.Adapter<EvaluationCriterionAdapter.ViewHolder> {

    private final List<EvaluationCriterion> criteria;
    private final Runnable onChanged;

    public EvaluationCriterionAdapter(
            List<EvaluationCriterion> criteria,
            Runnable onChanged) {
        this.criteria = criteria;
        this.onChanged = onChanged;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_evaluation_criterion, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        EvaluationCriterion criterion = criteria.get(position);

        holder.title.setText(criterion.getTitle());
        holder.details.setText(criterion.getDescription());

        holder.score.setText(
                String.format(Locale.getDefault(), "%s",
                        criterion.getScore() == 0
                                ? ""
                                : Double.toString(criterion.getScore()))
        );

        holder.score.setHint("0");

        if (holder.watcher != null) {
            holder.score.removeTextChangedListener(holder.watcher);
        }

        holder.watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String value = editable.toString().trim();

                if (value.isEmpty()) {
                    criterion.setScore(0);
                    onChanged.run();
                    return;
                }

                try {
                    double score = Double.parseDouble(
                            value.replace(',', '.'));

                    if (score >= 0 && score <= criterion.getMaxScore()) {
                        criterion.setScore(score);
                        onChanged.run();
                    }
                } catch (NumberFormatException ignored) {
                    // La saisie sera vérifiée à nouveau après modification.
                }
            }
        };

        holder.score.addTextChangedListener(holder.watcher);
    }

    @Override
    public int getItemCount() {
        return criteria.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView details;
        EditText score;
        TextWatcher watcher;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.tvGradeCriterionTitle);
            details = itemView.findViewById(R.id.tvGradeCriterionDetails);
            score = itemView.findViewById(R.id.etGradeCriterion);
        }
    }
}
