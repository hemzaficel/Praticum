package com.example.praticum.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.praticum.data.dao.AcademicYearDao;
import com.example.praticum.data.dao.LevelDao;
import com.example.praticum.data.dao.ModuleDao;
import com.example.praticum.data.dao.StudentGroupDao;
import com.example.praticum.data.dao.StudentDao;
import com.example.praticum.data.dao.SessionDao;
import com.example.praticum.data.dao.SessionCriterionDao;
import com.example.praticum.data.dao.SessionGroupDao;
import com.example.praticum.data.dao.AttendanceRecordDao;
import com.example.praticum.data.dao.EvaluationDao;
import com.example.praticum.data.dao.EvaluationTeamDao;
import com.example.praticum.data.dao.EvaluationTeamMemberDao;
import com.example.praticum.data.dao.EvaluationScoreDao;

import com.example.praticum.data.entity.AcademicYear;
import com.example.praticum.data.entity.Level;
import com.example.praticum.data.entity.Course;
import com.example.praticum.data.entity.StudentGroup;
import com.example.praticum.data.entity.Student;
import com.example.praticum.data.entity.Session;
import com.example.praticum.data.entity.SessionCriterion;
import com.example.praticum.data.entity.SessionGroup;
import com.example.praticum.data.entity.AttendanceRecord;
import com.example.praticum.data.entity.Evaluation;
import com.example.praticum.data.entity.EvaluationTeam;
import com.example.praticum.data.entity.EvaluationTeamMember;
import com.example.praticum.data.entity.EvaluationScore;

@Database(
        entities = {
                AcademicYear.class,
                Level.class,
                Course.class,
                StudentGroup.class,
                Student.class,
                Session.class,
                SessionCriterion.class,
                SessionGroup.class,
                AttendanceRecord.class,
                Evaluation.class,
                EvaluationTeam.class,
                EvaluationTeamMember.class,
                EvaluationScore.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract AcademicYearDao academicYearDao();

    public abstract LevelDao levelDao();

    public abstract ModuleDao moduleDao();

    public abstract StudentGroupDao studentGroupDao();

    public abstract StudentDao studentDao();

    public abstract SessionDao sessionDao();

    public abstract SessionCriterionDao sessionCriterionDao();

    public abstract SessionGroupDao sessionGroupDao();

    public abstract AttendanceRecordDao attendanceRecordDao();

    public abstract EvaluationDao evaluationDao();

    public abstract EvaluationTeamDao evaluationTeamDao();

    public abstract EvaluationTeamMemberDao evaluationTeamMemberDao();

    public abstract EvaluationScoreDao evaluationScoreDao();
}
