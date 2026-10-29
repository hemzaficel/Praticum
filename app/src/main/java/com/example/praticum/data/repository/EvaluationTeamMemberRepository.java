
package com.example.praticum.data.repository;

import androidx.room.RoomDatabase;

import com.example.praticum.data.dao.EvaluationTeamDao;
import com.example.praticum.data.dao.EvaluationTeamMemberDao;
import com.example.praticum.data.entity.EvaluationTeam;
import com.example.praticum.data.entity.EvaluationTeamMember;

import java.util.List;

public class EvaluationTeamMemberRepository {

    private final RoomDatabase database;
    private final EvaluationTeamMemberDao memberDao;
    private final EvaluationTeamDao teamDao;

    public EvaluationTeamMemberRepository(
            RoomDatabase database,
            EvaluationTeamMemberDao memberDao,
            EvaluationTeamDao teamDao
    ) {
        this.database = database;
        this.memberDao = memberDao;
        this.teamDao = teamDao;
    }

    public List<EvaluationTeamMember> getByTeamId(int teamId) {
        return memberDao.getByTeamId(teamId);
    }

    public List<EvaluationTeamMember> getByEvaluationId(int evaluationId) {
        return memberDao.getByEvaluationId(evaluationId);
    }

    public void addMember(EvaluationTeamMember member) {
        validate(member);

        database.runInTransaction(() -> {
            EvaluationTeam team =
                    teamDao.getById(member.getTeamId());

            if (team == null) {
                throw new IllegalArgumentException(
                        "Équipe introuvable.");
            }

            if (team.getEvaluationId() != member.getEvaluationId()) {
                throw new IllegalArgumentException(
                        "L'équipe ne correspond pas à cette évaluation.");
            }

            int existingCount = memberDao.countStudentInEvaluation(
                    member.getEvaluationId(),
                    member.getStudentId());

            if (existingCount > 0) {
                throw new IllegalArgumentException(
                        "Cet étudiant appartient déjà à une équipe "
                                + "de cette évaluation.");
            }

            memberDao.insert(member);
        });
    }

    public void removeMember(int teamId, String studentId) {
        memberDao.removeStudentFromTeam(teamId, studentId);
    }

    public void deleteByTeamId(int teamId) {
        memberDao.deleteByTeamId(teamId);
    }

    private void validate(EvaluationTeamMember member) {
        if (member == null) {
            throw new IllegalArgumentException(
                    "Le membre est obligatoire.");
        }

        if (member.getTeamId() <= 0
                || member.getEvaluationId() <= 0
                || member.getStudentId() == null
                || member.getStudentId().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Les informations du membre sont invalides.");
        }
    }
}
