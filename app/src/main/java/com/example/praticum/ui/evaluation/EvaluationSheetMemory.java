
package com.example.praticum.ui.evaluation;

import java.util.ArrayList;
import java.util.List;

public class EvaluationSheetMemory {

    private final int sessionId;
    private final String sessionTitle;

    private double maxScore = 20;
    private String scoringMode = "Note commune";

    private final List<EvaluationTeamMemory> teams =
            new ArrayList<>();

    public EvaluationSheetMemory(int sessionId, String sessionTitle) {
        this.sessionId = sessionId;
        this.sessionTitle = sessionTitle;
    }

    public int getSessionId() {
        return sessionId;
    }

    public String getSessionTitle() {
        return sessionTitle;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(double maxScore) {
        if (maxScore > 0
                && !Double.isNaN(maxScore)
                && !Double.isInfinite(maxScore)) {
            this.maxScore = maxScore;
        }
    }

    public String getScoringMode() {
        return scoringMode;
    }

    public void setScoringMode(String scoringMode) {
        this.scoringMode = scoringMode;
    }

    public List<EvaluationTeamMemory> getTeams() {
        return teams;
    }

    public void addTeam(EvaluationTeamMemory team) {
        if (team != null) {
            teams.add(team);
        }
    }
}
