
package com.example.praticum.data.repository;

import androidx.room.RoomDatabase;

import com.example.praticum.data.dao.StudentDao;
import com.example.praticum.data.entity.Student;

import java.util.List;
import java.util.UUID;

public class StudentRepository {

    private final StudentDao studentDao;
    private final RoomDatabase database;

    public StudentRepository(
            RoomDatabase database,
            StudentDao studentDao
    ) {
        this.database = database;
        this.studentDao = studentDao;
    }

    public List<Student> getAll() {
        return studentDao.getAll();
    }

    public Student getById(String studentId) {
        return studentDao.getById(studentId);
    }

    public List<Student> getByGroupId(long groupId) {
        return studentDao.getByGroupId(groupId);
    }

    public List<Student> search(String query) {
        return studentDao.search(query == null ? "" : query.trim());
    }

    public List<Student> searchInGroup(long groupId, String query) {
        return studentDao.searchInGroup(
                groupId,
                query == null ? "" : query.trim()
        );
    }

    public void insert(Student student) {
        validate(student);

        if (student.getMatricule() != null
                && !student.getMatricule().trim().isEmpty()
                && studentDao.countByMatricule(
                        student.getMatricule().trim()) > 0) {
            throw new IllegalArgumentException(
                    "Ce matricule est déjà utilisé.");
        }

        database.runInTransaction(() -> {
            Integer maxOrder = studentDao.getMaxGroupOrder(
                    student.getGroupId());

            student.setGroupOrder(maxOrder == null ? 1 : maxOrder + 1);

            if (student.getStudentId() == null
                    || student.getStudentId().trim().isEmpty()) {
                student.setStudentId(UUID.randomUUID().toString());
            }

            if (student.getMatricule() != null
                    && student.getMatricule().trim().isEmpty()) {
                student.setMatricule(null);
            }

            studentDao.insert(student);
        });
    }

    public void update(Student student) {
        validate(student);

        if (student.getMatricule() != null
                && !student.getMatricule().trim().isEmpty()
                && studentDao.countByMatriculeExcludingId(
                        student.getMatricule().trim(),
                        student.getStudentId()) > 0) {
            throw new IllegalArgumentException(
                    "Ce matricule est déjà utilisé.");
        }

        if (student.getMatricule() != null
                && student.getMatricule().trim().isEmpty()) {
            student.setMatricule(null);
        }

        if (studentDao.update(student) == 0) {
            throw new IllegalArgumentException(
                    "Étudiant introuvable.");
        }
    }

    public void delete(Student student) {
        if (student == null) {
            throw new IllegalArgumentException(
                    "L'étudiant est obligatoire.");
        }

        if (studentDao.delete(student) == 0) {
            throw new IllegalArgumentException(
                    "Étudiant introuvable.");
        }
    }

    private void validate(Student student) {
        if (student == null) {
            throw new IllegalArgumentException(
                    "L'étudiant est obligatoire.");
        }

        if (student.getLastName() == null
                || student.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Le nom est obligatoire.");
        }

        if (student.getFirstName() == null
                || student.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Le prénom est obligatoire.");
        }

        if (student.getGroupId() <= 0) {
            throw new IllegalArgumentException(
                    "Un groupe valide doit être sélectionné.");
        }
    }
}
