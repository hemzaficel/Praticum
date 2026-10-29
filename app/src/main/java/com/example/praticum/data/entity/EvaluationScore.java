
package com.example.praticum.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(
        tableName = "evaluation_score",
        primaryKeys = {"team_id", "criterion_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = EvaluationTeam.class,
                        parentColumns = "id",
                        childColumns = "team_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = SessionCriterion.class,
                        parentColumns = "id",
                        childColumns = "criterion_id",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {
                @Index("criterion_id")
        }
)
public class EvaluationScore {

    @ColumnInfo(name = "team_id")
    private long teamId;

    @ColumnInfo(name = "criterion_id")
    private long criterionId;

    @ColumnInfo(name = "score", defaultValue = "0")
    private double score;

    @Ignore
    public EvaluationScore(long teamId, long criterionId) {
        this.teamId = teamId;
        this.criterionId = criterionId;
        this.score = 0.0;
    }

    public EvaluationScore(long teamId, long criterionId, double score) {
        this.teamId = teamId;
        this.criterionId = criterionId;
        this.score = score;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public long getCriterionId() {
        return criterionId;
    }

    public void setCriterionId(long criterionId) {
        this.criterionId = criterionId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
