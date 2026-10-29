package com.example.praticum.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "level",
        indices = {
                @Index(value = {"code"}, unique = true)
        }
)
public class Level {

    public static final String CYCLE_LICENCE = "Licence";
    public static final String CYCLE_MASTER = "Master";
    public static final String CYCLE_DOCTORAT = "Doctorat";
    public static final String CYCLE_INGENIEUR = "Ingénieur";

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "code", collate = ColumnInfo.NOCASE)
    private String code;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "cycle")
    private String cycle;

    @ColumnInfo(name = "display_order")
    private int displayOrder;

    @ColumnInfo(name = "description")
    private String description;

    public Level(
            String code,
            String name,
            String cycle,
            int displayOrder,
            String description
    ) {
        this.code = code;
        this.name = name;
        this.cycle = cycle;
        this.displayOrder = displayOrder;
        this.description = description;
    }

    @androidx.room.Ignore
    public Level() {
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

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
