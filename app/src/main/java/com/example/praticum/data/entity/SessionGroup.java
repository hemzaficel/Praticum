
package com.example.praticum.data.entity;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "session_group",
        foreignKeys = {
                @ForeignKey(
                        entity = Session.class,
                        parentColumns = "id",
                        childColumns = "session_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = StudentGroup.class,
                        parentColumns = "id",
                        childColumns = "group_id",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {
                @Index(value = "session_id"),
                @Index(value = "group_id")
        }
)
public class SessionGroup {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "session_id")
    private int sessionId;

    @ColumnInfo(name = "group_id")
    private int groupId;

    @Nullable
    @ColumnInfo(name = "session_date")
    private String sessionDate;

    @Nullable
    @ColumnInfo(name = "start_time")
    private String startTime;

    @Nullable
    @ColumnInfo(name = "end_time")
    private String endTime;

    @Nullable
    private String room;

    public SessionGroup(int sessionId, int groupId) {
        this.sessionId = sessionId;
        this.groupId = groupId;
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

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    @Nullable
    public String getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(@Nullable String sessionDate) {
        this.sessionDate = sessionDate;
    }

    @Nullable
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(@Nullable String startTime) {
        this.startTime = startTime;
    }

    @Nullable
    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(@Nullable String endTime) {
        this.endTime = endTime;
    }

    @Nullable
    public String getRoom() {
        return room;
    }

    public void setRoom(@Nullable String room) {
        this.room = room;
    }
}
