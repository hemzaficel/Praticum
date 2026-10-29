package com.example.praticum.ui.attendance;

public class AttendanceRecordMemory {

    public static final String PRESENT = "Présent";
    public static final String ABSENT = "Absent";
    public static final String LATE = "En retard";
    public static final String UNMARKED = "Non renseigné";

    private int studentId;
    private String studentName;
    private String arabicName;
    private String matricule;
    private String groupName;
    private int groupOrder;
    private String status;

    public AttendanceRecordMemory(
            int studentId,
            String studentName,
            String arabicName,
            String matricule,
            String groupName,
            int groupOrder) {

        this.studentId = studentId;
        this.studentName = studentName;
        this.arabicName = arabicName;
        this.matricule = matricule;
        this.groupName = groupName;
        this.groupOrder = groupOrder;
        this.status = UNMARKED;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getArabicName() {
        return arabicName;
    }

    public String getMatricule() {
        return matricule;
    }

    public String getGroupName() {
        return groupName;
    }

    public int getGroupOrder() {
        return groupOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? UNMARKED : status;
    }
}
