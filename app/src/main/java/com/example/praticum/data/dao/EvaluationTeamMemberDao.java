package com.example.praticum.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.praticum.data.entity.EvaluationTeamMember;

import java.util.List;

@Dao
public interface EvaluationTeamMemberDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(EvaluationTeamMember member);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertAll(List<EvaluationTeamMember> members);

    @Update
    int update(EvaluationTeamMember member);

    @Delete
    int delete(EvaluationTeamMember member);

    @Query("SELECT * FROM evaluation_team_member WHERE team_id = :teamId")
    List<EvaluationTeamMember> getByTeamId(long teamId);

    @Query("SELECT * FROM evaluation_team_member WHERE evaluation_id = :evaluationId")
    List<EvaluationTeamMember> getByEvaluationId(long evaluationId);

    @Query("SELECT * FROM evaluation_team_member WHERE team_id = :teamId AND student_id = :studentId LIMIT 1")
    EvaluationTeamMember getByTeamAndStudent(long teamId, String studentId);

    // Vérifie qu'un étudiant n'appartient pas déjà à une autre équipe de cette évaluation.
    @Query("SELECT COUNT(*) FROM evaluation_team_member WHERE evaluation_id = :evaluationId AND student_id = :studentId")
    int countStudentInEvaluation(long evaluationId, String studentId);

    @Query("DELETE FROM evaluation_team_member WHERE team_id = :teamId AND student_id = :studentId")
    int removeStudentFromTeam(long teamId, String studentId);

    @Query("DELETE FROM evaluation_team_member WHERE team_id = :teamId")
    int deleteByTeamId(long teamId);

    @Query("DELETE FROM evaluation_team_member WHERE evaluation_id = :evaluationId")
    int deleteByEvaluationId(long evaluationId);
}