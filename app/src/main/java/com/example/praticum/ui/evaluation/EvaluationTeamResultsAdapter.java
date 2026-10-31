package com.example.praticum.ui.evaluation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;
import com.example.praticum.data.entity.EvaluationTeam;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Liste d'équipes repliables (une carte par équipe, ses étudiants en dessous).
 * Utilisée par le détail d'une évaluation (équipes ouvertes par défaut, sans menu)
 * et par l'historique (équipes repliées par défaut, avec menu ⋮).
 */
public final class EvaluationTeamResultsAdapter
        extends RecyclerView.Adapter<EvaluationTeamResultsAdapter.Holder> {

 /** Actions du menu ⋮ d'une équipe. */
 public interface Listener {
  void onEdit(TeamItem item);

  void onDelete(TeamItem item);
 }

 private final List<TeamItem> items = new ArrayList<>();
 /** Équipes dont l'état est inversé par rapport à l'état par défaut. */
 private final Set<Long> toggled = new HashSet<>();
 private final boolean defaultOpen;
 private final boolean showEvaluation;
 private final Listener listener;
 private boolean lastExpand;

 /** Comportement historique du détail : équipes ouvertes, pas de menu. */
 public EvaluationTeamResultsAdapter() {
  this(true, false, null);
 }

 public EvaluationTeamResultsAdapter(boolean defaultOpen, boolean showEvaluation, Listener listener) {
  this.defaultOpen = defaultOpen;
  this.showEvaluation = showEvaluation;
  this.listener = listener;
 }

 public void submitList(List<TeamItem> x, boolean expand) {
  items.clear();
  if (x != null) {
   items.addAll(x);
  }
  if (expand) {
   toggled.clear();
   if (!defaultOpen) {
    for (TeamItem t : items) {
     toggled.add(t.team.getId());
    }
   }
  } else if (lastExpand && !defaultOpen) {
   toggled.clear();
  }
  lastExpand = expand;
  notifyDataSetChanged();
 }

 private boolean isOpen(long teamId) {
  return defaultOpen != toggled.contains(teamId);
 }

 @NonNull
 @Override
 public Holder onCreateViewHolder(@NonNull ViewGroup p, int t) {
  return new Holder(LayoutInflater.from(p.getContext())
          .inflate(R.layout.item_evaluation_team_results, p, false));
 }

 @Override
 public void onBindViewHolder(@NonNull Holder h, int p) {
  TeamItem x = items.get(p);
  // Historique : numéro continu dans la séance ; sinon numéro propre à l'évaluation.
  if (x.displayNumber > 0) {
   h.number.setText(String.valueOf(x.displayNumber));
   h.name.setText("Équipe " + x.displayNumber);
  } else {
   h.number.setText(String.valueOf(x.team.getTeamNumber()));
   h.name.setText(x.team.getName());
  }

  // Historique : date d'enregistrement ; le détail n'a pas besoin de cette ligne.
  if (showEvaluation && x.finalizedAt != null) {
   h.meta.setText(new SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.getDefault())
           .format(new Date(x.finalizedAt)));
   h.meta.setVisibility(View.VISIBLE);
  } else {
   h.meta.setVisibility(View.GONE);
  }

  h.count.setText(x.students.size() + (x.students.size() == 1 ? " étudiant" : " étudiants"));
  EvaluationStudentAdapter.styleScore(h.score, x.evaluated, x.score, "Non évaluée");

  boolean open = isOpen(x.team.getId());
  h.children.setVisibility(open ? View.VISIBLE : View.GONE);
  h.chevron.setText(open ? "⌃" : "⌄");
  h.divider.setVisibility(open ? View.VISIBLE : View.GONE);
  h.children.setLayoutManager(new LinearLayoutManager(h.itemView.getContext()));
  h.children.setNestedScrollingEnabled(false);
  EvaluationStudentAdapter a = new EvaluationStudentAdapter();
  h.children.setAdapter(a);
  a.submitList(x.students);

  h.header.setOnClickListener(v -> {
   int n = h.getBindingAdapterPosition();
   if (n == RecyclerView.NO_POSITION) {
    return;
   }
   long id = x.team.getId();
   if (toggled.contains(id)) {
    toggled.remove(id);
   } else {
    toggled.add(id);
   }
   notifyItemChanged(n);
  });

  if (listener == null) {
   h.actions.setVisibility(View.GONE);
   h.actions.setOnClickListener(null);
  } else {
   h.actions.setVisibility(View.VISIBLE);
   h.actions.setOnClickListener(v -> {
    PopupMenu menu = new PopupMenu(v.getContext(), h.actions);
    menu.getMenu().add("Modifier la note").setOnMenuItemClickListener(i -> {
     listener.onEdit(x);
     return true;
    });
    menu.getMenu().add("Supprimer l’évaluation").setOnMenuItemClickListener(i -> {
     listener.onDelete(x);
     return true;
    });
    menu.show();
   });
  }
 }

 @Override
 public int getItemCount() {
  return items.size();
 }

 static final class TeamItem {
  final EvaluationTeam team;
  final List<EvaluationStudentAdapter.StudentEvaluationItem> students;
  final boolean evaluated;
  final double score;
  /** Date d'enregistrement de l'évaluation (null si inconnue). */
  final Long finalizedAt;
  /** Numéro continu dans la séance (historique) ; 0 = utiliser le numéro de l'évaluation. */
  final int displayNumber;

  TeamItem(EvaluationTeam t, List<EvaluationStudentAdapter.StudentEvaluationItem> s,
           boolean e, double x) {
   this(t, s, e, x, null);
  }

  TeamItem(EvaluationTeam t, List<EvaluationStudentAdapter.StudentEvaluationItem> s,
           boolean e, double x, Long finalizedAt) {
   this(t, s, e, x, finalizedAt, 0);
  }

  TeamItem(EvaluationTeam t, List<EvaluationStudentAdapter.StudentEvaluationItem> s,
           boolean e, double x, Long finalizedAt, int displayNumber) {
   this.finalizedAt = finalizedAt;
   this.displayNumber = displayNumber;
   team = t;
   students = new ArrayList<>(s);
   evaluated = e;
   score = x;
  }
 }

 static final class Holder extends RecyclerView.ViewHolder {
  final View header;
  final View divider;
  final TextView number, name, meta, count, score, chevron;
  final MaterialButton actions;
  final RecyclerView children;

  Holder(View v) {
   super(v);
   header = v.findViewById(R.id.layoutEvaluationTeamHeader);
   divider = v.findViewById(R.id.dividerEvaluationTeam);
   number = v.findViewById(R.id.tvEvaluationTeamNumber);
   name = v.findViewById(R.id.tvEvaluationTeamName);
   meta = v.findViewById(R.id.tvEvaluationTeamMeta);
   count = v.findViewById(R.id.tvEvaluationTeamCount);
   score = v.findViewById(R.id.tvEvaluationTeamScore);
   chevron = v.findViewById(R.id.tvEvaluationTeamChevron);
   actions = v.findViewById(R.id.btnEvaluationTeamActions);
   children = v.findViewById(R.id.rvEvaluationTeamStudents);
  }
 }
}