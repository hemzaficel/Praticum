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

    @Query("SELECT * FROM student_group WHERE code = :code COLLATE NOCASE AND academic_year_id = :academicYearId AND level_id IS :levelId LIMIT 1")
    StudentGroup getByCodeAndYearAndLevel(String code, long academicYearId, Integer levelId);

    @Query("SELECT COUNT(*) FROM student_group WHERE code = :code COLLATE NOCASE AND academic_year_id = :academicYearId AND level_id IS :levelId")
    int countByCodeAndYearAndLevel(String code, long academicYearId, Integer levelId);

    @Query("SELECT COUNT(*) FROM student_group WHERE code = :code COLLATE NOCASE AND academic_year_id = :academicYearId AND level_id IS :levelId AND id != :excludeId")
    int countByCodeAndYearAndLevelExcludingId(String code, long academicYearId, Integer levelId, long excludeId);

    @Query("SELECT COUNT(*) FROM enrollment WHERE group_id = :groupId")
    int countStudents(long groupId);

    // Suppression refusée si le groupe contient encore des étudiants.
    @Query("DELETE FROM student_group WHERE id = :groupId AND NOT EXISTS (SELECT 1 FROM enrollment WHERE group_id = :groupId)")
    int deleteIfEmpty(long groupId);


    @Query(
            "SELECT g.* " +
                    "FROM student_group AS g " +
                    "INNER JOIN Course AS m ON m.level_id = g.level_id " +
                    "WHERE m.id = :moduleId " +
                    "AND g.academic_year_id = :academicYearId " +
                    "AND g.level_id = m.level_id " +
                    "ORDER BY g.code"
    )
    List<StudentGroup> getGroupsForModuleAndYear(
            int moduleId,
            int academicYearId
    );
}
