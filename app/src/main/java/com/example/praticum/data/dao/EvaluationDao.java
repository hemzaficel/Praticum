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

    @Query("SELECT * FROM evaluation WHERE session_id = :sessionId LIMIT 1")
    Evaluation getBySessionId(long sessionId);

    @Query("SELECT COUNT(*) FROM evaluation WHERE session_id = :sessionId")
    int countBySessionId(long sessionId);

    // Le mode de notation retenu est uniquement « Note commune ».
    @Query("UPDATE evaluation SET scoring_mode = 'Note commune' WHERE id = :evaluationId")
    int setCommonScoringMode(long evaluationId);
}