package com.example.praticum.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "academic_year",
        indices = {
                @Index(value = {"start_year"}, unique = true)
        }
)
public class AcademicYear {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "start_year")
    private int startYear;

    @ColumnInfo(name = "end_year")
    private int endYear;

    @ColumnInfo(name = "start_date")
    private String startDate;

    @ColumnInfo(name = "end_date")
    private String endDate;

    @ColumnInfo(name = "is_active")
    private boolean active;

    public AcademicYear(
            int startYear,
            int endYear,
            String startDate,
            String endDate,
            boolean active
    ) {
        this.startYear = startYear;
        this.endYear = endYear;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
    }

    @androidx.room.Ignore
    public AcademicYear() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public int getEndYear() {
        return endYear;
    }

    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getLabel() {
        return startYear + "-" + endYear;
    }
}