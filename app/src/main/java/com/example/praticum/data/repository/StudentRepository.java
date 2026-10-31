package com.example.praticum.data.repository;

import androidx.room.RoomDatabase;
import com.example.praticum.data.dao.StudentDao;
import com.example.praticum.data.database.AppDatabase;
import com.example.praticum.data.entity.Enrollment;
import com.example.praticum.data.entity.Student;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StudentRepository {
    private final StudentDao studentDao;
    private final AppDatabase database;

    public StudentRepository(RoomDatabase database, StudentDao studentDao) {
        this.database = (AppDatabase) database;
        this.studentDao = studentDao;
    }

    public List<Student> getAll() { return studentDao.getAll(); }
    public Student getById(String studentId) { return studentDao.getById(studentId); }

    public List<Student> getByGroupId(long groupId) { return studentDao.getByGroupId(groupId); }
    public List<Student> search(String query) { return studentDao.search(query == null ? "" : query.trim()); }
    public List<Student> searchInGroup(long groupId, String query) {
        return studentDao.searchInGroup(groupId, query == null ? "" : query.trim());
    }
    public void insert(Student student) {
        validate(student);
        if (student.getMatricule() != null && !student.getMatricule().trim().isEmpty()
                && studentDao.countByMatricule(student.getMatricule().trim()) > 0) {
            throw new IllegalArgumentException("Ce matricule est déjà utilisé.");
        }
        database.runInTransaction(() -> {
            prepare(student);
            studentDao.insert(student);
        });
    }

    public void update(Student student) {
        validate(student);
        if (student.getMatricule() != null && !student.getMatricule().trim().isEmpty()
                && studentDao.countByMatriculeExcludingId(student.getMatricule().trim(), student.getStudentId()) > 0) {
            throw new IllegalArgumentException("Ce matricule est déjà utilisé.");
        }
        prepare(student);
        if (studentDao.update(student) == 0) throw new IllegalArgumentException("Étudiant introuvable.");
    }

    public void delete(Student student) {
        if (student == null) throw new IllegalArgumentException("L'étudiant est obligatoire.");
        database.runInTransaction(() -> {
            database.enrollmentDao().deleteByStudent(student.getStudentId());
            if (studentDao.delete(student) == 0) throw new IllegalArgumentException("Étudiant introuvable.");
        });
    }

    private void prepare(Student student) {
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) student.setStudentId(UUID.randomUUID().toString());
        if (student.getMatricule() != null) {
            String matricule = student.getMatricule().trim();
            student.setMatricule(matricule.isEmpty() ? null : matricule);
        }
    }

    private void validate(Student student) {
        if (student == null) throw new IllegalArgumentException("L'étudiant est obligatoire.");
        if (student.getLastName() == null || student.getLastName().trim().isEmpty()) throw new IllegalArgumentException("Le nom est obligatoire.");
        if (student.getFirstName() == null || student.getFirstName().trim().isEmpty()) throw new IllegalArgumentException("Le prénom est obligatoire.");
    }
}


