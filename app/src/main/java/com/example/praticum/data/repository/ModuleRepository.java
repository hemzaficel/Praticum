
package com.example.praticum.data.repository;

import com.example.praticum.data.dao.ModuleDao;
import com.example.praticum.data.entity.Module;

import java.util.List;

public class ModuleRepository {

    private final ModuleDao moduleDao;

    public ModuleRepository(ModuleDao moduleDao) {
        this.moduleDao = moduleDao;
    }

    public List<Module> getAll() {
        return moduleDao.getAll();
    }

    public Module getById(long id) {
        return moduleDao.getById(id);
    }

    public Module getByCode(String code) {
        return moduleDao.getByCode(code);
    }

    public List<Module> getByLevelId(long levelId) {
        return moduleDao.getByLevelId(levelId);
    }

    public List<Module> getByLevelAndSemester(
            long levelId, int semester) {
        return moduleDao.getByLevelAndSemester(levelId, semester);
    }

    public long insert(Module module) {
        validate(module);

        if (moduleDao.countByCodeAndLevel(
                module.getCode(), module.getLevelId()) > 0) {
            throw new IllegalArgumentException(
                    "Ce code existe déjà pour ce niveau.");
        }

        return moduleDao.insert(module);
    }

    public void update(Module module) {
        validate(module);

        if (moduleDao.countByCodeAndLevelExcludingId(
                module.getCode(),
                module.getLevelId(),
                module.getId()) > 0) {
            throw new IllegalArgumentException(
                    "Ce code est déjà utilisé dans ce niveau.");
        }

        if (moduleDao.update(module) == 0) {
            throw new IllegalArgumentException("Module introuvable.");
        }
    }

    public void delete(Module module) {
        if (module == null) {
            throw new IllegalArgumentException(
                    "Le module est obligatoire.");
        }

        if (moduleDao.delete(module) == 0) {
            throw new IllegalArgumentException("Module introuvable.");
        }
    }

    private void validate(Module module) {
        if (module == null) {
            throw new IllegalArgumentException(
                    "Le module est obligatoire.");
        }

        if (module.getCode() == null
                || module.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Le code du module est obligatoire.");
        }

        if (module.getName() == null
                || module.getName().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Le nom du module est obligatoire.");
        }

        if (module.getLevelId() <= 0) {
            throw new IllegalArgumentException(
                    "Un niveau valide doit être sélectionné.");
        }

        if (module.getSemester() != 1
                && module.getSemester() != 2) {
            throw new IllegalArgumentException(
                    "Le semestre doit être 1 ou 2.");
        }

        if (!Double.isFinite(module.getCoefficient())
                || module.getCoefficient() <= 0) {
            throw new IllegalArgumentException(
                    "Le coefficient doit être un nombre fini supérieur à zéro.");
        }
    }
}
