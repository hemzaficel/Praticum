package com.example.praticum.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.praticum.data.entity.SessionGroup;

import java.util.List;

@Dao
public interface SessionGroupDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(SessionGroup sessionGroup);

    @Update
    int update(SessionGroup sessionGroup);

    @Delete
    int delete(SessionGroup sessionGroup);

    @Query("SELECT * FROM session_group ORDER BY session_date DESC, start_time ASC")
    List<SessionGroup> getAll();

    @Query("SELECT * FROM session_group WHERE id = :id LIMIT 1")
    SessionGroup getById(long id);

    @Query("SELECT * FROM session_group WHERE session_id = :sessionId ORDER BY session_date ASC, start_time ASC")
    List<SessionGroup> getBySessionId(long sessionId);

    @Query("SELECT * FROM session_group WHERE group_id = :groupId ORDER BY session_date DESC, start_time ASC")
    List<SessionGroup> getByGroupId(long groupId);

    @Query("SELECT * FROM session_group WHERE session_id = :sessionId AND group_id = :groupId LIMIT 1")
    SessionGroup getBySessionAndGroup(long sessionId, long groupId);

    @Query("SELECT COUNT(*) FROM session_group WHERE session_id = :sessionId")
    int countBySessionId(long sessionId);

    @Query("SELECT COUNT(*) FROM session_group WHERE group_id = :groupId")
    int countByGroupId(long groupId);

    @Query("SELECT * FROM session_group WHERE session_date = :date ORDER BY start_time ASC")
    List<SessionGroup> getByDate(String date);

    @Query("SELECT * FROM session_group WHERE group_id = :groupId AND session_date >= :startDate AND session_date <= :endDate ORDER BY session_date ASC, start_time ASC")
    List<SessionGroup> getByGroupAndDateRange(
            long groupId,
            String startDate,
            String endDate
    );
}