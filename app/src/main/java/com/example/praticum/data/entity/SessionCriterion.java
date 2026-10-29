
package com.example.praticum.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "session_criterion",
        foreignKeys = {
                @ForeignKey(
                        entity = Session.class,
                        parentColumns = "id",
                        childColumns = "session_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = "session_id")
        }
)
public class SessionCriterion {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "session_id")
    private int sessionId;

    @NonNull
    private String title;

    @Nullable
    private String description;

    @ColumnInfo(name = "max_score")
    private double maxScore;

    private int position;

    public SessionCriterion(
            int sessionId,
            @NonNull String title,
            double maxScore,
            int position
    ) {
        this.sessionId = sessionId;
        this.title = title;
        this.maxScore = maxScore;
        this.position = position;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(double maxScore) {
        this.maxScore = maxScore;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
