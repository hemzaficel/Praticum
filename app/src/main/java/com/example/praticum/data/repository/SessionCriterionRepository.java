
package com.example.praticum.data.repository;

import androidx.room.RoomDatabase;

import com.example.praticum.data.dao.SessionCriterionDao;
import com.example.praticum.data.dao.EvaluationScoreDao;
import com.example.praticum.data.entity.SessionCriterion;
import com.example.praticum.data.entity.EvaluationScore;

import java.util.List;

public class SessionCriterionRepository {

    private final SessionCriterionDao criterionDao;
    private final EvaluationScoreDao scoreDao;
    private final RoomDatabase database;

    public SessionCriterionRepository(
            RoomDatabase database,
            SessionCriterionDao criterionDao,
            EvaluationScoreDao scoreDao
    ) {
        this.database = database;
        this.criterionDao = criterionDao;
        this.scoreDao = scoreDao;
    }

    public SessionCriterion getById(long id) {
        return criterionDao.getById(id);
    }

    public List<SessionCriterion> getBySessionId(long sessionId) {
        return criterionDao.getBySessionId(sessionId);
    }

    public Double getTotalMaxScore(long sessionId) {
        return criterionDao.getTotalMaxScore(sessionId);
    }

    public long insert(SessionCriterion criterion) {
        validate(criterion);

        final long[] id = {-1};

        database.runInTransaction(() -> {
            Integer maxPosition =
                    criterionDao.getMaxPosition(criterion.getSessionId());

            criterion.setPosition(maxPosition == null ? 1 : maxPosition + 1);
            id[0] = criterionDao.insert(criterion);
        });

        return id[0];
    }

    public void update(SessionCriterion criterion) {
        validate(criterion);

        database.runInTransaction(() -> {
            List<EvaluationScore> scores =
                    scoreDao.getByCriterionId(criterion.getId());

            for (EvaluationScore score : scores) {
                if (score.getScore() > criterion.getMaxScore()) {
                    throw new IllegalStateException(
                            "Le barème ne peut pas être inférieur à une note déjà attribuée.");
                }
            }

            if (criterionDao.update(criterion) == 0) {
                throw new IllegalArgumentException(
                        "Critère introuvable.");
            }
        });
    }

    public void delete(long criterionId) {
        if (scoreDao.countByCriterionId(criterionId) > 0) {
            throw new IllegalStateException(
                    "Impossible de supprimer un critère déjà noté.");
        }

        if (criterionDao.deleteIfNotScored(criterionId) == 0) {
            throw new IllegalStateException(
                    "Le critère n'existe pas ou ne peut pas être supprimé.");
        }
    }

    private void validate(SessionCriterion criterion) {
        if (criterion == null) {
            throw new IllegalArgumentException(
                    "Le critère est obligatoire.");
        }

        if (criterion.getTitle() == null
                || criterion.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Le titre du critère est obligatoire.");
        }

        if (criterion.getMaxScore() <= 0) {
            throw new IllegalArgumentException(
                    "Le barème doit être supérieur à zéro.");
        }

        if (criterion.getSessionId() <= 0) {
            throw new IllegalArgumentException(
                    "Une séance valide doit être sélectionnée.");
        }
    }
}
