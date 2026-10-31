package com.example.praticum.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.praticum.data.entity.Student;

import java.util.List;

@Dao
public interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(Student student);

    @Update
    int update(Student student);

    @Delete
    int delete(Student student);

    @Query("SELECT * FROM student ORDER BY last_name COLLATE NOCASE, first_name COLLATE NOCASE")
    List<Student> getAll();

    @Query("SELECT * FROM student WHERE student_id = :studentId LIMIT 1")
    Student getById(String studentId);

    @Query("SELECT s.* FROM student s INNER JOIN enrollment e ON e.student_id = s.student_id WHERE e.group_id = :groupId ORDER BY e.group_order ASC")
    List<Student> getByGroupId(long groupId);

    @Query("SELECT * FROM student WHERE matricule = :matricule LIMIT 1")
    Student getByMatricule(String matricule);

    @Query("SELECT * FROM student WHERE email = :email COLLATE NOCASE LIMIT 1")
    Student getByEmail(String email);

    @Query("SELECT COUNT(*) FROM student WHERE matricule = :matricule")
    int countByMatricule(String matricule);

    @Query("SELECT COUNT(*) FROM student WHERE matricule = :matricule AND student_id != :excludeStudentId")
    int countByMatriculeExcludingId(String matricule, String excludeStudentId);

    @Query("SELECT s.* FROM student s INNER JOIN enrollment e ON e.student_id = s.student_id WHERE e.group_id = :groupId AND (s.last_name LIKE '%' || :query || '%' OR s.first_name LIKE '%' || :query || '%' OR s.matricule LIKE '%' || :query || '%') ORDER BY e.group_order ASC")
    List<Student> searchInGroup(long groupId, String query);

    @Query("SELECT * FROM student WHERE last_name LIKE '%' || :query || '%' OR first_name LIKE '%' || :query || '%' OR matricule LIKE '%' || :query || '%' ORDER BY last_name COLLATE NOCASE, first_name COLLATE NOCASE")
    List<Student> search(String query);

    @Query("SELECT COUNT(*) FROM student")
    int countAll();

}

