
package com.example.praticum.ui.evaluation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;
import com.example.praticum.ui.attendance.AttendanceRecordMemory;

import java.util.List;

public class EvaluationTeamAdapter
        extends RecyclerView.Adapter<EvaluationTeamAdapter.ViewHolder> {

    public interface OnRemoveStudentListener {
        void onRemove(AttendanceRecordMemory student);
    }

    private final List<AttendanceRecordMemory> students;
    private final OnRemoveStudentListener listener;

    public EvaluationTeamAdapter(
            List<AttendanceRecordMemory> students,
            OnRemoveStudentListener listener) {
        this.students = students;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_evaluation_team, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        AttendanceRecordMemory student = students.get(position);

        String name = student.getStudentName();
        holder.name.setText(name);
        holder.details.setText(
                student.getMatricule() + " • " + student.getGroupName());

        holder.initials.setText(getInitials(name));

        holder.remove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemove(student);
            }
        });
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }

        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();

        for (int i = 0; i < Math.min(2, parts.length); i++) {
            initials.append(parts[i].charAt(0));
        }

        return initials.toString().toUpperCase();
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView initials;
        TextView name;
        TextView details;
        View remove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            initials = itemView.findViewById(R.id.tvTeamStudentInitials);
            name = itemView.findViewById(R.id.tvTeamStudentName);
            details = itemView.findViewById(R.id.tvTeamStudentDetails);
            remove = itemView.findViewById(R.id.btnRemoveTeamStudent);
        }
    }
}
