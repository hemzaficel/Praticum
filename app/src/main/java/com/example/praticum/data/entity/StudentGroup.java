package com.example.praticum.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "student_group",
        foreignKeys = {
                @ForeignKey(
                        entity = AcademicYear.class,
                        parentColumns = "id",
                        childColumns = "academic_year_id",
                        onDelete = ForeignKey.RESTRICT
                ),
                @ForeignKey(
                        entity = Level.class,
                        parentColumns = "id",
                        childColumns = "level_id",
                        onDelete = ForeignKey.SET_NULL
                )
        },
        indices = {
                @Index(
                        value = {"code", "academic_year_id", "level_id"},
                        unique = true
                ),
                @Index(value = {"academic_year_id"}),
                @Index(value = {"level_id"})
        }
)
public class StudentGroup {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "code")
    private String code;

    @ColumnInfo(name = "academic_year_id")
    private int academicYearId;

    @ColumnInfo(name = "level_id")
    private Integer levelId;

    @ColumnInfo(name = "subject")
    private String subject;

    @ColumnInfo(name = "description")
    private String description;

    public StudentGroup(
            String name,
            String code,
            int academicYearId,
            Integer levelId,
            String subject,
            String description
    ) {
        this.name = name;
        this.code = code;
        this.academicYearId = academicYearId;
        this.levelId = levelId;
        this.subject = subject;
        this.description = description;
    }

    @androidx.room.Ignore
    public StudentGroup() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(int academicYearId) {
        this.academicYearId = academicYearId;
    }

    public Integer getLevelId() {
        return levelId;
    }

    public void setLevelId(Integer levelId) {
        this.levelId = levelId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}