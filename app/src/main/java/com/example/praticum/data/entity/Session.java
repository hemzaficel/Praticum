
package com.example.praticum.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "session",
        foreignKeys = {
                @ForeignKey(
                        entity = Module.class,
                        parentColumns = "id",
                        childColumns = "module_id",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {
                @Index(value = "module_id"),
                @Index(value = {"is_archived", "status"})
        }
)
public class Session {

    public static final String TYPE_COURS = "Cours";
    public static final String TYPE_TD = "TD";
    public static final String TYPE_TP = "TP";
    public static final String TYPE_ATELIER = "Atelier";
    public static final String TYPE_EVALUATION = "Évaluation";

    public static final String STATUS_A_VENIR = "À venir";
    public static final String STATUS_TERMINEE = "Terminée";
    public static final String STATUS_ANNULEE = "Annulée";

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String title;

    @NonNull
    @ColumnInfo(name = "activity_type")
    private String activityType;

    @ColumnInfo(name = "module_id")
    private int moduleId;

    @Nullable
    private String description;

    @NonNull
    private String status = STATUS_A_VENIR;

    @ColumnInfo(name = "is_archived")
    private boolean archived = false;

    public Session(
            @NonNull String title,
            @NonNull String activityType,
            int moduleId
    ) {
        this.title = title;
        this.activityType = activityType;
        this.moduleId = moduleId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(@NonNull String activityType) {
        this.activityType = activityType;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
