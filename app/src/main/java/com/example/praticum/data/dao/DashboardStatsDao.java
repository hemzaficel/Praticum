package com.example.praticum.data.dao;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface DashboardStatsDao {

    @Query("SELECT COUNT(*) FROM student")
    int countStudents();

    @Query("SELECT COUNT(*) FROM student_group WHERE academic_year_id = :academicYearId")
    int countGroupsForAcademicYear(int academicYearId);

    @Query("SELECT COUNT(*) FROM enrollment WHERE academic_year_id = :academicYearId")
    int countEnrollmentsForAcademicYear(int academicYearId);

    @Query("SELECT COUNT(DISTINCT e.id) FROM evaluation e " +
            "WHERE EXISTS (" +
            "SELECT 1 FROM session s " +
            "INNER JOIN session_group sg ON sg.session_id = s.id " +
            "INNER JOIN student_group g ON g.id = sg.group_id " +
            "WHERE s.id = e.session_id AND g.academic_year_id = :academicYearId" +
            ") AND EXISTS (" +
            "SELECT 1 FROM evaluation_team t " +
            "INNER JOIN evaluation_score es ON es.team_id = t.id " +
            "WHERE t.evaluation_id = e.id AND es.score > 0" +
            ")")
    int countEvaluationsPerformedForAcademicYear(int academicYearId);
}
