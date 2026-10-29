
package com.example.praticum.data.repository;

import androidx.room.RoomDatabase;

import com.example.praticum.data.dao.AcademicYearDao;
import com.example.praticum.data.entity.AcademicYear;

import java.util.List;

public class AcademicYearRepository {

    private final AcademicYearDao academicYearDao;
    private final RoomDatabase database;

    public AcademicYearRepository(
            RoomDatabase database,
            AcademicYearDao academicYearDao
    ) {
        this.database = database;
        this.academicYearDao = academicYearDao;
    }

    public List<AcademicYear> getAll() {
        return academicYearDao.getAll();
    }

    public AcademicYear getById(long id) {
        return academicYearDao.getById(id);
    }

    public AcademicYear getActive() {
        return academicYearDao.getActive();
    }

    public long insert(AcademicYear academicYear) {
        validate(academicYear);

        if (academicYearDao.countByStartYear(
                academicYear.getStartYear()) > 0) {
            throw new IllegalArgumentException(
                    "Cette année universitaire existe déjà.");
        }

        final long[] insertedId = {-1};

        database.runInTransaction(() -> {
            if (academicYear.isActive()) {
                academicYearDao.deactivateAll();
            }

            insertedId[0] = academicYearDao.insert(academicYear);
        });

        return insertedId[0];
    }

    public void update(AcademicYear academicYear) {
        validate(academicYear);

        if (academicYearDao.countByStartYearExcludingId(
                academicYear.getStartYear(),
                academicYear.getId()) > 0) {
            throw new IllegalArgumentException(
                    "Une autre année utilise déjà cette année de début.");
        }

        database.runInTransaction(() -> {
            if (academicYear.isActive()) {
                academicYearDao.deactivateAll();
            }

            if (academicYearDao.update(academicYear) == 0) {
                throw new IllegalArgumentException(
                        "Année universitaire introuvable.");
            }
        });
    }

    public void setActive(long id) {
        database.runInTransaction(() -> {
            if (academicYearDao.getById(id) == null) {
                throw new IllegalArgumentException(
                        "Année universitaire introuvable.");
            }

            academicYearDao.deactivateAll();
            academicYearDao.activateById(id);
        });
    }

    public void delete(long id) {
        database.runInTransaction(() -> {
            if (academicYearDao.countGroups(id) > 0) {
                throw new IllegalStateException(
                        "Impossible de supprimer cette année : "
                                + "des groupes y sont rattachés.");
            }

            if (academicYearDao.deleteById(id) == 0) {
                throw new IllegalArgumentException(
                        "Année universitaire introuvable.");
            }
        });
    }

    private void validate(AcademicYear academicYear) {
        if (academicYear == null) {
            throw new IllegalArgumentException(
                    "L'année universitaire est obligatoire.");
        }

        if (academicYear.getEndYear()
                != academicYear.getStartYear() + 1) {
            throw new IllegalArgumentException(
                    "L'année de fin doit suivre l'année de début.");
        }

        String startDate = academicYear.getStartDate();
        String endDate = academicYear.getEndDate();

        if (startDate != null && endDate != null
                && endDate.compareTo(startDate) < 0) {
            throw new IllegalArgumentException(
                    "La date de fin doit être postérieure "
                            + "ou égale à la date de début.");
        }
    }
}
