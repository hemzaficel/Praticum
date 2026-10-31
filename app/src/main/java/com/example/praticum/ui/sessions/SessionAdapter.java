package com.example.praticum.ui.sessions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;
import com.example.praticum.data.entity.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SessionAdapter
        extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {

    public interface OnSessionClickListener {
        void onSessionClick(Session session);
    }

    private final List<Session> sessions = new ArrayList<>();
    private Map<Integer, SessionViewModel.SessionCardSummary> summaries = new HashMap<>();

    private final OnSessionClickListener listener;

    public SessionAdapter(OnSessionClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Session> nouvelleListe) {
        sessions.clear();

        if (nouvelleListe != null) {
            sessions.addAll(nouvelleListe);
        }

        notifyDataSetChanged();
    }

    public void submitSummaries(Map<Integer, SessionViewModel.SessionCardSummary> values) {
        summaries = values == null ? new HashMap<>() : new HashMap<>(values);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session, parent, false);

        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull SessionViewHolder holder,
            int position) {

        Session session = sessions.get(position);

        holder.title.setText(
                valeurOuVide(session.getTitle())
        );

        holder.type.setText(
                valeurOuVide(session.getActivityType())
        );

        holder.description.setText(
                valeurOuVide(session.getDescription())
        );

        String statut = session.isArchived()
                ? "Archivée"
                : valeurOuVide(session.getStatus());

        holder.status.setText(statut);

        SessionViewModel.SessionCardSummary summary = summaries.get(session.getId());
        if (summary == null) {
            holder.module.setText("Chargement du module…");
            holder.criteriaCount.setText("Critères d’évaluation : —");
            holder.maxScore.setText("Barème : —");
        } else {
            holder.module.setText(summary.getModuleLabel().isEmpty()
                    ? "Module non renseigné" : summary.getModuleLabel());
            int count = summary.getCriteriaCount();
            holder.criteriaCount.setText(count == 0 ? "Aucun critère d’évaluation"
                    : count + (count == 1 ? " critère d’évaluation" : " critères d’évaluation"));
            holder.maxScore.setText(summary.getMaxScore() <= 0 ? "Barème : —"
                    : String.format(Locale.getDefault(), "Barème · %.2f points", summary.getMaxScore()));
        }

        String description = session.getDescription();
        holder.description.setText(description == null || description.trim().isEmpty()
                ? "Aucune description" : description.trim());
        holder.description.setAlpha(description == null || description.trim().isEmpty() ? 0.7f : 1f);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSessionClick(session);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    private String valeurOuVide(String valeur) {
        return valeur == null || valeur.trim().isEmpty()
                ? "—"
                : valeur;
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView type;
        final TextView description;
        final TextView status;
        final TextView module;
        final TextView criteriaCount;
        final TextView maxScore;

        SessionViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.tvSessionTitle);
            type = itemView.findViewById(R.id.tvSessionType);
            description = itemView.findViewById(
                    R.id.tvSessionDescription
            );
            status = itemView.findViewById(R.id.tvSessionStatus);
            module = itemView.findViewById(R.id.tvSessionModule);
            criteriaCount = itemView.findViewById(R.id.tvSessionCriteriaCount);
            maxScore = itemView.findViewById(R.id.tvSessionMaxScore);
        }
    }
}
