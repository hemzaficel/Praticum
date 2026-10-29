package com.example.praticum.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.praticum.data.entity.EvaluationScore;

import java.util.List;

@Dao
public interface EvaluationScoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(EvaluationScore score);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<EvaluationScore> scores);

    @Update
    int update(EvaluationScore score);

    @Query("SELECT * FROM evaluation_score WHERE team_id = :teamId")
    List<EvaluationScore> getByTeamId(long teamId);

    @Query("SELECT * FROM evaluation_score WHERE criterion_id = :criterionId")
    List<EvaluationScore> getByCriterionId(long criterionId);

    @Query("SELECT * FROM evaluation_score WHERE team_id = :teamId AND criterion_id = :criterionId LIMIT 1")
    EvaluationScore getByTeamAndCriterion(long teamId, long criterionId);

    @Query("SELECT SUM(score) FROM evaluation_score WHERE team_id = :teamId")
    Double getTotalScore(long teamId);

    @Query("SELECT COUNT(*) FROM evaluation_score WHERE team_id = :teamId")
    int countByTeamId(long teamId);

    @Query("SELECT COUNT(*) FROM evaluation_score WHERE criterion_id = :criterionId")
    int countByCriterionId(long criterionId);

    @Query("DELETE FROM evaluation_score WHERE team_id = :teamId AND criterion_id = :criterionId")
    int deleteByTeamAndCriterion(long teamId, long criterionId);

    @Query("DELETE FROM evaluation_score WHERE team_id = :teamId")
    int deleteByTeamId(long teamId);
}