
package com.example.praticum.data.repository;

import com.example.praticum.data.dao.SessionDao;
import com.example.praticum.data.entity.Session;

import java.util.List;

public class SessionRepository {

    private final SessionDao sessionDao;

    public SessionRepository(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public List<Session> getAll() {
        return sessionDao.getAll();
    }

    public Session getById(long id) {
        return sessionDao.getById(id);
    }

    public List<Session> getByModuleId(long moduleId) {
        return sessionDao.getByModuleId(moduleId);
    }

    public List<Session> getByStatus(String status) {
        return sessionDao.getByStatus(status);
    }

    public List<Session> getActiveSessions() {
        return sessionDao.getActiveSessions();
    }

    public List<Session> getArchivedSessions() {
        return sessionDao.getArchivedSessions();
    }

    public long insert(Session session) {
        validate(session);
        return sessionDao.insert(session);
    }

    public void update(Session session) {
        validate(session);

        if (sessionDao.update(session) == 0) {
            throw new IllegalArgumentException(
                    "Séance introuvable.");
        }
    }

    public void delete(Session session) {
        if (session == null) {
            throw new IllegalArgumentException(
                    "La séance est obligatoire.");
        }

        if (sessionDao.delete(session) == 0) {
            throw new IllegalArgumentException(
                    "Séance introuvable.");
        }
    }

    public void setArchived(long sessionId, boolean archived) {
        if (sessionDao.setArchived(sessionId, archived) == 0) {
            throw new IllegalArgumentException(
                    "Séance introuvable.");
        }
    }

    public void updateStatus(long sessionId, String status) {
        validateStatus(status);

        if (sessionDao.updateStatus(sessionId, status) == 0) {
            throw new IllegalArgumentException(
                    "Séance introuvable.");
        }
    }

    private void validate(Session session) {
        if (session == null) {
            throw new IllegalArgumentException(
                    "La séance est obligatoire.");
        }

        if (session.getTitle() == null
                || session.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Le titre de la séance est obligatoire.");
        }

        if (session.getModuleId() <= 0) {
            throw new IllegalArgumentException(
                    "Un module valide doit être sélectionné.");
        }

        String type = session.getActivityType();
        if (type == null
                || !(type.equals("Cours")
                || type.equals("TD")
                || type.equals("TP")
                || type.equals("Atelier")
                || type.equals("Évaluation"))) {
            throw new IllegalArgumentException(
                    "Le type d'activité est invalide.");
        }

        validateStatus(session.getStatus());
    }

    private void validateStatus(String status) {
        if (status == null
                || !(status.equals("À venir")
                || status.equals("Terminée")
                || status.equals("Annulée"))) {
            throw new IllegalArgumentException(
                    "Le statut de la séance est invalide.");
        }
    }
}
