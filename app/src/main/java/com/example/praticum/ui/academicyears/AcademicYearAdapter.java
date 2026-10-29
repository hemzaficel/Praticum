package com.example.praticum.ui.academicyears;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;
import com.example.praticum.data.entity.AcademicYear;

public class AcademicYearAdapter
        extends ListAdapter<AcademicYear, AcademicYearAdapter.ViewHolder> {

    private final OnAcademicYearActionListener listener;

    public interface OnAcademicYearActionListener {
        void onSetActive(AcademicYear year);

        void onEdit(AcademicYear year);

        void onDelete(AcademicYear year);
    }

    private static final DiffUtil.ItemCallback<AcademicYear> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<AcademicYear>() {

                @Override
                public boolean areItemsTheSame(
                        @NonNull AcademicYear oldItem,
                        @NonNull AcademicYear newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull AcademicYear oldItem,
                        @NonNull AcademicYear newItem) {

                    return oldItem.getStartYear() == newItem.getStartYear()
                            && oldItem.getEndYear() == newItem.getEndYear()
                            && oldItem.isActive() == newItem.isActive()
                            && equal(oldItem.getStartDate(), newItem.getStartDate())
                            && equal(oldItem.getEndDate(), newItem.getEndDate());
                }

                private boolean equal(String first, String second) {
                    return first == null
                            ? second == null
                            : first.equals(second);
                }
            };

    public AcademicYearAdapter(
            OnAcademicYearActionListener listener) {

        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_academic_year,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        AcademicYear year = getItem(position);
        holder.bind(year);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvLabel;
        private final TextView tvStatus;
        private final TextView tvStartDate;
        private final TextView tvEndDate;

        private final ImageButton btnSetActive;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvLabel = itemView.findViewById(
                    R.id.tvAcademicYearLabel);

            tvStatus = itemView.findViewById(
                    R.id.tvAcademicYearStatus);

            tvStartDate = itemView.findViewById(
                    R.id.tvAcademicYearStartDate);

            tvEndDate = itemView.findViewById(
                    R.id.tvAcademicYearEndDate);

            btnSetActive = itemView.findViewById(
                    R.id.btnAcademicYearSetActive);

            btnEdit = itemView.findViewById(
                    R.id.btnAcademicYearEdit);

            btnDelete = itemView.findViewById(
                    R.id.btnAcademicYearDelete);
        }

        void bind(AcademicYear year) {

            tvLabel.setText(year.getLabel());

            if (year.isActive()) {
                tvStatus.setText("Année active");
                btnSetActive.setVisibility(View.GONE);
            } else {
                tvStatus.setText("Année inactive");
                btnSetActive.setVisibility(View.VISIBLE);
            }

            tvStartDate.setText(formatDate(year.getStartDate()));
            tvEndDate.setText(formatDate(year.getEndDate()));

            btnSetActive.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSetActive(year);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(year);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(year);
                }
            });
        }

        private String formatDate(String date) {
            if (date == null || date.trim().isEmpty()) {
                return "Non définie";
            }

            // Convertit yyyy-MM-dd en dd/MM/yyyy si le format est valide.
            if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                String[] parts = date.split("-");
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }

            return date;
        }
    }
}