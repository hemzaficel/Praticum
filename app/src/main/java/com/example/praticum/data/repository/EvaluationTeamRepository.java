
package com.example.praticum.data.repository;

import androidx.room.RoomDatabase;

import com.example.praticum.data.dao.EvaluationTeamDao;
import com.example.praticum.data.entity.EvaluationTeam;

import java.util.List;

public class EvaluationTeamRepository {

    private final RoomDatabase database;
    private final EvaluationTeamDao teamDao;

    public EvaluationTeamRepository(
            RoomDatabase database,
            EvaluationTeamDao teamDao
    ) {
        this.database = database;
        this.teamDao = teamDao;
    }

    public EvaluationTeam getById(int id) {
        return teamDao.getById(id);
    }

    public List<EvaluationTeam> getByEvaluationId(int evaluationId) {
        return teamDao.getByEvaluationId(evaluationId);
    }

    public int createTeam(EvaluationTeam team) {
        validate(team);

        final int[] teamId = {-1};

        database.runInTransaction(() -> {
            int nextNumber =
                    teamDao.getMaxTeamNumber(team.getEvaluationId()) + 1;

            team.setTeamNumber(nextNumber);

            long insertedId = teamDao.insert(team);
            teamId[0] = (int) insertedId;
        });

        return teamId[0];
    }

    public void update(EvaluationTeam team) {
        validate(team);

        if (teamDao.update(team) == 0) {
            throw new IllegalArgumentException(
                    "Équipe introuvable.");
        }
    }

    public void delete(EvaluationTeam team) {
        if (team == null) {
            throw new IllegalArgumentException(
                    "L'équipe est obligatoire.");
        }

        if (teamDao.delete(team) == 0) {
            throw new IllegalArgumentException(
                    "Équipe introuvable.");
        }
    }

    private void validate(EvaluationTeam team) {
        if (team == null) {
            throw new IllegalArgumentException(
                    "L'équipe est obligatoire.");
        }

        if (team.getEvaluationId() <= 0) {
            throw new IllegalArgumentException(
                    "L'évaluation est invalide.");
        }

        if (team.getName() == null
                || team.getName().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Le nom de l'équipe est obligatoire.");
        }
    }
}
