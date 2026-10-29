package com.example.praticum.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.praticum.data.entity.SessionCriterion;

import java.util.List;

@Dao
public interface SessionCriterionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(SessionCriterion criterion);

    @Update
    int update(SessionCriterion criterion);

    @Delete
    int delete(SessionCriterion criterion);

    @Query("SELECT * FROM session_criterion WHERE id = :id LIMIT 1")
    SessionCriterion getById(long id);

    @Query("SELECT * FROM session_criterion WHERE session_id = :sessionId ORDER BY position ASC")
    List<SessionCriterion> getBySessionId(long sessionId);

    @Query("SELECT COUNT(*) FROM session_criterion WHERE session_id = :sessionId")
    int countBySessionId(long sessionId);

    @Query("SELECT SUM(max_score) FROM session_criterion WHERE session_id = :sessionId")
    Double getTotalMaxScore(long sessionId);

    @Query("SELECT MAX(position) FROM session_criterion WHERE session_id = :sessionId")
    Integer getMaxPosition(long sessionId);

    // Empêche la suppression si des notes utilisent déjà ce critère.
    @Query("DELETE FROM session_criterion WHERE id = :criterionId AND NOT EXISTS (SELECT 1 FROM evaluation_score WHERE criterion_id = :criterionId)")
    int deleteIfNotScored(long criterionId);
}