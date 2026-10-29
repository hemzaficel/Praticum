
package com.example.praticum.data.repository;

import androidx.room.RoomDatabase;

import com.example.praticum.data.dao.AttendanceRecordDao;
import com.example.praticum.data.entity.AttendanceRecord;

import java.util.List;

public class AttendanceRecordRepository {

    private final AttendanceRecordDao attendanceDao;
    private final RoomDatabase database;

    public AttendanceRecordRepository(
            RoomDatabase database,
            AttendanceRecordDao attendanceDao
    ) {
        this.database = database;
        this.attendanceDao = attendanceDao;
    }

    public List<AttendanceRecord> getBySessionGroupId(long sessionGroupId) {
        return attendanceDao.getBySessionGroupId(sessionGroupId);
    }

    public AttendanceRecord getByStudentAndSessionGroup(
            long sessionGroupId, String studentId) {
        return attendanceDao.getByStudentAndSessionGroup(
                sessionGroupId, studentId);
    }

    public List<AttendanceRecord> getByStudentId(String studentId) {
        return attendanceDao.getByStudentId(studentId);
    }

    public int countByStatus(long sessionGroupId, String status) {
        validateStatus(status);
        return attendanceDao.countByStatus(sessionGroupId, status);
    }

    public void save(AttendanceRecord record) {
        validate(record);

        database.runInTransaction(() ->
                attendanceDao.insertOrUpdate(record));
    }

    // Supprimer la ligne signifie « Non renseigné ».
    public void setUnreported(long sessionGroupId, String studentId) {
        attendanceDao.deleteRecord(sessionGroupId, studentId);
    }

    public void deleteBySessionGroupId(long sessionGroupId) {
        attendanceDao.deleteBySessionGroupId(sessionGroupId);
    }

    private void validate(AttendanceRecord record) {
        if (record == null) {
            throw new IllegalArgumentException(
                    "L'enregistrement de présence est obligatoire.");
        }

        if (record.getSessionGroupId() <= 0
                || record.getStudentId() == null
                || record.getStudentId().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "La séance-groupe et l'étudiant doivent être valides.");
        }

        validateStatus(record.getStatus());
    }

    private void validateStatus(String status) {
        if (status == null
                || !(status.equals("Présent")
                || status.equals("Absent")
                || status.equals("En retard"))) {
            throw new IllegalArgumentException(
                    "Statut de présence invalide.");
        }
    }
}
