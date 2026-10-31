package com.example.praticum.ui.evaluation;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;
import com.example.praticum.data.entity.EvaluationTeam;
import com.example.praticum.data.entity.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EvaluationStudentAdapter
        extends RecyclerView.Adapter<
        EvaluationStudentAdapter.StudentViewHolder> {

    private final List<StudentEvaluationItem> items =
            new ArrayList<>();

    public EvaluationStudentAdapter() {
    }

    public void submitList(List<StudentEvaluationItem> newItems) {
        items.clear();

        if (newItems != null) {
            items.addAll(newItems);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_evaluation_student,
                        parent,
                        false
                );

        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull StudentViewHolder holder,
            int position) {

        StudentEvaluationItem item = items.get(position);

        EvaluationViewModel.StudentWithGroup entry = item.getStudentEntry();
        Student student = entry.getStudent();

        holder.tvInitials.setText(student.getInitials());
        holder.tvName.setText(student.getFullName());

        String arabicName = student.getFullNameArabic();
        holder.tvArabicName.setText(arabicName == null ? "" : arabicName);
        holder.tvArabicName.setVisibility(arabicName == null || arabicName.trim().isEmpty()
                ? View.GONE : View.VISIBLE);

        String matricule = student.getMatricule();
        holder.tvMatricule.setText(
                matricule == null || matricule.trim().isEmpty()
                        ? "Matricule non renseigné"
                        : matricule
        );

        // Groupe et ordre dans le groupe
        String groupName = entry.getGroupName();
        if (groupName.trim().isEmpty()) {
            holder.tvGroup.setVisibility(View.GONE);
        } else {
            holder.tvGroup.setText("Groupe " + groupName);
            holder.tvGroup.setVisibility(View.VISIBLE);
        }

        int groupOrder = entry.getGroupOrder();
        if (groupOrder > 0) {
            holder.tvOrder.setText("Ordre " + groupOrder);
            holder.tvOrder.setVisibility(View.VISIBLE);
        } else {
            holder.tvOrder.setVisibility(View.GONE);
        }

        // Note et statut
        styleScore(holder.tvScore, item.isEvaluated(), item.getScoreSur20(), "— / 20");

        if (item.isEvaluated()) {
            holder.tvStatus.setText("Évalué");
            applyBadge(holder.tvStatus, R.color.color_success_container, R.color.color_success);
        } else {
            holder.tvStatus.setText("Non évalué");
            applyBadge(holder.tvStatus, R.color.color_late_container, R.color.color_late);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ------------------------------------------------------------------ styles partagés

    /** Applique une couleur de fond et de texte à une pastille (fond arrondi existant). */
    static void applyBadge(TextView view, @ColorRes int background, @ColorRes int text) {
        ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(
                ContextCompat.getColor(view.getContext(), background)));
        view.setTextColor(ContextCompat.getColor(view.getContext(), text));
    }

    /** Pastille de note : verte à partir de 10/20, rouge en dessous, orange si non évaluée. */
    static void styleScore(TextView view, boolean evaluated, double score, String emptyText) {
        if (!evaluated) {
            view.setText(emptyText);
            applyBadge(view, R.color.color_late_container, R.color.color_late);
            return;
        }
        view.setText(String.format(Locale.getDefault(), "%.2f / 20", score));
        if (score >= 10) {
            applyBadge(view, R.color.color_success_container, R.color.color_success);
        } else {
            applyBadge(view, R.color.color_error_container, R.color.color_absent);
        }
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {

        final TextView tvInitials;
        final TextView tvName;
        final TextView tvArabicName;
        final TextView tvMatricule;
        final TextView tvScore;
        final TextView tvGroup;
        final TextView tvOrder;
        final TextView tvStatus;

        StudentViewHolder(@NonNull View itemView) {
            super(itemView);

            tvInitials = itemView.findViewById(R.id.tvEvaluationStudentInitials);
            tvName = itemView.findViewById(R.id.tvEvaluationStudentName);
            tvArabicName = itemView.findViewById(R.id.tvEvaluationStudentNameArabic);
            tvMatricule = itemView.findViewById(R.id.tvEvaluationStudentMatricule);
            tvScore = itemView.findViewById(R.id.tvEvaluationStudentScore);
            tvGroup = itemView.findViewById(R.id.tvEvaluationStudentGroup);
            tvOrder = itemView.findViewById(R.id.tvEvaluationStudentOrder);
            tvStatus = itemView.findViewById(R.id.tvEvaluationStudentStatus);
        }
    }

    public static final class StudentEvaluationItem {

        private final EvaluationViewModel.StudentWithGroup studentEntry;
        private final EvaluationTeam team;
        private final boolean evaluated;
        private final double scoreSur20;

        public StudentEvaluationItem(
                EvaluationViewModel.StudentWithGroup studentEntry,
                EvaluationTeam team,
                boolean evaluated,
                double scoreSur20) {

            this.studentEntry = studentEntry;
            this.team = team;
            this.evaluated = evaluated;
            this.scoreSur20 = scoreSur20;
        }

        public EvaluationViewModel.StudentWithGroup getStudentEntry() {
            return studentEntry;
        }

        public Student getStudent() {
            return studentEntry.getStudent();
        }

        public EvaluationTeam getTeam() {
            return team;
        }

        public boolean isEvaluated() {
            return evaluated;
        }

        public double getScoreSur20() {
            return scoreSur20;
        }
    }
}