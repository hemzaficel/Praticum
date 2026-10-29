
package com.example.praticum.ui.sessions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;

import java.util.ArrayList;
import java.util.List;

public class SessionAdapter
        extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {

    public interface OnSessionClickListener {
        void onSessionClick(SessionMemory session);
    }

    private final List<SessionMemory> sessions = new ArrayList<>();
    private final OnSessionClickListener listener;

    public SessionAdapter(OnSessionClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<SessionMemory> nouvelleListe) {
        sessions.clear();

        if (nouvelleListe != null) {
            sessions.addAll(nouvelleListe);
        }

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

        SessionMemory session = sessions.get(position);

        holder.title.setText(valeurOuVide(session.getTitle()));
        holder.type.setText(valeurOuVide(session.getActivityType()));
        holder.description.setText(valeurOuVide(session.getDescription()));

        String statut = session.isArchived()
                ? "Archivée"
                : valeurOuVide(session.getStatus());

        holder.status.setText(statut);

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

        SessionViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.tvSessionTitle);
            type = itemView.findViewById(R.id.tvSessionType);
            description = itemView.findViewById(R.id.tvSessionDescription);
            status = itemView.findViewById(R.id.tvSessionStatus);
        }
    }
}
