
package com.example.praticum.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "evaluation_team_member",
        primaryKeys = {"team_id", "student_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = EvaluationTeam.class,
                        parentColumns = "id",
                        childColumns = "team_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Student.class,
                        parentColumns = "student_id",
                        childColumns = "student_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Evaluation.class,
                        parentColumns = "id",
                        childColumns = "evaluation_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("student_id"),
                @Index(
                        value = {"evaluation_id", "student_id"},
                        unique = true
                ),
                @Index("evaluation_id")
        }
)
public class EvaluationTeamMember {

    @NonNull
    @ColumnInfo(name = "team_id")
    private long teamId;
    @NonNull
    @ColumnInfo(name = "student_id")
    private String studentId;

    @ColumnInfo(name = "evaluation_id")
    private long evaluationId;

    public EvaluationTeamMember(
            long teamId,
            String studentId,
            long evaluationId
    ) {
        this.teamId = teamId;
        this.studentId = studentId;
        this.evaluationId = evaluationId;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public long getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(long evaluationId) {
        this.evaluationId = evaluationId;
    }
}
