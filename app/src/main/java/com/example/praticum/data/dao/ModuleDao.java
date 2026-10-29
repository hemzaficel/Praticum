package com.example.praticum.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.praticum.data.entity.Module;

import java.util.List;

@Dao
public interface ModuleDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Module module);

    @Update
    int update(Module module);

    @Delete
    int delete(Module module);

    @Query("SELECT * FROM module ORDER BY name COLLATE NOCASE ASC")
    List<Module> getAll();

    @Query("SELECT * FROM module WHERE id = :id LIMIT 1")
    Module getById(long id);

    @Query("SELECT * FROM module WHERE level_id = :levelId ORDER BY name COLLATE NOCASE ASC")
    List<Module> getByLevelId(long levelId);

    @Query("SELECT * FROM module WHERE code = :code COLLATE NOCASE LIMIT 1")
    Module getByCode(String code);

    @Query("SELECT COUNT(*) FROM module WHERE code = :code COLLATE NOCASE")
    int countByCode(String code);

    @Query("SELECT COUNT(*) FROM module WHERE code = :code COLLATE NOCASE AND id != :excludeId")
    int countByCodeExcludingId(String code, long excludeId);

    @Query("SELECT COUNT(*) FROM module WHERE level_id = :levelId")
    int countByLevelId(long levelId);

    @Query("SELECT * FROM module WHERE level_id = :levelId AND semester = :semester ORDER BY name COLLATE NOCASE ASC")
    List<Module> getByLevelAndSemester(long levelId, int semester);


    @Query("SELECT COUNT(*) FROM module " +
            "WHERE code = :code COLLATE NOCASE " +
            "AND level_id = :levelId")
    int countByCodeAndLevel(String code, int levelId);

    @Query("SELECT COUNT(*) FROM module " +
            "WHERE code = :code COLLATE NOCASE " +
            "AND level_id = :levelId " +
            "AND id != :excludeId")
    int countByCodeAndLevelExcludingId(
            String code, int levelId, int excludeId);
}