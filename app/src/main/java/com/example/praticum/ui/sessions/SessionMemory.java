
package com.example.praticum.ui.sessions;

import java.util.ArrayList;
import java.util.List;

public class SessionMemory {

    private int id;
    private String title;
    private String activityType;
    private String module;
    private String description;
    private String status;
    private boolean archived;

    private List<SessionCriterion> criteria = new ArrayList<>();

    public SessionMemory() {
        criteria = new ArrayList<>();
    }

    public SessionMemory(
            int id,
            String title,
            String activityType,
            String module,
            String description,
            String status,
            boolean archived) {

        this.id = id;
        this.title = title;
        this.activityType = activityType;
        this.module = module;
        this.description = description;
        this.status = status;
        this.archived = archived;
        this.criteria = new ArrayList<>();
    }

    // Compatibilité avec les anciens appels.
    public SessionMemory(
            int id,
            String title,
            String activityType,
            String module,
            String objectives,
            String description,
            String status,
            boolean archived) {

        this(
                id,
                title,
                activityType,
                module,
                description,
                status,
                archived
        );
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public List<SessionCriterion> getCriteria() {
        if (criteria == null) {
            criteria = new ArrayList<>();
        }
        return criteria;
    }

    public void setCriteria(List<SessionCriterion> criteria) {
        this.criteria = criteria == null
                ? new ArrayList<>()
                : new ArrayList<>(criteria);
    }
}
