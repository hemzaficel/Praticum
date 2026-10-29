
package com.example.praticum.ui.evaluation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;
import com.example.praticum.ui.attendance.AttendanceRecordMemory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EvaluationStudentAdapter
        extends RecyclerView.Adapter<EvaluationStudentAdapter.StudentViewHolder> {

    private final List<StudentEvaluationItem> items = new ArrayList<>();

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
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_evaluation_student, parent, false);

        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull StudentViewHolder holder, int position) {

        StudentEvaluationItem item = items.get(position);
        AttendanceRecordMemory student = item.getStudent();

        holder.tvName.setText(student.getStudentName());

        String matricule = student.getMatricule();
        holder.tvMatricule.setText(
                matricule == null || matricule.trim().isEmpty()
                        ? "Matricule non renseigné"
                        : matricule
        );

        holder.tvGroup.setText(
                "Groupe : " + item.getTeam().getName()
        );

        if (item.isEvaluated()) {
            holder.tvScore.setText(
                    String.format(Locale.getDefault(),
                            "%.2f / 20", item.getScoreSur20())
            );
            holder.tvStatus.setText("Évalué");
        } else {
            holder.tvScore.setText("— / 20");
            holder.tvStatus.setText("Non évalué");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {

        final TextView tvName;
        final TextView tvMatricule;
        final TextView tvScore;
        final TextView tvGroup;
        final TextView tvStatus;

        StudentViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvEvaluationStudentName);
            tvMatricule = itemView.findViewById(R.id.tvEvaluationStudentMatricule);
            tvScore = itemView.findViewById(R.id.tvEvaluationStudentScore);
            tvGroup = itemView.findViewById(R.id.tvEvaluationStudentGroup);
            tvStatus = itemView.findViewById(R.id.tvEvaluationStudentStatus);
        }
    }

    public static class StudentEvaluationItem {

        private final AttendanceRecordMemory student;
        private final EvaluationTeamMemory team;
        private final boolean evaluated;
        private final double scoreSur20;

        public StudentEvaluationItem(
                AttendanceRecordMemory student,
                EvaluationTeamMemory team,
                boolean evaluated,
                double scoreSur20) {

            this.student = student;
            this.team = team;
            this.evaluated = evaluated;
            this.scoreSur20 = scoreSur20;
        }

        public AttendanceRecordMemory getStudent() {
            return student;
        }

        public EvaluationTeamMemory getTeam() {
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
