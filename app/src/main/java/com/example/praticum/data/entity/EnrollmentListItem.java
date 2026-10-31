package com.example.praticum.data.entity;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;

public class EnrollmentListItem {
    @ColumnInfo(name = "enrollmentId")
    public int enrollmentId;
    @ColumnInfo(name = "studentId")
    public String studentId;
    @ColumnInfo(name = "firstName")
    public String firstName;
    @ColumnInfo(name = "lastName")
    public String lastName;
    @Nullable
    @ColumnInfo(name = "firstNameArabic")
    public String firstNameArabic;
    @Nullable
    @ColumnInfo(name = "lastNameArabic")
    public String lastNameArabic;
    @Nullable
    @ColumnInfo(name = "matricule")
    public String matricule;
    @ColumnInfo(name = "academicYearId")
    public int academicYearId;
    @ColumnInfo(name = "academicYearLabel")
    public String academicYearLabel;
    @ColumnInfo(name = "specializationName")
    public String specializationName;
    @ColumnInfo(name = "specializationId")
    public int specializationId;
    @ColumnInfo(name = "levelName")
    public String levelName;
    @ColumnInfo(name = "levelId")
    public int levelId;
    @ColumnInfo(name = "groupId")
    public int groupId;
    @ColumnInfo(name = "groupCode")
    public String groupCode;
    @ColumnInfo(name = "groupOrder")
    public int groupOrder;
    @ColumnInfo(name = "status")
    public String status;
    @ColumnInfo(name = "enrollmentDate")
    public String enrollmentDate;
}
