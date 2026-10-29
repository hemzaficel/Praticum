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

    @Query("SELECT * FROM student WHERE group_id = :groupId ORDER BY group_order ASC")
    List<Student> getByGroupId(long groupId);

    @Query("SELECT * FROM student WHERE matricule = :matricule LIMIT 1")
    Student getByMatricule(String matricule);

    @Query("SELECT COUNT(*) FROM student WHERE matricule = :matricule")
    int countByMatricule(String matricule);

    @Query("SELECT COUNT(*) FROM student WHERE matricule = :matricule AND student_id != :excludeStudentId")
    int countByMatriculeExcludingId(String matricule, String excludeStudentId);

    @Query("SELECT * FROM student WHERE group_id = :groupId AND (last_name LIKE '%' || :query || '%' OR first_name LIKE '%' || :query || '%' OR matricule LIKE '%' || :query || '%') ORDER BY group_order ASC")
    List<Student> searchInGroup(long groupId, String query);

    @Query("SELECT * FROM student WHERE last_name LIKE '%' || :query || '%' OR first_name LIKE '%' || :query || '%' OR matricule LIKE '%' || :query || '%' ORDER BY last_name COLLATE NOCASE, first_name COLLATE NOCASE")
    List<Student> search(String query);

    @Query("SELECT MAX(group_order) FROM student WHERE group_id = :groupId")
    Integer getMaxGroupOrder(long groupId);

    @Query("SELECT COUNT(*) FROM student WHERE group_id = :groupId")
    int countByGroupId(long groupId);

    @Query("SELECT COUNT(*) FROM student")
    int countAll();
}