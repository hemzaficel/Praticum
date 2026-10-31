package com.example.praticum.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "enrollment",
        foreignKeys = {
                @ForeignKey(entity = Student.class, parentColumns = "student_id", childColumns = "student_id", onDelete = ForeignKey.RESTRICT),
                @ForeignKey(entity = AcademicYear.class, parentColumns = "id", childColumns = "academic_year_id", onDelete = ForeignKey.RESTRICT),
                @ForeignKey(entity = Specialization.class, parentColumns = "id", childColumns = "specialization_id", onDelete = ForeignKey.RESTRICT),
                @ForeignKey(entity = Level.class, parentColumns = "id", childColumns = "level_id", onDelete = ForeignKey.RESTRICT),
                @ForeignKey(entity = StudentGroup.class, parentColumns = "id", childColumns = "group_id", onDelete = ForeignKey.RESTRICT)
        },
        indices = {
                @Index(value = {"student_id", "academic_year_id"}, unique = true),
                @Index(value = {"group_id", "group_order"}, unique = true),
                @Index("academic_year_id"), @Index("specialization_id"), @Index("level_id"), @Index("group_id")
        }
)
public class Enrollment {
    public static final String STATUS_IN_PROGRESS = "En cours";
    public static final String STATUS_VALIDATED = "Validée";
    public static final String STATUS_REPEAT = "Redoublement";
    public static final String STATUS_WITHDRAWN = "Abandonnée";

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "student_id")
    private String studentId;
    @ColumnInfo(name = "academic_year_id")
    private int academicYearId;
    @ColumnInfo(name = "specialization_id")
    private int specializationId;
    @ColumnInfo(name = "level_id")
    private int levelId;
    @ColumnInfo(name = "group_id")
    private int groupId;
    @ColumnInfo(name = "group_order")
    private int groupOrder;
    @NonNull
    @ColumnInfo(name = "status", defaultValue = "En cours")
    private String status = STATUS_IN_PROGRESS;
    @NonNull
    @ColumnInfo(name = "enrollment_date")
    private String enrollmentDate;

    public Enrollment(@NonNull String studentId, int academicYearId, int specializationId,
                      int levelId, int groupId, int groupOrder, @NonNull String status,
                      @NonNull String enrollmentDate) {
        this.studentId = studentId;
        this.academicYearId = academicYearId;
        this.specializationId = specializationId;
        this.levelId = levelId;
        this.groupId = groupId;
        this.groupOrder = groupOrder;
        this.status = status;
        this.enrollmentDate = enrollmentDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(@NonNull String studentId) {
        this.studentId = studentId;
    }

    public int getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(int academicYearId) {
        this.academicYearId = academicYearId;
    }

    public int getSpecializationId() {
        return specializationId;
    }

    public void setSpecializationId(int specializationId) {
        this.specializationId = specializationId;
    }

    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getGroupOrder() {
        return groupOrder;
    }

    public void setGroupOrder(int groupOrder) {
        this.groupOrder = groupOrder;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
    }

    @NonNull
    public String getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(@NonNull String enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
}
