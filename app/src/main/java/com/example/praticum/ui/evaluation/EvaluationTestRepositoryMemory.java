package com.example.praticum.ui.evaluation;

import java.util.ArrayList;
import java.util.List;

/**
 * Critères temporaires pour tester les fiches d'évaluation.
 * Ils pourront ensuite être remplacés par les critères configurés
 * par l'enseignant.
 */
public final class EvaluationTestRepositoryMemory {

    private EvaluationTestRepositoryMemory() {
    }

    public static List<EvaluationCriterion> getDefaultCriteria() {
        List<EvaluationCriterion> criteria = new ArrayList<>();

        criteria.add(new EvaluationCriterion(
                "1",
                "Compréhension",
                "Compréhension du sujet et des notions étudiées",
                10
        ));

        criteria.add(new EvaluationCriterion(
                "2",
                "Réalisation",
                "Qualité de la réalisation et respect des consignes",
                10
        ));

        return criteria;
    }
}
