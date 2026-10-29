
package com.example.praticum.data.repository;

import com.example.praticum.data.dao.StudentGroupDao;
import com.example.praticum.data.entity.StudentGroup;

import java.util.List;

public class StudentGroupRepository {

    private final StudentGroupDao groupDao;

    public StudentGroupRepository(StudentGroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public List<StudentGroup> getAll() {
        return groupDao.getAll();
    }

    public StudentGroup getById(long id) {
        return groupDao.getById(id);
    }

    public List<StudentGroup> getByAcademicYear(long academicYearId) {
        return groupDao.getByAcademicYear(academicYearId);
    }

    public List<StudentGroup> getByLevelAndAcademicYear(
            long levelId, long academicYearId) {
        return groupDao.getByLevelAndAcademicYear(
                levelId, academicYearId);
    }

    public long insert(StudentGroup group) {
        validate(group);
        checkUniqueCode(group, 0);
        return groupDao.insert(group);
    }

    public void update(StudentGroup group) {
        validate(group);
        checkUniqueCode(group, group.getId());

        if (groupDao.update(group) == 0) {
            throw new IllegalArgumentException(
                    "Groupe introuvable.");
        }
    }

    public void delete(StudentGroup group) {
        if (group == null) {
            throw new IllegalArgumentException(
                    "Le groupe est obligatoire.");
        }

        if (groupDao.countStudents(group.getId()) > 0) {
            throw new IllegalStateException(
                    "Impossible de supprimer ce groupe : "
                            + "des étudiants y sont affectés.");
        }

        if (groupDao.deleteIfEmpty(group.getId()) == 0) {
            throw new IllegalStateException(
                    "Le groupe n'a pas pu être supprimé.");
        }
    }

    private void checkUniqueCode(StudentGroup group, long excludeId) {
        int count;

        if (excludeId == 0) {
            count = groupDao.countByCodeAndAcademicYear(
                    group.getCode(), group.getAcademicYearId());
        } else {
            count = groupDao.countByCodeAndAcademicYearExcludingId(
                    group.getCode(),
                    group.getAcademicYearId(),
                    excludeId);
        }

        if (count > 0) {
            throw new IllegalArgumentException(
                    "Ce code est déjà utilisé pour cette année universitaire.");
        }
    }

    private void validate(StudentGroup group) {
        if (group == null) {
            throw new IllegalArgumentException(
                    "Le groupe est obligatoire.");
        }

        if (group.getName() == null
                || group.getName().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Le nom du groupe est obligatoire.");
        }

        if (group.getCode() == null
                || group.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Le code du groupe est obligatoire.");
        }

        if (group.getAcademicYearId() <= 0) {
            throw new IllegalArgumentException(
                    "Une année universitaire doit être sélectionnée.");
        }
    }
}
