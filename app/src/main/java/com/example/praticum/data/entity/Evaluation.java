
package com.example.praticum.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "evaluation",
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
public class Evaluation {

    public static final String SCORING_MODE_COMMON = "Note commune";
    public static final String SCORING_MODE_INDIVIDUAL = "Individuelle";

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "session_id")
    private int sessionId;

    @NonNull
    @ColumnInfo(name = "scoring_mode", defaultValue = "Note commune")
    private String scoringMode = SCORING_MODE_COMMON;

    @ColumnInfo(name = "is_finalized", defaultValue = "0")
    private boolean finalized;

    @Nullable
    @ColumnInfo(name = "finalized_at")
    private Long finalizedAt;

    public Evaluation(int sessionId) {
        this.sessionId = sessionId;
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
    public String getScoringMode() {
        return scoringMode;
    }

    public void setScoringMode(@NonNull String scoringMode) {
        this.scoringMode = scoringMode;
    }

    public boolean isFinalized() { return finalized; }

    public void setFinalized(boolean finalized) { this.finalized = finalized; }

    @Nullable
    public Long getFinalizedAt() { return finalizedAt; }

    public void setFinalizedAt(@Nullable Long finalizedAt) { this.finalizedAt = finalizedAt; }
}
