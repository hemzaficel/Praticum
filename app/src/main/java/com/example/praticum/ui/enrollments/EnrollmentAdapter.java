package com.example.praticum.ui.enrollments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.praticum.R;
import com.example.praticum.data.entity.EnrollmentListItem;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentAdapter extends RecyclerView.Adapter<EnrollmentAdapter.Holder> {
    public interface Listener {
        void onDetails(EnrollmentListItem item);
        void onEdit(EnrollmentListItem item);
        void onDelete(EnrollmentListItem item);
    }
    private final List<EnrollmentListItem> items = new ArrayList<>();
    private final Listener listener;
    private final boolean selectionMode;
    public EnrollmentAdapter(Listener listener) { this(listener, false); }
    public EnrollmentAdapter(Listener listener, boolean selectionMode) {
        this.listener = listener;
        this.selectionMode = selectionMode;
    }
    public void submitList(List<EnrollmentListItem> rows) {
        items.clear();
        if (rows != null) items.addAll(rows);
        notifyDataSetChanged();
    }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_enrollment, parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder h, int position) {
        EnrollmentListItem row = items.get(position);
        h.name.setText((row.firstName == null ? "" : row.firstName) + " " + (row.lastName == null ? "" : row.lastName));
        String arabicName = ((row.firstNameArabic == null ? "" : row.firstNameArabic) + " " +
                (row.lastNameArabic == null ? "" : row.lastNameArabic)).trim();
        h.nameArabic.setText(arabicName);
        h.nameArabic.setVisibility(arabicName.isEmpty() ? View.GONE : View.VISIBLE);
        h.meta.setText(row.matricule == null || row.matricule.trim().isEmpty()
                ? "Matricule non renseigné" : "Matricule · " + row.matricule);
        h.program.setText(row.specializationName + " · " + row.levelName);
        h.year.setText("Année académique · " + row.academicYearLabel);
        h.group.setText(row.groupCode == null ? "Groupe non renseigné" : row.groupCode);
        h.orderBadge.setText("N° " + row.groupOrder);
        h.status.setText(row.status);
        h.itemView.setOnClickListener(v -> listener.onDetails(row));
        h.details.setVisibility(selectionMode ? View.GONE : View.VISIBLE);
        h.edit.setVisibility(selectionMode ? View.GONE : View.VISIBLE);
        h.delete.setVisibility(selectionMode ? View.GONE : View.VISIBLE);
        if (!selectionMode) {
            h.details.setOnClickListener(v -> listener.onDetails(row));
            h.edit.setOnClickListener(v -> listener.onEdit(row));
            h.delete.setOnClickListener(v -> listener.onDelete(row));
        }
    }
    @Override public int getItemCount() { return items.size(); }
    static class Holder extends RecyclerView.ViewHolder {
        TextView name, nameArabic, meta, program, year, group, orderBadge, status;
        View details, edit, delete;
        Holder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.tvEnrollmentStudentName);
            nameArabic = v.findViewById(R.id.tvEnrollmentStudentNameArabic);
            meta = v.findViewById(R.id.tvEnrollmentMeta);
            program = v.findViewById(R.id.tvEnrollmentProgram);
            year = v.findViewById(R.id.tvEnrollmentYear);
            group = v.findViewById(R.id.tvEnrollmentGroup);
            orderBadge = v.findViewById(R.id.tvEnrollmentOrderBadge);
            status = v.findViewById(R.id.tvEnrollmentStatus);
            details = v.findViewById(R.id.btnEnrollmentDetails);
            edit = v.findViewById(R.id.btnEnrollmentEdit);
            delete = v.findViewById(R.id.btnEnrollmentDelete);
        }
    }
}
