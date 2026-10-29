
package com.example.praticum.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "evaluation_team",
        foreignKeys = {
                @ForeignKey(
                        entity = Evaluation.class,
                        parentColumns = "id",
                        childColumns = "evaluation_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(
                        value = {"evaluation_id", "team_number"},
                        unique = true
                )
        }
)
public class EvaluationTeam {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "evaluation_id")
    private long evaluationId;

    @ColumnInfo(name = "team_number")
    private int teamNumber;

    @ColumnInfo(name = "name")
    private String name;

    public EvaluationTeam(long evaluationId, int teamNumber, String name) {
        this.evaluationId = evaluationId;
        this.teamNumber = teamNumber;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(long evaluationId) {
        this.evaluationId = evaluationId;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
