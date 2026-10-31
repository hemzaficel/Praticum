package com.example.praticum.ui.groups;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;
import com.example.praticum.data.entity.StudentGroup;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupAdapter
        extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(StudentGroup group);
    }

    private final List<StudentGroup> groups = new ArrayList<>();
    private final Map<Integer, Integer> effectifs = new HashMap<>();
    private final Map<Integer, Integer> seances = new HashMap<>();
    private final OnGroupClickListener listener;

    public GroupAdapter(OnGroupClickListener listener) {
        this.listener = listener;
    }

    public void remplacerGroupes(
            List<StudentGroup> nouveauxGroupes,
            Map<Integer, Integer> nouveauxEffectifs,
            Map<Integer, Integer> nouvellesSeances) {

        groups.clear();
        effectifs.clear();
        seances.clear();

        if (nouveauxGroupes != null) {
            groups.addAll(nouveauxGroupes);
        }

        if (nouveauxEffectifs != null) {
            effectifs.putAll(nouveauxEffectifs);
        }

        if (nouvellesSeances != null) {
            seances.putAll(nouvellesSeances);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull GroupViewHolder holder, int position) {
        StudentGroup group = groups.get(position);

        String specialite = valeur(group.getSubject()).trim();
        int nbEtudiants = compter(effectifs, group.getId());
        int nbSeances = compter(seances, group.getId());

        holder.name.setText(valeur(group.getName()));
        holder.code.setText("Code : " + valeur(group.getCode()));
        holder.subject.setText(
                "Spécialité : " + (specialite.isEmpty() ? "—" : specialite)
        );
        holder.students.setText(
                nbEtudiants + (nbEtudiants > 1
                        ? " étudiants"
                        : " étudiant")
        );
        holder.sessions.setText(
                nbSeances + (nbSeances > 1
                        ? " séances"
                        : " séance")
        );

        holder.initials.setText(getInitials(group.getName()));

        holder.card.setOnClickListener(v -> {
            int positionActuelle = holder.getBindingAdapterPosition();

            if (positionActuelle != RecyclerView.NO_POSITION
                    && listener != null) {
                listener.onGroupClick(groups.get(positionActuelle));
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    private int compter(Map<Integer, Integer> valeurs, int groupId) {
        Integer nombre = valeurs.get(groupId);
        return nombre == null ? 0 : nombre;
    }

    private String getInitials(@Nullable String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }

        String[] words = name.trim().split("\\s+");

        if (words.length == 1) {
            return words[0].substring(
                    0, Math.min(2, words[0].length())
            ).toUpperCase();
        }

        return (words[0].substring(0, 1)
                + words[1].substring(0, 1)).toUpperCase();
    }

    private String valeur(@Nullable String texte) {
        return texte == null ? "" : texte;
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {

        final MaterialCardView card;
        final TextView initials;
        final TextView name;
        final TextView code;
        final TextView subject;
        final TextView students;
        final TextView sessions;

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);

            card = itemView.findViewById(R.id.cardGroupItem);
            initials = itemView.findViewById(R.id.tvGroupInitials);
            name = itemView.findViewById(R.id.tvGroupName);
            code = itemView.findViewById(R.id.tvGroupCode);
            subject = itemView.findViewById(R.id.tvGroupSubject);
            students = itemView.findViewById(R.id.tvGroupStudents);
            sessions = itemView.findViewById(R.id.tvGroupSessions);
        }
    }
}