package com.example.praticum.ui.students;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;
import com.example.praticum.data.entity.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StudentAdapter
        extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private final List<Student> students = new ArrayList<>();
    private final Map<Integer, String> codesGroupes = new HashMap<>();
    private final OnStudentClickListener clickListener;

    public StudentAdapter(OnStudentClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /**
     * @param codesGroupes code du groupe par identifiant de groupe
     */
    public void remplacerEtudiants(
            List<Student> nouveauxEtudiants,
            Map<Integer, String> codesGroupes) {

        students.clear();
        this.codesGroupes.clear();

        if (nouveauxEtudiants != null) {
            students.addAll(nouveauxEtudiants);
        }

        if (codesGroupes != null) {
            this.codesGroupes.putAll(codesGroupes);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);

        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull StudentViewHolder holder,
            int position) {

        Student student = students.get(position);

        // Nom complet en français
        holder.tvStudentName.setText(student.getFullName());

        // Nom complet en arabe : facultatif
        String arabicName = student.getFullNameArabic();

        if (arabicName.trim().isEmpty()) {
            holder.tvStudentNameArabic.setText("");
            holder.tvStudentNameArabic.setVisibility(View.GONE);
        } else {
            holder.tvStudentNameArabic.setText(arabicName);
            holder.tvStudentNameArabic.setVisibility(View.VISIBLE);
        }

        // Groupe
        String group = codesGroupes.get(student.getGroupId());

        if (group == null || group.trim().isEmpty()) {
            holder.tvStudentGroup.setText("Groupe non défini");
        } else {
            holder.tvStudentGroup.setText("Groupe : " + group);
        }

        // Matricule facultatif
        String matricule = student.getMatricule();

        if (matricule == null || matricule.trim().isEmpty()) {
            holder.tvStudentMatriculeDetail.setText("Non renseigné");
        } else {
            holder.tvStudentMatriculeDetail.setText(matricule);
        }

        // Ordre dans le groupe
        holder.tvStudentGroupOrder.setText(
                String.format(
                        Locale.ROOT,
                        "N° %02d",
                        student.getGroupOrder()
                )
        );

        // Initiales
        String initials = student.getInitials();

        holder.tvStudentInitials.setText(
                initials.trim().isEmpty() ? "?" : initials
        );

        // Ouvrir le détail au clic sur la carte
        holder.itemView.setOnClickListener(v -> {
            int positionActuelle = holder.getBindingAdapterPosition();

            if (positionActuelle != RecyclerView.NO_POSITION
                    && clickListener != null) {
                clickListener.onStudentClick(
                        students.get(positionActuelle)
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {

        final TextView tvStudentInitials;
        final TextView tvStudentName;
        final TextView tvStudentNameArabic;
        final TextView tvStudentGroup;
        final TextView tvStudentMatriculeDetail;
        final TextView tvStudentGroupOrder;

        StudentViewHolder(@NonNull View itemView) {
            super(itemView);

            tvStudentInitials =
                    itemView.findViewById(R.id.tvStudentInitials);

            tvStudentName =
                    itemView.findViewById(R.id.tvStudentName);

            tvStudentNameArabic =
                    itemView.findViewById(R.id.tvStudentNameArabic);

            tvStudentGroup =
                    itemView.findViewById(R.id.tvStudentGroup);

            tvStudentMatriculeDetail =
                    itemView.findViewById(R.id.tvStudentMatriculeDetail);

            tvStudentGroupOrder =
                    itemView.findViewById(R.id.tvStudentGroupOrder);
        }
    }

    /**
     * Interface appelée lorsqu'un étudiant est sélectionné.
     */
    public interface OnStudentClickListener {
        void onStudentClick(Student student);
    }
}