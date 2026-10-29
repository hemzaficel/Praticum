
package com.example.praticum.ui.evaluation;

public class EvaluationCriterion {

    private final String id;
    private String title;
    private String description;
    private double maxScore;
    private double score;

    public EvaluationCriterion(
            String id,
            String title,
            String description,
            double maxScore) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.maxScore = maxScore;
        this.score = 0;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(double maxScore) {
        this.maxScore = Math.max(0, maxScore);
    }

    public double getScore() {
        return score;
    }

    public boolean setScore(double score) {
        if (Double.isNaN(score)
                || Double.isInfinite(score)
                || score < 0
                || score > maxScore) {
            return false;
        }

        this.score = score;
        return true;
    }
}
