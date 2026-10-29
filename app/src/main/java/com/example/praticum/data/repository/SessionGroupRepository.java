
package com.example.praticum.data.repository;

import com.example.praticum.data.dao.SessionGroupDao;
import com.example.praticum.data.entity.SessionGroup;

import java.util.List;

public class SessionGroupRepository {

    private final SessionGroupDao sessionGroupDao;

    public SessionGroupRepository(SessionGroupDao sessionGroupDao) {
        this.sessionGroupDao = sessionGroupDao;
    }

    public List<SessionGroup> getAll() {
        return sessionGroupDao.getAll();
    }

    public SessionGroup getById(long id) {
        return sessionGroupDao.getById(id);
    }

    public List<SessionGroup> getBySessionId(long sessionId) {
        return sessionGroupDao.getBySessionId(sessionId);
    }

    public List<SessionGroup> getByGroupId(long groupId) {
        return sessionGroupDao.getByGroupId(groupId);
    }

    public SessionGroup getBySessionAndGroup(
            long sessionId, long groupId) {
        return sessionGroupDao.getBySessionAndGroup(sessionId, groupId);
    }

    public long insert(SessionGroup sessionGroup) {
        validate(sessionGroup);

        if (sessionGroupDao.getBySessionAndGroup(
                sessionGroup.getSessionId(),
                sessionGroup.getGroupId()) != null) {
            throw new IllegalArgumentException(
                    "Ce groupe est déjà associé à cette séance.");
        }

        return sessionGroupDao.insert(sessionGroup);
    }

    public void update(SessionGroup sessionGroup) {
        validate(sessionGroup);

        SessionGroup existing = sessionGroupDao.getBySessionAndGroup(
                sessionGroup.getSessionId(),
                sessionGroup.getGroupId());

        if (existing != null && existing.getId() != sessionGroup.getId()) {
            throw new IllegalArgumentException(
                    "Ce groupe est déjà associé à cette séance.");
        }

        if (sessionGroupDao.update(sessionGroup) == 0) {
            throw new IllegalArgumentException(
                    "Association séance-groupe introuvable.");
        }
    }

    public void delete(SessionGroup sessionGroup) {
        if (sessionGroup == null) {
            throw new IllegalArgumentException(
                    "L'association séance-groupe est obligatoire.");
        }

        if (sessionGroupDao.delete(sessionGroup) == 0) {
            throw new IllegalArgumentException(
                    "Association séance-groupe introuvable.");
        }
    }

    private void validate(SessionGroup sessionGroup) {
        if (sessionGroup == null) {
            throw new IllegalArgumentException(
                    "L'association séance-groupe est obligatoire.");
        }

        if (sessionGroup.getSessionId() <= 0
                || sessionGroup.getGroupId() <= 0) {
            throw new IllegalArgumentException(
                    "La séance et le groupe doivent être valides.");
        }

        // La date et les horaires peuvent rester vides pendant la saisie.
        if (sessionGroup.getSessionDate() != null
                && !sessionGroup.getSessionDate().trim().isEmpty()
                && sessionGroup.getStartTime() != null
                && !sessionGroup.getStartTime().trim().isEmpty()
                && sessionGroup.getEndTime() != null
                && !sessionGroup.getEndTime().trim().isEmpty()
                && sessionGroup.getEndTime().compareTo(
                        sessionGroup.getStartTime()) <= 0) {
            throw new IllegalArgumentException(
                    "L'heure de fin doit être postérieure à l'heure de début.");
        }
    }
}
