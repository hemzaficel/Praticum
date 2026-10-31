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

    @Query("SELECT COUNT(*) FROM evaluation_team_member etm INNER JOIN evaluation e ON e.id = etm.evaluation_id WHERE e.session_id = :sessionId AND e.is_finalized = 1 AND e.id != :excludedEvaluationId AND etm.student_id = :studentId")
    int countStudentFinalizedInSessionExceptEvaluation(long sessionId, long excludedEvaluationId, String studentId);

    @Query("SELECT DISTINCT etm.student_id FROM evaluation_team_member etm INNER JOIN evaluation e ON e.id = etm.evaluation_id WHERE e.session_id = :sessionId AND e.is_finalized = 1")
    List<String> getFinalizedStudentIdsBySession(long sessionId);

    @Query("SELECT DISTINCT etm.student_id FROM evaluation_team_member etm INNER JOIN evaluation ev ON ev.id = etm.evaluation_id INNER JOIN enrollment en ON en.student_id = etm.student_id INNER JOIN academic_year ay ON ay.id = en.academic_year_id WHERE ev.session_id = :sessionId AND ev.is_finalized = 1 AND en.level_id = :levelId AND en.specialization_id = :specializationId AND ay.is_active = 1")
    List<String> getFinalizedStudentIdsBySessionLevelAndSpecializationInActiveYear(long sessionId, int levelId, int specializationId);

    @Query("DELETE FROM evaluation_team_member WHERE team_id = :teamId AND student_id = :studentId")
    int removeStudentFromTeam(long teamId, String studentId);

    @Query("DELETE FROM evaluation_team_member WHERE team_id = :teamId")
    int deleteByTeamId(long teamId);

    @Query("DELETE FROM evaluation_team_member WHERE evaluation_id = :evaluationId")
    int deleteByEvaluationId(long evaluationId);
}
