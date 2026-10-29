package com.example.praticum.ui.sessions;

import java.util.ArrayList;
import java.util.List;

public final class SessionDataMemory {

    private SessionDataMemory() {
        // Classe utilitaire.
    }

    public static List<SessionMemory> createSampleSessions() {
        List<SessionMemory> sessions = new ArrayList<>();

        sessions.add(new SessionMemory(
                1,
                "Introduction à la programmation",
                "Cours",
                "Informatique",
                "Présentation des concepts fondamentaux et des premiers algorithmes.",
                "À venir",
                false
        ));

        sessions.add(new SessionMemory(
                2,
                "Travaux pratiques Java",
                "TP",
                "Programmation",
                "Exercices pratiques sur les variables, les conditions et les boucles.",
                "À venir",
                false
        ));

        sessions.add(new SessionMemory(
                3,
                "Évaluation algorithmique",
                "Évaluation",
                "Algorithmique",
                "Évaluation des notions étudiées pendant les séances précédentes.",
                "Terminée",
                false
        ));

        sessions.add(new SessionMemory(
                4,
                "Atelier de révision",
                "Atelier",
                "Informatique",
                "Séance de révision et de préparation aux évaluations.",
                "Annulée",
                false
        ));

        return sessions;
    }
}
