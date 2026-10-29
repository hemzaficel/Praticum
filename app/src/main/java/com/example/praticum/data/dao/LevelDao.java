package com.example.praticum.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.praticum.data.entity.Level;

import java.util.List;

@Dao
public interface LevelDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Level level);

    @Update
    int update(Level level);

    @Delete
    int delete(Level level);

    @Query("SELECT * FROM level ORDER BY display_order ASC")
    List<Level> getAll();

    @Query("SELECT * FROM level WHERE id = :id LIMIT 1")
    Level getById(long id);

    @Query("SELECT * FROM level WHERE code = :code COLLATE NOCASE LIMIT 1")
    Level getByCode(String code);

    @Query("SELECT COUNT(*) FROM level WHERE code = :code COLLATE NOCASE")
    int countByCode(String code);

    @Query("SELECT COUNT(*) FROM level WHERE code = :code COLLATE NOCASE AND id != :excludeId")
    int countByCodeExcludingId(String code, long excludeId);

    @Query("SELECT MAX(display_order) FROM level")
    Integer getMaxDisplayOrder();

    @Query("SELECT COUNT(*) FROM student_group WHERE level_id = :levelId")
    int countGroups(long levelId);

    @Query("SELECT COUNT(*) FROM Course WHERE level_id = :levelId")
    int countModules(long levelId);
}