package com.example.praticum.ui.attendance;

import java.util.ArrayList;
import java.util.List;

public class AttendanceSheet {

    private int sessionId;
    private String date;
    private String startTime;
    private String endTime;
    private String room;
    private String groupName;

    private final List<AttendanceRecordMemory> records = new ArrayList<>();

    public AttendanceSheet(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<AttendanceRecordMemory> getRecords() {
        return new ArrayList<>(records);
    }

    public void setRecords(List<AttendanceRecordMemory> newRecords) {
        records.clear();

        if (newRecords != null) {
            records.addAll(newRecords);
        }
    }

    public void addRecord(AttendanceRecordMemory record) {
        if (record != null) {
            records.add(record);
        }
    }
}
