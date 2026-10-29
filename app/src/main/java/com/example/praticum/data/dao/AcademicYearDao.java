package com.example.praticum.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.praticum.data.entity.AcademicYear;

import java.util.List;

@Dao
public interface AcademicYearDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(AcademicYear academicYear);

    @Update
    int update(AcademicYear academicYear);

    @Delete
    int delete(AcademicYear academicYear);

    @Query("SELECT * FROM academic_year ORDER BY start_year DESC")
    List<AcademicYear> getAll();

    @Query("SELECT * FROM academic_year WHERE id = :id LIMIT 1")
    AcademicYear getById(long id);

    @Query("SELECT * FROM academic_year WHERE is_active = 1 LIMIT 1")
    AcademicYear getActive();

    @Query("SELECT COUNT(*) FROM academic_year WHERE start_year = :startYear")
    int countByStartYear(int startYear);

    @Query("SELECT COUNT(*) FROM academic_year WHERE start_year = :startYear AND id != :excludeId")
    int countByStartYearExcludingId(int startYear, long excludeId);

    // Désactive toutes les années ; l'activation se fera ensuite dans une transaction.
    @Query("UPDATE academic_year SET is_active = 0")
    int deactivateAll();

    @Query("UPDATE academic_year SET is_active = 1 WHERE id = :id")
    int activateById(long id);

    @Query("SELECT COUNT(*) FROM student_group WHERE academic_year_id = :academicYearId")
    int countGroups(long academicYearId);

    @Query("DELETE FROM academic_year WHERE id = :id")
    int deleteById(long id);
}