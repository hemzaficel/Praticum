
package com.example.praticum.data.repository;

import androidx.room.RoomDatabase;

import com.example.praticum.data.dao.EvaluationScoreDao;
import com.example.praticum.data.dao.EvaluationTeamDao;
import com.example.praticum.data.dao.SessionCriterionDao;
import com.example.praticum.data.entity.EvaluationScore;
import com.example.praticum.data.entity.EvaluationTeam;
import com.example.praticum.data.entity.SessionCriterion;

import java.util.List;

public class EvaluationScoreRepository {

    private final RoomDatabase database;
    private final EvaluationScoreDao scoreDao;
    private final EvaluationTeamDao teamDao;
    private final SessionCriterionDao criterionDao;

    public EvaluationScoreRepository(
            RoomDatabase database,
            EvaluationScoreDao scoreDao,
            EvaluationTeamDao teamDao,
            SessionCriterionDao criterionDao
    ) {
        this.database = database;
        this.scoreDao = scoreDao;
        this.teamDao = teamDao;
        this.criterionDao = criterionDao;
    }

    public List<EvaluationScore> getByTeamId(int teamId) {
        return scoreDao.getByTeamId(teamId);
    }

    public List<EvaluationScore> getByCriterionId(int criterionId) {
        return scoreDao.getByCriterionId(criterionId);
    }

    public EvaluationScore getByTeamAndCriterion(
            int teamId, int criterionId) {
        return scoreDao.getByTeamAndCriterion(teamId, criterionId);
    }

    public void save(EvaluationScore score) {
        validate(score);

        database.runInTransaction(() -> {
            EvaluationTeam team = teamDao.getById(score.getTeamId());
            if (team == null) {
                throw new IllegalArgumentException(
                        "Équipe d'évaluation introuvable.");
            }

            SessionCriterion criterion =
                    criterionDao.getById(score.getCriterionId());

            if (criterion == null) {
                throw new IllegalArgumentException(
                        "Critère d'évaluation introuvable.");
            }

            if (score.getScore() > criterion.getMaxScore()) {
                throw new IllegalArgumentException(
                        "La note ne peut pas dépasser le barème maximal.");
            }

            scoreDao.insertOrUpdate(score);
        });
    }

    public double getTotalScore(int teamId) {
        return scoreDao.getTotalScore(teamId);
    }

    public int countByTeamId(int teamId) {
        return scoreDao.countByTeamId(teamId);
    }

    public int countByCriterionId(int criterionId) {
        return scoreDao.countByCriterionId(criterionId);
    }

    public void deleteByTeamId(int teamId) {
        scoreDao.deleteByTeamId(teamId);
    }

    public void deleteByTeamAndCriterion(int teamId, int criterionId) {
        scoreDao.deleteByTeamAndCriterion(teamId, criterionId);
    }

    private void validate(EvaluationScore score) {
        if (score == null) {
            throw new IllegalArgumentException(
                    "La note est obligatoire.");
        }

        if (score.getTeamId() <= 0 || score.getCriterionId() <= 0) {
            throw new IllegalArgumentException(
                    "L'équipe et le critère doivent être valides.");
        }

        if (Double.isNaN(score.getScore())
                || Double.isInfinite(score.getScore())
                || score.getScore() < 0) {
            throw new IllegalArgumentException(
                    "La note doit être un nombre positif ou nul.");
        }
    }
}
