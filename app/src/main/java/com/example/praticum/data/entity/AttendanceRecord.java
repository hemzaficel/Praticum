
package com.example.praticum.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "attendance_record",
        primaryKeys = {"session_group_id", "student_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = SessionGroup.class,
                        parentColumns = "id",
                        childColumns = "session_group_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Student.class,
                        parentColumns = "student_id",
                        childColumns = "student_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = "student_id")
        }
)
public class AttendanceRecord {

    public static final String PRESENT = "Présent";
    public static final String ABSENT = "Absent";
    public static final String LATE = "En retard";

    @ColumnInfo(name = "session_group_id")
    private int sessionGroupId;

    @NonNull
    @ColumnInfo(name = "student_id")
    private String studentId;

    @NonNull
    private String status;

    public AttendanceRecord(
            int sessionGroupId,
            @NonNull String studentId,
            @NonNull String status
    ) {
        this.sessionGroupId = sessionGroupId;
        this.studentId = studentId;
        this.status = status;
    }

    public int getSessionGroupId() {
        return sessionGroupId;
    }

    public void setSessionGroupId(int sessionGroupId) {
        this.sessionGroupId = sessionGroupId;
    }

    @NonNull
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(@NonNull String studentId) {
        this.studentId = studentId;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
    }
}
