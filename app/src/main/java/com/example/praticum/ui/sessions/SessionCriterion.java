
package com.example.praticum.ui.sessions;

public class SessionCriterion {

    private String title;
    private String description;
    private double maxScore;

    public SessionCriterion() {
        this("", "", 0);
    }

    public SessionCriterion(
            String title,
            String description,
            double maxScore) {

        this.title = title;
        this.description = description;
        this.maxScore = maxScore;
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
        this.maxScore = maxScore;
    }
}
