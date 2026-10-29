
package com.example.praticum.ui.evaluation;

import com.example.praticum.ui.attendance.AttendanceRecordMemory;

import java.util.ArrayList;
import java.util.List;

public class EvaluationTeamMemory {

    private final int id;
    private String name;

    private boolean evaluationTerminee = false;

    public boolean isEvaluationTerminee() {
        return evaluationTerminee;
    }

    public void setEvaluationTerminee(boolean evaluationTerminee) {
        this.evaluationTerminee = evaluationTerminee;
    }


    private final List<AttendanceRecordMemory> students =
            new ArrayList<>();

    private final List<EvaluationCriterion> criteria =
            new ArrayList<>();

    public EvaluationTeamMemory(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AttendanceRecordMemory> getStudents() {
        return students;
    }

    public List<EvaluationCriterion> getCriteria() {
        return criteria;
    }

    public boolean addStudent(AttendanceRecordMemory student) {
        if (student == null) {
            return false;
        }

        for (AttendanceRecordMemory existing : students) {
            if (existing.getStudentId() == student.getStudentId()) {
                return false;
            }
        }

        students.add(student);
        return true;
    }

    public void removeStudent(int studentId) {
        for (int i = students.size() - 1; i >= 0; i--) {
            if (students.get(i).getStudentId() == studentId) {
                students.remove(i);
            }
        }
    }

    public double getTotalScore() {
        double total = 0;

        for (EvaluationCriterion criterion : criteria) {
            total += criterion.getScore();
        }

        return total;
    }

    public double getMaxScore() {
        double total = 0;

        for (EvaluationCriterion criterion : criteria) {
            total += criterion.getMaxScore();
        }

        return total;
    }
}
