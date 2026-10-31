package com.example.praticum.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.praticum.data.entity.Evaluation;

import java.util.List;

@Dao
public interface EvaluationDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Evaluation evaluation);

    @Update
    int update(Evaluation evaluation);

    @Delete
    int delete(Evaluation evaluation);

    @Query("SELECT * FROM evaluation ORDER BY id DESC")
    List<Evaluation> getAll();

    @Query("SELECT * FROM evaluation WHERE id = :id LIMIT 1")
    Evaluation getById(long id);

    @Query("SELECT * FROM evaluation WHERE session_id = :sessionId ORDER BY id DESC LIMIT 1")
    Evaluation getBySessionId(long sessionId);

    @Query("SELECT * FROM evaluation WHERE session_id = :sessionId ORDER BY id DESC LIMIT 1")
    Evaluation getLatestBySessionId(long sessionId);

    @Query("SELECT * FROM evaluation WHERE session_id = :sessionId AND is_finalized = 1 ORDER BY id DESC")
    List<Evaluation> getFinalizedBySessionId(long sessionId);

    @Query("SELECT COUNT(*) FROM evaluation WHERE session_id = :sessionId")
    int countBySessionId(long sessionId);

    // Le mode de notation retenu est uniquement « Note commune ».
    @Query("UPDATE evaluation SET scoring_mode = 'Note commune' WHERE id = :evaluationId")
    int setCommonScoringMode(long evaluationId);

    @Query("UPDATE evaluation SET is_finalized = 1, finalized_at = :finalizedAt WHERE id = :evaluationId")
    int finalizeEvaluation(long evaluationId, long finalizedAt);

    @Query("DELETE FROM evaluation WHERE id = :evaluationId AND session_id = :sessionId")
    int deleteByIdAndSession(long evaluationId, long sessionId);

    @Query("SELECT COUNT(*) FROM evaluation WHERE id = :evaluationId AND is_finalized = 1")
    int isFinalized(long evaluationId);
}
