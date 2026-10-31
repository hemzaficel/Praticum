package com.example.praticum.ui.evaluation;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.example.praticum.core.common.DeletionConfirmationDialog;
import com.example.praticum.data.entity.Evaluation;
import com.example.praticum.data.entity.Student;
import com.google.android.material.textfield.TextInputEditText;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Historique d'une séance : toutes les équipes évaluées, chacune dans un menu repliable
 * qui révèle ses étudiants. La recherche (nom, prénom, ordre dans le groupe) masque les
 * équipes sans correspondance et déplie celles qui en ont une.
 */
public class EvaluationHistoryFragment extends Fragment {

 private int sid = -1;
 private String title = "";
 private String query = "";
 private boolean refresh;

 private EvaluationViewModel vm;
 private RecyclerView list;
 private TextView empty;
 private TextInputEditText search;
 private EvaluationTeamResultsAdapter adapter;

 /** Toutes les équipes de la séance (non filtrées). */
 private final List<EvaluationTeamResultsAdapter.TeamItem> all = new ArrayList<>();

 public EvaluationHistoryFragment() {
 }

 @Nullable
 @Override
 public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle b) {
  return i.inflate(R.layout.fragment_evaluation_history, c, false);
 }

 @Override
 public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
  super.onViewCreated(v, b);

  Bundle a = getArguments();
  if (a != null) {
   sid = a.getInt("session_id", -1);
   title = a.getString("session_title", "");
  }
  CommonHeaderHelper.configurerEnteteSousFenetre(
          v, "Historique des évaluations", title, requireActivity());

  vm = new ViewModelProvider(this).get(EvaluationViewModel.class);
  list = v.findViewById(R.id.rvEvaluationHistory);
  empty = v.findViewById(R.id.tvEvaluationHistoryEmpty);
  search = v.findViewById(R.id.etEvaluationHistorySearch);

  list.setLayoutManager(new LinearLayoutManager(requireContext()));
  adapter = new EvaluationTeamResultsAdapter(false, true,
          new EvaluationTeamResultsAdapter.Listener() {
           @Override
           public void onEdit(EvaluationTeamResultsAdapter.TeamItem item) {
            openEdit(item);
           }

           @Override
           public void onDelete(EvaluationTeamResultsAdapter.TeamItem item) {
            confirmDelete(item);
           }
          });
  list.setAdapter(adapter);

  search.addTextChangedListener(new TextWatcher() {
   @Override
   public void beforeTextChanged(CharSequence s, int st, int c, int af) {
   }

   @Override
   public void onTextChanged(CharSequence s, int st, int before, int c) {
    query = s == null ? "" : s.toString().trim();
    render();
   }

   @Override
   public void afterTextChanged(Editable e) {
   }
  });

  vm.getSheetState().observe(getViewLifecycleOwner(), s -> {
   if (s == null || !isAdded()) {
    return;
   }
   all.clear();
   Map<Long, Long> dates = new HashMap<>();
   for (Evaluation e : s.getEvaluations()) {
    if (e.getFinalizedAt() != null) {
     dates.put((long) e.getId(), e.getFinalizedAt());
    }
   }
   // Numéro continu dans la séance : la plus ancienne équipe est l'équipe 1.
   List<EvaluationViewModel.EvaluationTeamState> chrono = s.getTeams();
   Collections.sort(chrono, (p1, p2) -> {
    int byEvaluation = Long.compare(
            p1.getTeam().getEvaluationId(), p2.getTeam().getEvaluationId());
    return byEvaluation != 0 ? byEvaluation : Integer.compare(
            p1.getTeam().getTeamNumber(), p2.getTeam().getTeamNumber());
   });
   Map<Long, Integer> ranks = new HashMap<>();
   for (int i = 0; i < chrono.size(); i++) {
    ranks.put(chrono.get(i).getTeam().getId(), i + 1);
   }

   for (EvaluationViewModel.EvaluationTeamState t : s.getTeams()) {
    List<EvaluationStudentAdapter.StudentEvaluationItem> rows = new ArrayList<>();
    for (EvaluationViewModel.StudentWithGroup x : t.getStudents()) {
     rows.add(new EvaluationStudentAdapter.StudentEvaluationItem(
             x, t.getTeam(), t.isEvaluated(), t.getScoreSur20()));
    }
    all.add(new EvaluationTeamResultsAdapter.TeamItem(
            t.getTeam(), rows, t.isEvaluated(), t.getScoreSur20(),
            dates.get(t.getTeam().getEvaluationId()),
            ranks.get(t.getTeam().getId())));
   }
   render();
  });

  vm.getOperationResult().observe(getViewLifecycleOwner(), r -> {
   if (r == null) {
    return;
   }
   vm.clearOperationResult();
   if (!r.getMessage().trim().isEmpty()) {
    Toast.makeText(requireContext(), r.getMessage(), Toast.LENGTH_SHORT).show();
   }
   if (!r.isSuccess() && r.shouldCloseScreen()) {
    getParentFragmentManager().popBackStack();
   }
  });

  if (sid <= 0) {
   getParentFragmentManager().popBackStack();
   return;
  }
  vm.loadSheet(sid);
 }

 // ------------------------------------------------------------------ affichage

 private void render() {
  if (adapter == null || list == null || empty == null) {
   return;
  }
  List<String> tokens = tokens(query);
  List<EvaluationTeamResultsAdapter.TeamItem> out = new ArrayList<>();

  for (EvaluationTeamResultsAdapter.TeamItem t : all) {
   if (tokens.isEmpty()) {
    out.add(t);
    continue;
   }
   List<EvaluationStudentAdapter.StudentEvaluationItem> rows = new ArrayList<>();
   for (EvaluationStudentAdapter.StudentEvaluationItem x : t.students) {
    if (matches(x, tokens)) {
     rows.add(x);
    }
   }
   // Les équipes sans correspondance sont masquées.
   if (!rows.isEmpty()) {
    out.add(new EvaluationTeamResultsAdapter.TeamItem(
            t.team, rows, t.evaluated, t.score, t.finalizedAt, t.displayNumber));
   }
  }

  adapter.submitList(out, !tokens.isEmpty());

  boolean none = out.isEmpty();
  list.setVisibility(none ? View.GONE : View.VISIBLE);
  empty.setVisibility(none ? View.VISIBLE : View.GONE);
  empty.setText(all.isEmpty()
          ? "Aucune évaluation enregistrée pour cette séance."
          : "Aucune équipe ne contient un étudiant correspondant à cette recherche.");
 }

 // ------------------------------------------------------------------ recherche

 private List<String> tokens(String raw) {
  List<String> out = new ArrayList<>();
  for (String part : norm(raw).split("\\s+")) {
   if (!part.isEmpty()) {
    out.add(part);
   }
  }
  return out;
 }

 /** Chaque mot saisi doit correspondre au nom/prénom, au matricule ou à l'ordre dans le groupe. */
 private boolean matches(EvaluationStudentAdapter.StudentEvaluationItem row, List<String> tokens) {
  Student s = row.getStudent();
  int order = row.getStudentEntry().getGroupOrder();
  String names = norm(s.getFirstName()) + " " + norm(s.getLastName()) + " "
          + norm(s.getFullName()) + " " + norm(s.getFullNameArabic());
  String matricule = norm(s.getMatricule());

  for (String t : tokens) {
   boolean numeric = t.matches("\\d{1,6}");
   boolean ok = names.contains(t);
   if (!ok && numeric) {
    ok = order > 0 && order == Integer.parseInt(t);
    if (!ok) {
     ok = matricule.equals(t);
    }
   } else if (!ok) {
    ok = matricule.contains(t);
   }
   if (!ok) {
    return false;
   }
  }
  return true;
 }

 private String norm(String s) {
  if (s == null) {
   return "";
  }
  return Normalizer.normalize(s, Normalizer.Form.NFD)
          .replaceAll("\\p{M}+", "")
          .toLowerCase(Locale.ROOT)
          .trim();
 }

 // ------------------------------------------------------------------ actions ⋮

 private void openEdit(EvaluationTeamResultsAdapter.TeamItem item) {
  EvaluationSheetFragment f = new EvaluationSheetFragment();
  Bundle b = new Bundle();
  b.putInt("session_id", sid);
  b.putString("session_title", title);
  b.putLong("evaluation_id", item.team.getEvaluationId());
  b.putLong("team_id", item.team.getId());
  f.setArguments(b);
  refresh = true;
  getParentFragmentManager().beginTransaction()
          .replace(R.id.mainContainer, f)
          .addToBackStack("evaluation_edit_" + item.team.getEvaluationId())
          .commit();
 }

 private void confirmDelete(EvaluationTeamResultsAdapter.TeamItem item) {
  long evaluationId = item.team.getEvaluationId();
  int teams = 0;
  for (EvaluationTeamResultsAdapter.TeamItem t : all) {
   if (t.team.getEvaluationId() == evaluationId) {
    teams++;
   }
  }
  String who = item.displayNumber > 0 ? "L’équipe " + item.displayNumber : "Cette équipe";
  String message = who + " sera supprimée définitivement, avec ses résultats"
          + (teams > 1
          ? " et les " + (teams - 1) + " autre(s) équipe(s) de la même évaluation."
          : ".");
  DeletionConfirmationDialog.show(requireContext(), "Supprimer cette évaluation ?", message,
          () -> vm.deleteEvaluation(sid, evaluationId));
 }

 // ------------------------------------------------------------------ cycle de vie

 @Override
 public void onResume() {
  super.onResume();
  if (refresh && vm != null) {
   refresh = false;
   vm.loadSheet(sid);
  }
 }

 @Override
 public void onDestroyView() {
  list = null;
  empty = null;
  search = null;
  adapter = null;
  super.onDestroyView();
 }
}