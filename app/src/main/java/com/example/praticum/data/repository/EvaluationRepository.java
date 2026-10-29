
package com.example.praticum.data.repository;

import androidx.room.RoomDatabase;

import com.example.praticum.data.dao.EvaluationDao;
import com.example.praticum.data.entity.Evaluation;

import java.util.List;

public class EvaluationRepository {

    private final RoomDatabase database;
    private final EvaluationDao evaluationDao;

    public EvaluationRepository(
            RoomDatabase database,
            EvaluationDao evaluationDao
    ) {
        this.database = database;
        this.evaluationDao = evaluationDao;
    }

    public List<Evaluation> getAll() {
        return evaluationDao.getAll();
    }

    public Evaluation getById(int id) {
        return evaluationDao.getById(id);
    }

    public Evaluation getBySessionId(int sessionId) {
        return evaluationDao.getBySessionId(sessionId);
    }

    /**
     * Démarre une évaluation.
     * Une seule évaluation est autorisée par séance.
     */
    public int startEvaluation(int sessionId) {
        if (sessionId <= 0) {
            throw new IllegalArgumentException(
                    "L'identifiant de la séance est invalide.");
        }

        final int[] evaluationId = {-1};

        database.runInTransaction(() -> {
            Evaluation existing =
                    evaluationDao.getBySessionId(sessionId);

            if (existing != null) {
                evaluationId[0] = existing.getId();
                return;
            }

            Evaluation evaluation = new Evaluation(sessionId);
            evaluation.setScoringMode(
                    Evaluation.SCORING_MODE_COMMON);

            long insertedId = evaluationDao.insert(evaluation);
            evaluationId[0] = (int) insertedId;
        });

        return evaluationId[0];
    }

    public void update(Evaluation evaluation) {
        if (evaluation == null) {
            throw new IllegalArgumentException(
                    "L'évaluation est obligatoire.");
        }

        evaluation.setScoringMode(
                Evaluation.SCORING_MODE_COMMON);

        if (evaluationDao.update(evaluation) == 0) {
            throw new IllegalArgumentException(
                    "Évaluation introuvable.");
        }
    }

    public void delete(Evaluation evaluation) {
        if (evaluation == null) {
            throw new IllegalArgumentException(
                    "L'évaluation est obligatoire.");
        }

        if (evaluationDao.delete(evaluation) == 0) {
            throw new IllegalArgumentException(
                    "Évaluation introuvable.");
        }
    }
}
