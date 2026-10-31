package com.example.praticum.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.praticum.data.entity.Enrollment;
import java.util.List;

@Dao
public abstract class EnrollmentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    public abstract long insert(Enrollment enrollment);

    @Update
    public abstract int update(Enrollment enrollment);

    @Delete
    public abstract int delete(Enrollment enrollment);

    @Query("SELECT * FROM enrollment WHERE id = :id LIMIT 1")
    public abstract Enrollment getById(int id);

    @Query("SELECT * FROM enrollment WHERE student_id = :studentId ORDER BY academic_year_id DESC")
    public abstract List<Enrollment> getByStudent(String studentId);

    @Query("SELECT * FROM enrollment WHERE student_id = :studentId AND academic_year_id = :yearId LIMIT 1")
    public abstract Enrollment getByStudentAndYear(String studentId, int yearId);

    @Query("SELECT * FROM enrollment WHERE group_id = :groupId ORDER BY group_order ASC")
    public abstract List<Enrollment> getByGroup(int groupId);

    @Query("SELECT COALESCE(MAX(group_order), 0) + 1 FROM enrollment WHERE group_id = :groupId")
    protected abstract int nextGroupOrder(int groupId);

    @Query("SELECT COUNT(*) FROM student_group WHERE id = :groupId AND academic_year_id = :yearId AND level_id = :levelId")
    public abstract int countMatchingGroup(int groupId, int yearId, int levelId);

    @Transaction
    public long insertAssigningGroupOrder(Enrollment enrollment) {
        if (countMatchingGroup(enrollment.getGroupId(), enrollment.getAcademicYearId(), enrollment.getLevelId()) == 0) {
            throw new IllegalArgumentException("Group academic year and level must match enrollment.");
        }
        enrollment.setGroupOrder(nextGroupOrder(enrollment.getGroupId()));
        return insert(enrollment);
    }

    @Query("SELECT e.id AS enrollmentId, e.student_id AS studentId, s.first_name AS firstName, s.last_name AS lastName, s.first_name_arabic AS firstNameArabic, s.last_name_arabic AS lastNameArabic, s.matricule AS matricule, e.academic_year_id AS academicYearId, (y.start_year || '-' || y.end_year) AS academicYearLabel, sp.name AS specializationName, e.specialization_id AS specializationId, l.name AS levelName, e.level_id AS levelId, e.group_id AS groupId, g.code AS groupCode, e.group_order AS groupOrder, e.status AS status, e.enrollment_date AS enrollmentDate FROM enrollment e INNER JOIN student s ON s.student_id = e.student_id INNER JOIN academic_year y ON y.id = e.academic_year_id INNER JOIN specialization sp ON sp.id = e.specialization_id INNER JOIN level l ON l.id = e.level_id INNER JOIN student_group g ON g.id = e.group_id ORDER BY y.start_year DESC, g.code COLLATE NOCASE, e.group_order ASC")
    public abstract List<com.example.praticum.data.entity.EnrollmentListItem> getAllWithDetails();

    @Query("SELECT e.id AS enrollmentId, e.student_id AS studentId, s.first_name AS firstName, s.last_name AS lastName, s.first_name_arabic AS firstNameArabic, s.last_name_arabic AS lastNameArabic, s.matricule AS matricule, e.academic_year_id AS academicYearId, (y.start_year || '-' || y.end_year) AS academicYearLabel, sp.name AS specializationName, e.specialization_id AS specializationId, l.name AS levelName, e.level_id AS levelId, e.group_id AS groupId, g.code AS groupCode, e.group_order AS groupOrder, e.status AS status, e.enrollment_date AS enrollmentDate FROM enrollment e INNER JOIN student s ON s.student_id = e.student_id INNER JOIN academic_year y ON y.id = e.academic_year_id INNER JOIN specialization sp ON sp.id = e.specialization_id INNER JOIN level l ON l.id = e.level_id INNER JOIN student_group g ON g.id = e.group_id WHERE e.id = :enrollmentId LIMIT 1")
    public abstract com.example.praticum.data.entity.EnrollmentListItem getDetailsById(int enrollmentId);

    @Query("SELECT COALESCE(MAX(group_order), 0) + 1 FROM enrollment WHERE group_id = :groupId")
    public abstract int getNextGroupOrder(int groupId);

    @Query("SELECT COUNT(DISTINCT e.student_id) FROM enrollment e INNER JOIN academic_year y ON y.id = e.academic_year_id WHERE e.level_id = :levelId AND e.specialization_id = :specializationId AND y.is_active = 1")
    public abstract int countStudentsInActiveYearByLevelAndSpecialization(int levelId, int specializationId);

    @Query("SELECT e.* FROM enrollment e INNER JOIN academic_year y ON y.id = e.academic_year_id WHERE e.student_id = :studentId AND y.is_active = 1 ORDER BY e.id DESC LIMIT 1")
    public abstract Enrollment getActiveByStudent(String studentId);

    @Query("DELETE FROM enrollment WHERE student_id = :studentId")
    public abstract int deleteByStudent(String studentId);
}





