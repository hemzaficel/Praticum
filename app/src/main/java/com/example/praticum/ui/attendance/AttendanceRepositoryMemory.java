package com.example.praticum.ui.attendance;


import java.util.HashMap;
import java.util.Map;

public class AttendanceRepositoryMemory {

    private static AttendanceRepositoryMemory instance;

    private final Map<String, AttendanceSheet> sheets = new HashMap<>();

    private AttendanceRepositoryMemory() {
    }

    public static synchronized AttendanceRepositoryMemory getInstance() {
        if (instance == null) {
            instance = new AttendanceRepositoryMemory();
        }

        return instance;
    }

    private String makeKey(int sessionId, String groupName) {
        return sessionId + "_" + (groupName == null ? "" : groupName);
    }

    public synchronized AttendanceSheet getSheet(
            int sessionId, String groupName) {

        String key = makeKey(sessionId, groupName);
        AttendanceSheet sheet = sheets.get(key);

        if (sheet == null) {
            sheet = new AttendanceSheet(sessionId);
            sheet.setGroupName(groupName);
            sheet.setRecords(AttendanceDataMemory.getStudents(groupName));
            sheets.put(key, sheet);
        }

        return sheet;
    }

    public synchronized void saveSheet(AttendanceSheet sheet) {
        if (sheet == null) {
            return;
        }

        sheets.put(
                makeKey(sheet.getSessionId(), sheet.getGroupName()),
                sheet
        );
    }

    public synchronized void updateStudentStatus(
            int sessionId,
            String groupName,
            int studentId,
            String status) {

        AttendanceSheet sheet = getSheet(sessionId, groupName);

        for (AttendanceRecordMemory record : sheet.getRecords()) {
            if (record.getStudentId() == studentId) {
                record.setStatus(status);
                break;
            }
        }

        saveSheet(sheet);
    }
}
