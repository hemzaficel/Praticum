
package com.example.praticum.data.repository;

import com.example.praticum.data.dao.LevelDao;
import com.example.praticum.data.entity.Level;

import java.util.List;
import java.util.Locale;

public class LevelRepository {

    private final LevelDao levelDao;

    public LevelRepository(LevelDao levelDao) {
        if (levelDao == null) {
            throw new IllegalArgumentException(
                    "Le DAO des niveaux est obligatoire.");
        }

        this.levelDao = levelDao;
    }

    public List<Level> getAll() {
        return levelDao.getAll();
    }

    public Level getById(long id) {
        return levelDao.getById(id);
    }

    public Level getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        return levelDao.getByCode(code.trim());
    }

    public long insert(Level level) {
        validate(level);

        if (levelDao.countByCode(level.getCode()) > 0) {
            throw new IllegalArgumentException(
                    "Ce code de niveau existe déjà.");
        }

        return levelDao.insert(level);
    }

    public void update(Level level) {
        validate(level);

        if (level.getId() <= 0) {
            throw new IllegalArgumentException(
                    "L'identifiant du niveau est invalide.");
        }

        if (levelDao.countByCodeExcludingId(
                level.getCode(), level.getId()) > 0) {
            throw new IllegalArgumentException(
                    "Ce code est déjà utilisé par un autre niveau.");
        }

        if (levelDao.update(level) == 0) {
            throw new IllegalArgumentException(
                    "Niveau introuvable.");
        }
    }

    public void delete(Level level) {
        if (level == null) {
            throw new IllegalArgumentException(
                    "Le niveau est obligatoire.");
        }

        if (level.getId() <= 0) {
            throw new IllegalArgumentException(
                    "L'identifiant du niveau est invalide.");
        }

        if (levelDao.countModules(level.getId()) > 0) {
            throw new IllegalStateException(
                    "Impossible de supprimer ce niveau : "
                            + "des modules y sont rattachés.");
        }

        if (levelDao.countGroups(level.getId()) > 0) {
            throw new IllegalStateException(
                    "Impossible de supprimer ce niveau : "
                            + "des groupes y sont rattachés.");
        }

        if (levelDao.delete(level) == 0) {
            throw new IllegalArgumentException(
                    "Niveau introuvable.");
        }
    }

    private void validate(Level level) {
        if (level == null) {
            throw new IllegalArgumentException(
                    "Le niveau est obligatoire.");
        }

        if (level.getCode() == null
                || level.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Le code du niveau est obligatoire.");
        }

        if (level.getName() == null
                || level.getName().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Le nom du niveau est obligatoire.");
        }

        if (level.getCycle() == null) {
            throw new IllegalArgumentException(
                    "Le cycle du niveau est obligatoire.");
        }

        String cycle = level.getCycle().trim();

        if (!cycle.equals(Level.CYCLE_LICENCE)
                && !cycle.equals(Level.CYCLE_MASTER)
                && !cycle.equals(Level.CYCLE_DOCTORAT)
                && !cycle.equals(Level.CYCLE_INGENIEUR)) {
            throw new IllegalArgumentException(
                    "Le cycle doit être Licence, Master, "
                            + "Doctorat ou Ingénieur.");
        }

        if (level.getDisplayOrder() <= 0) {
            throw new IllegalArgumentException(
                    "L'ordre d'affichage doit être supérieur à zéro.");
        }

        level.setCode(level.getCode().trim());
        level.setName(level.getName().trim());
        level.setCycle(cycle);

        if (level.getDescription() != null) {
            String description = level.getDescription().trim();
            level.setDescription(
                    description.isEmpty() ? null : description);
        }
    }
}
