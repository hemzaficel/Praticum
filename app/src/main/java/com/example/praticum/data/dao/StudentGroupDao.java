package com.example.praticum.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.praticum.data.entity.StudentGroup;

import java.util.List;

@Dao
public interface StudentGroupDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(StudentGroup group);

    @Update
    int update(StudentGroup group);

    @Delete
    int delete(StudentGroup group);

    @Query("SELECT * FROM student_group ORDER BY name COLLATE NOCASE ASC")
    List<StudentGroup> getAll();

    @Query("SELECT * FROM student_group WHERE id = :id LIMIT 1")
    StudentGroup getById(long id);

    @Query("SELECT * FROM student_group WHERE academic_year_id = :academicYearId ORDER BY name COLLATE NOCASE ASC")
    List<StudentGroup> getByAcademicYear(long academicYearId);

    @Query("SELECT * FROM student_group WHERE level_id = :levelId AND academic_year_id = :academicYearId ORDER BY name COLLATE NOCASE ASC")
    List<StudentGroup> getByLevelAndAcademicYear(long levelId, long academicYearId);

    @Query("SELECT * FROM student_group WHERE code = :code COLLATE NOCASE AND academic_year_id = :academicYearId LIMIT 1")
    StudentGroup getByCodeAndAcademicYear(String code, long academicYearId);

    @Query("SELECT COUNT(*) FROM student_group WHERE code = :code COLLATE NOCASE AND academic_year_id = :academicYearId")
    int countByCodeAndAcademicYear(String code, long academicYearId);

    @Query("SELECT COUNT(*) FROM student_group WHERE code = :code COLLATE NOCASE AND academic_year_id = :academicYearId AND id != :excludeId")
    int countByCodeAndAcademicYearExcludingId(
            String code,
            long academicYearId,
            long excludeId
    );

    @Query("SELECT COUNT(*) FROM student WHERE group_id = :groupId")
    int countStudents(long groupId);

    // Suppression refusée si le groupe contient encore des étudiants.
    @Query("DELETE FROM student_group WHERE id = :groupId AND NOT EXISTS (SELECT 1 FROM student WHERE group_id = :groupId)")
    int deleteIfEmpty(long groupId);
}