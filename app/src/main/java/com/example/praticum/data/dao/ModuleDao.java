package com.example.praticum.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.praticum.data.entity.Course;

import java.util.List;

@Dao
public interface ModuleDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Course module);

    @Update
    int update(Course module);

    @Delete
    int delete(Course module);

    @Query("SELECT * FROM Course ORDER BY name COLLATE NOCASE ASC")
    List<Course> getAll();

    @Query("SELECT * FROM Course WHERE id = :id LIMIT 1")
    Course getById(long id);

    @Query("SELECT * FROM Course WHERE level_id = :levelId ORDER BY name COLLATE NOCASE ASC")
    List<Course> getByLevelId(long levelId);

    @Query("SELECT * FROM Course WHERE code = :code COLLATE NOCASE LIMIT 1")
    Course getByCode(String code);

    @Query("SELECT COUNT(*) FROM Course WHERE code = :code COLLATE NOCASE")
    int countByCode(String code);

    @Query("SELECT COUNT(*) FROM Course WHERE code = :code COLLATE NOCASE AND id != :excludeId")
    int countByCodeExcludingId(String code, long excludeId);

    @Query("SELECT COUNT(*) FROM Course WHERE level_id = :levelId")
    int countByLevelId(long levelId);

    @Query("SELECT * FROM Course WHERE level_id = :levelId AND semester = :semester ORDER BY name COLLATE NOCASE ASC")
    List<Course> getByLevelAndSemester(long levelId, int semester);


    @Query("SELECT COUNT(*) FROM Course " +
            "WHERE code = :code COLLATE NOCASE " +
            "AND level_id = :levelId")
    int countByCodeAndLevel(String code, int levelId);

    @Query("SELECT COUNT(*) FROM Course " +
            "WHERE code = :code COLLATE NOCASE " +
            "AND level_id = :levelId " +
            "AND id != :excludeId")
    int countByCodeAndLevelExcludingId(
            String code, int levelId, int excludeId);
}