
package com.example.praticum.ui.sessions;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SessionRepositoryMemory {

    private static SessionRepositoryMemory instance;

    private final List<SessionMemory> sessions;
    private int nextId;

    private SessionRepositoryMemory() {
        sessions = new ArrayList<>(SessionDataMemory.createSampleSessions());

        int maxId = 0;

        for (SessionMemory session : sessions) {
            if (session != null) {
                maxId = Math.max(maxId, session.getId());
            }
        }

        nextId = maxId + 1;
    }

    public static synchronized SessionRepositoryMemory getInstance() {
        if (instance == null) {
            instance = new SessionRepositoryMemory();
        }

        return instance;
    }

    public List<SessionMemory> getAllSessions() {
        return new ArrayList<>(sessions);
    }

    public List<SessionMemory> getActiveSessions() {
        List<SessionMemory> resultat = new ArrayList<>();

        for (SessionMemory session : sessions) {
            if (session != null && !session.isArchived()) {
                resultat.add(session);
            }
        }

        return resultat;
    }

    public SessionMemory getSessionById(int id) {
        for (SessionMemory session : sessions) {
            if (session != null && session.getId() == id) {
                return session;
            }
        }

        return null;
    }

    public SessionMemory addSession(SessionMemory session) {
        if (session == null) {
            return null;
        }

        session.setId(nextId++);
        sessions.add(session);

        return session;
    }

    public boolean updateSession(SessionMemory session) {
        if (session == null) {
            return false;
        }

        for (int i = 0; i < sessions.size(); i++) {
            SessionMemory existante = sessions.get(i);

            if (existante != null
                    && existante.getId() == session.getId()) {

                sessions.set(i, session);
                return true;
            }
        }

        return false;
    }

    public boolean deleteSession(int id) {
        for (int i = 0; i < sessions.size(); i++) {
            SessionMemory session = sessions.get(i);

            if (session != null && session.getId() == id) {
                sessions.remove(i);
                return true;
            }
        }

        return false;
    }

    public boolean archiveSession(int id) {
        SessionMemory session = getSessionById(id);

        if (session == null) {
            return false;
        }

        session.setArchived(true);
        return true;
    }

    public boolean unarchiveSession(int id) {
        SessionMemory session = getSessionById(id);

        if (session == null) {
            return false;
        }

        session.setArchived(false);
        return true;
    }

    public List<SessionMemory> searchSessions(String query) {
        List<SessionMemory> resultat = new ArrayList<>();
        String recherche = normaliser(query);

        for (SessionMemory session : getActiveSessions()) {
            if (recherche.isEmpty()
                    || contient(session.getTitle(), recherche)
                    || contient(session.getActivityType(), recherche)
                    || contient(session.getModule(), recherche)
                    || contient(session.getDescription(), recherche)) {

                resultat.add(session);
            }
        }

        return resultat;
    }

    public List<SessionMemory> getSessionsByStatus(String status) {
        List<SessionMemory> resultat = new ArrayList<>();

        for (SessionMemory session : getActiveSessions()) {
            if (status == null
                    || status.trim().isEmpty()
                    || status.equalsIgnoreCase(session.getStatus())) {

                resultat.add(session);
            }
        }

        return resultat;
    }

    public List<SessionMemory> getArchivedSessions() {
        List<SessionMemory> resultat = new ArrayList<>();

        for (SessionMemory session : sessions) {
            if (session != null && session.isArchived()) {
                resultat.add(session);
            }
        }

        return resultat;
    }

    private boolean contient(String texte, String recherche) {
        return normaliser(texte).contains(recherche);
    }

    private String normaliser(String texte) {
        return texte == null
                ? ""
                : texte.toLowerCase(Locale.ROOT).trim();
    }
}
