package com.example.praticum.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.praticum.data.entity.Specialization;
import java.util.List;

@Dao
public interface SpecializationDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Specialization specialization);

    @Update
    int update(Specialization specialization);

    @Delete
    int delete(Specialization specialization);

    @Query("SELECT * FROM specialization ORDER BY name COLLATE NOCASE ASC")
    List<Specialization> getAll();

    @Query("SELECT * FROM specialization WHERE id = :id LIMIT 1")
    Specialization getById(int id);

    @Query("SELECT * FROM specialization WHERE name = :name COLLATE NOCASE LIMIT 1")
    Specialization getByName(String name);

    @Query("SELECT COUNT(*) FROM specialization WHERE code = :code COLLATE NOCASE")
    int countByCode(String code);
}

