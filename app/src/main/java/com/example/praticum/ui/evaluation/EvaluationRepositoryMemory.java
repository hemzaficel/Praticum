
package com.example.praticum.ui.evaluation;

import java.util.HashMap;
import java.util.Map;

public class EvaluationRepositoryMemory {

    private static EvaluationRepositoryMemory instance;

    private final Map<Integer, EvaluationSheetMemory> sheets =
            new HashMap<>();

    private EvaluationRepositoryMemory() {
    }

    public static synchronized EvaluationRepositoryMemory getInstance() {
        if (instance == null) {
            instance = new EvaluationRepositoryMemory();
        }

        return instance;
    }

    public synchronized EvaluationSheetMemory getSheet(
            int sessionId,
            String sessionTitle) {

        EvaluationSheetMemory sheet = sheets.get(sessionId);

        if (sheet == null) {
            sheet = new EvaluationSheetMemory(
                    sessionId,
                    sessionTitle
            );

            sheets.put(sessionId, sheet);
        }

        return sheet;
    }

    public synchronized void saveSheet(EvaluationSheetMemory sheet) {
        if (sheet != null) {
            sheets.put(sheet.getSessionId(), sheet);
        }
    }

    public synchronized boolean hasEvaluation(int sessionId) {
        return sheets.containsKey(sessionId);
    }
}
