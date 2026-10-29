
package com.example.praticum.ui.levels;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;
import com.example.praticum.data.entity.Level;

import java.util.ArrayList;
import java.util.List;

public class LevelAdapter
        extends RecyclerView.Adapter<LevelAdapter.LevelViewHolder> {

    public interface OnLevelActionListener {
        void onDetails(Level level);

        void onEdit(Level level);

        void onDelete(Level level);
    }

    private final List<Level> niveaux = new ArrayList<>();
    private final OnLevelActionListener listener;

    public LevelAdapter(OnLevelActionListener listener) {
        this.listener = listener;
    }

    public void remplacerNiveaux(List<Level> nouveauxNiveaux) {
        niveaux.clear();

        if (nouveauxNiveaux != null) {
            niveaux.addAll(nouveauxNiveaux);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LevelViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View vue = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_level, parent, false);

        return new LevelViewHolder(vue);
    }

    @Override
    public void onBindViewHolder(
            @NonNull LevelViewHolder holder,
            int position) {

        Level level = niveaux.get(position);

        holder.tvCode.setText(valeur(level.getCode()));
        holder.tvCycle.setText(valeur(level.getCycle()));
        holder.tvName.setText(valeur(level.getName()));

        String description = valeur(level.getDescription()).trim();

        if (description.isEmpty()) {
            holder.tvDescription.setVisibility(View.GONE);
            holder.tvDescription.setText("");
        } else {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(description);
        }

        // Les statistiques seront raccordées aux requêtes Room adaptées.
        holder.tvStats.setText("Groupes : —  ·  Étudiants : —");

        holder.btnDetails.setOnClickListener(v -> {
            int positionActuelle = holder.getBindingAdapterPosition();

            if (positionActuelle != RecyclerView.NO_POSITION
                    && listener != null) {
                listener.onDetails(niveaux.get(positionActuelle));
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            int positionActuelle = holder.getBindingAdapterPosition();

            if (positionActuelle != RecyclerView.NO_POSITION
                    && listener != null) {
                listener.onEdit(niveaux.get(positionActuelle));
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            int positionActuelle = holder.getBindingAdapterPosition();

            if (positionActuelle != RecyclerView.NO_POSITION
                    && listener != null) {
                listener.onDelete(niveaux.get(positionActuelle));
            }
        });
    }

    @Override
    public int getItemCount() {
        return niveaux.size();
    }

    private String valeur(String texte) {
        return texte == null ? "" : texte;
    }

    static class LevelViewHolder extends RecyclerView.ViewHolder {

        final TextView tvCode;
        final TextView tvCycle;
        final TextView tvName;
        final TextView tvDescription;
        final TextView tvStats;

        final View btnDetails;
        final View btnEdit;
        final View btnDelete;

        LevelViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCode = itemView.findViewById(R.id.tvLevelCode);
            tvCycle = itemView.findViewById(R.id.tvLevelCycle);
            tvName = itemView.findViewById(R.id.tvLevelName);
            tvDescription =
                    itemView.findViewById(R.id.tvLevelDescription);
            tvStats = itemView.findViewById(R.id.tvLevelStats);

            btnDetails = itemView.findViewById(R.id.btnLevelDetails);
            btnEdit = itemView.findViewById(R.id.btnLevelEdit);
            btnDelete = itemView.findViewById(R.id.btnLevelDelete);
        }
    }
}
