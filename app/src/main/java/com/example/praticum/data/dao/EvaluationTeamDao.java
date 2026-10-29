package com.example.praticum.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.praticum.data.entity.EvaluationTeam;

import java.util.List;

@Dao
public interface EvaluationTeamDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(EvaluationTeam team);

    @Update
    int update(EvaluationTeam team);

    @Delete
    int delete(EvaluationTeam team);

    @Query("SELECT * FROM evaluation_team WHERE id = :id LIMIT 1")
    EvaluationTeam getById(long id);

    @Query("SELECT * FROM evaluation_team WHERE evaluation_id = :evaluationId ORDER BY team_number ASC")
    List<EvaluationTeam> getByEvaluationId(long evaluationId);

    @Query("SELECT COUNT(*) FROM evaluation_team WHERE evaluation_id = :evaluationId")
    int countByEvaluationId(long evaluationId);

    @Query("SELECT MAX(team_number) FROM evaluation_team WHERE evaluation_id = :evaluationId")
    Integer getMaxTeamNumber(long evaluationId);

    @Query("SELECT COUNT(*) FROM evaluation_team WHERE evaluation_id = :evaluationId AND team_number = :teamNumber")
    int countByEvaluationAndTeamNumber(long evaluationId, int teamNumber);

    @Query("DELETE FROM evaluation_team WHERE id = :teamId")
    int deleteById(long teamId);
}