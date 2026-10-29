package com.example.praticum.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.praticum.data.entity.Session;

import java.util.List;

@Dao
public interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Session session);

    @Update
    int update(Session session);

    @Delete
    int delete(Session session);

    @Query("SELECT * FROM `Session` ORDER BY id DESC")
    List<Session> getAll();

    @Query("SELECT * FROM `Session` WHERE id = :id LIMIT 1")
    Session getById(long id);

    @Query("SELECT * FROM `Session` WHERE module_id = :moduleId ORDER BY id DESC")
    List<Session> getByModuleId(long moduleId);

    @Query("SELECT * FROM `Session` WHERE status = :status ORDER BY id DESC")
    List<Session> getByStatus(String status);

    @Query("SELECT * FROM `Session` WHERE is_archived = 0 ORDER BY id DESC")
    List<Session> getActiveSessions();

    @Query("SELECT * FROM `Session` WHERE is_archived = 1 ORDER BY id DESC")
    List<Session> getArchivedSessions();

    @Query("SELECT COUNT(*) FROM `Session` WHERE module_id = :moduleId")
    int countByModuleId(long moduleId);

    @Query("UPDATE `Session` SET is_archived = :archived WHERE id = :sessionId")
    int setArchived(long sessionId, boolean archived);

    @Query("UPDATE `Session` SET status = :status WHERE id = :sessionId")
    int updateStatus(long sessionId, String status);
}