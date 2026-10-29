package com.example.praticum.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.praticum.data.entity.AttendanceRecord;

import java.util.List;

@Dao
public interface AttendanceRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(AttendanceRecord record);

    @Query("SELECT * FROM attendance_record WHERE session_group_id = :sessionGroupId")
    List<AttendanceRecord> getBySessionGroupId(long sessionGroupId);

    @Query("SELECT * FROM attendance_record WHERE session_group_id = :sessionGroupId AND student_id = :studentId LIMIT 1")
    AttendanceRecord getByStudentAndSessionGroup(
            long sessionGroupId,
            String studentId
    );

    @Query("SELECT * FROM attendance_record WHERE student_id = :studentId ORDER BY session_group_id DESC")
    List<AttendanceRecord> getByStudentId(String studentId);

    @Query("SELECT COUNT(*) FROM attendance_record WHERE session_group_id = :sessionGroupId AND status = :status")
    int countByStatus(long sessionGroupId, String status);

    @Query("DELETE FROM attendance_record WHERE session_group_id = :sessionGroupId AND student_id = :studentId")
    int deleteRecord(long sessionGroupId, String studentId);

    @Query("DELETE FROM attendance_record WHERE session_group_id = :sessionGroupId")
    int deleteBySessionGroupId(long sessionGroupId);

    @Query("DELETE FROM attendance_record WHERE student_id = :studentId")
    int deleteByStudentId(String studentId);
}