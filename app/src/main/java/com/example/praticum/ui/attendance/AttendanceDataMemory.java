package com.example.praticum.ui.attendance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AttendanceDataMemory {

    private AttendanceDataMemory() {
    }

    public static List<String> getGroups() {
        return Arrays.asList("TP-01", "TP-02", "TD-01");
    }

    public static List<AttendanceRecordMemory> getStudents(String groupName) {
        List<AttendanceRecordMemory> students = new ArrayList<>();

        if (groupName == null) {
            return students;
        }

        if (groupName.equals("TP-01")) {
            students.add(new AttendanceRecordMemory(
                    1, "Amine Benali", "أمين بن علي",
                    "20260001", groupName, 1));

            students.add(new AttendanceRecordMemory(
                    2, "Sara Mansouri", "سارة منصوري",
                    "20260002", groupName, 2));

            students.add(new AttendanceRecordMemory(
                    3, "Yacine Haddad", "ياسين حداد",
                    "20260003", groupName, 3));

        } else if (groupName.equals("TP-02")) {
            students.add(new AttendanceRecordMemory(
                    4, "Lina Boudiaf", "لينا بوضياف",
                    "20260004", groupName, 1));

            students.add(new AttendanceRecordMemory(
                    5, "Karim Saidi", "كريم السعيدي",
                    "20260005", groupName, 2));

        } else if (groupName.equals("TD-01")) {
            students.add(new AttendanceRecordMemory(
                    6, "Nour Belkacem", "نور بلقاسم",
                    "20260006", groupName, 1));

            students.add(new AttendanceRecordMemory(
                    7, "Mehdi Cherif", "مهدي شريف",
                    "20260007", groupName, 2));
        }

        return students;
    }
}
