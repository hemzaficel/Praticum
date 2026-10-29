
package com.example.praticum.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "module",
        foreignKeys = {
                @ForeignKey(
                        entity = Level.class,
                        parentColumns = "id",
                        childColumns = "level_id",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {
                @Index(
                        value = {"code", "level_id"},
                        unique = true
                ),
                @Index(value = {"level_id"})
        }
)
public class Module {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "code")
    private String code;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "level_id")
    private int levelId;

    @ColumnInfo(name = "semester")
    private int semester;

    @ColumnInfo(name = "coefficient")
    private double coefficient;

    @ColumnInfo(name = "description")
    private String description;

    public Module(
            String code,
            String name,
            int levelId,
            int semester,
            double coefficient,
            String description
    ) {
        this.code = code;
        this.name = name;
        this.levelId = levelId;
        this.semester = semester;
        this.coefficient = coefficient;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(double coefficient) {
        this.coefficient = coefficient;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
