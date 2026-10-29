package com.example.praticum.ui.evaluation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;
import com.example.praticum.data.entity.Student;

import java.util.ArrayList;
import java.util.List;

public class EvaluationTeamAdapter
        extends RecyclerView.Adapter<EvaluationTeamAdapter.ViewHolder> {

    public interface OnRemoveStudentListener {
        void onRemove(
                EvaluationViewModel.StudentWithGroup student
        );
    }

    private final List<EvaluationViewModel.StudentWithGroup> students =
            new ArrayList<>();

    private final OnRemoveStudentListener listener;

    public EvaluationTeamAdapter(
            List<EvaluationViewModel.StudentWithGroup> students,
            OnRemoveStudentListener listener) {

        if (students != null) {
            this.students.addAll(students);
        }

        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_evaluation_team,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        EvaluationViewModel.StudentWithGroup entry =
                students.get(position);

        Student student = entry.getStudent();

        holder.name.setText(student.getFullName());
        holder.initials.setText(student.getInitials());

        String groupName = entry.getGroupName();

        if (groupName == null || groupName.trim().isEmpty()) {
            groupName = "Groupe non renseigné";
        }

        holder.details.setText(
                "Ordre : "
                        + student.getGroupOrder()
                        + " • "
                        + groupName
        );

        holder.remove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemove(entry);
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView initials;
        final TextView name;
        final TextView details;
        final View remove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            initials = itemView.findViewById(
                    R.id.tvTeamStudentInitials
            );

            name = itemView.findViewById(
                    R.id.tvTeamStudentName
            );

            details = itemView.findViewById(
                    R.id.tvTeamStudentDetails
            );

            remove = itemView.findViewById(
                    R.id.btnRemoveTeamStudent
            );
        }
    }
}