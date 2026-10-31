package com.example.praticum.data.database;

import com.example.praticum.data.entity.AcademicYear;
import com.example.praticum.data.entity.Course;
import com.example.praticum.data.entity.Level;
import com.example.praticum.data.entity.Specialization;
import com.example.praticum.data.entity.Student;
import com.example.praticum.data.entity.StudentGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Inserts the minimum reference configuration and sample students for testing. */
public final class DatabaseSeeder {
    private static final int START_YEAR = 2026;
    private static final String SPECIALIZATION_CODE = "INFO";
    private static final String SPECIALIZATION_NAME = "Informatique";

    private DatabaseSeeder() { }

    /**
     * Adds missing seed rows in one transaction. Existing rows are kept intact.
     * @return number of rows inserted during this call
     */
    public static int seed(AppDatabase database) {
        if (database == null) throw new IllegalArgumentException("La base de données est obligatoire.");
        final int[] inserted = {0};

        database.runInTransaction(() -> {
            AcademicYear year = findAcademicYear(database, START_YEAR);
            if (year == null) {
                boolean active = database.academicYearDao().getActive() == null;
                year = new AcademicYear(
                        START_YEAR, START_YEAR + 1,
                        START_YEAR + "-09-01", (START_YEAR + 1) + "-06-30", active
                );
                year.setId((int) database.academicYearDao().insert(year));
                inserted[0]++;
            } else if (database.academicYearDao().getActive() == null) {
                database.academicYearDao().activateById(year.getId());
                year.setActive(true);
            }

            Map<String, Level> levels = new HashMap<>();
            String[][] levelSeeds = {
                    {"L1", "Licence 1", "1", "Première année de Licence"},
                    {"L2", "Licence 2", "2", "Deuxième année de Licence"},
                    {"L3", "Licence 3", "3", "Troisième année de Licence"}
            };
            for (String[] seed : levelSeeds) {
                Level level = database.levelDao().getByCode(seed[0]);
                if (level == null) {
                    level = new Level(seed[0], seed[1], Level.CYCLE_LICENCE,
                            Integer.parseInt(seed[2]), seed[3]);
                    level.setId((int) database.levelDao().insert(level));
                    inserted[0]++;
                }
                levels.put(seed[0], level);
            }

            Specialization specialization = ensureSpecialization(database, inserted);

            String[][] moduleSeeds = {
                    {"INF101", "Algorithmique", "L1", "1", "1.0"},
                    {"INF102", "Programmation", "L1", "2", "1.0"},
                    {"INF201", "Structures de données", "L2", "1", "1.0"},
                    {"INF202", "Bases de données", "L2", "2", "1.0"},
                    {"INF301", "Génie logiciel", "L3", "1", "1.0"},
                    {"INF302", "Réseaux", "L3", "2", "1.0"}
            };
            for (String[] seed : moduleSeeds) {
                Level level = levels.get(seed[2]);
                if (database.moduleDao().countByCodeAndLevel(seed[0], level.getId()) == 0) {
                    database.moduleDao().insert(new Course(
                            seed[0], seed[1], level.getId(), specialization.getId(),
                            Integer.parseInt(seed[3]), Double.parseDouble(seed[4]),
                            "Module de configuration de test"
                    ));
                    inserted[0]++;
                }
            }

            removeLegacyTestGroups(database, year.getId());

            String[][] groupSeeds = {
                    {"L1", "8"},
                    {"L2", "6"},
                    {"L3", "4"}
            };
            for (String[] groupSeed : groupSeeds) {
                String levelCode = groupSeed[0];
                Level level = levels.get(levelCode);
                int groupCount = Integer.parseInt(groupSeed[1]);
                for (int number = 1; number <= groupCount; number++) {
                    String groupCode = "G" + number;
                    if (database.studentGroupDao().getByCodeAndYearAndLevel(
                            groupCode, year.getId(), level.getId()) != null) continue;

                    database.studentGroupDao().insert(new StudentGroup(
                            groupCode + " - " + level.getName(),
                            groupCode,
                            year.getId(),
                            level.getId(),
                            SPECIALIZATION_NAME,
                            "Groupe de test pour le niveau " + levelCode
                    ));
                    inserted[0]++;
                }
            }

            ensureTestStudents(database, inserted);
        });

        return inserted[0];
    }

    private static void removeLegacyTestGroups(AppDatabase database, int academicYearId) {
        for (String levelCode : new String[]{"L1", "L2", "L3"}) {
            String legacyCode = "INFO-" + levelCode + "-A";
            StudentGroup legacyGroup = database.studentGroupDao()
                    .getByCodeAndAcademicYear(legacyCode, academicYearId);
            String expectedName = SPECIALIZATION_NAME + " " + levelCode + " - Groupe A";
            if (legacyGroup != null && expectedName.equals(legacyGroup.getName())) {
                database.studentGroupDao().deleteIfEmpty(legacyGroup.getId());
            }
        }
    }

    private static void ensureTestStudents(AppDatabase database, int[] inserted) {
        String[][] studentSeeds = {
                {"DUPONT", "Alice", "TEST-2026-001", "alice.test@example.com", "دوبون", "أليس", "Étudiante de test : dossier avec toutes les informations facultatives."},
                {"MARTIN", "Yacine", "TEST-2026-002", "yacine.test@example.com", "مارتن", "ياسين", "Étudiant de test : vérification des noms en français et en arabe."},
                {"BENALI", "Lina", "TEST-2026-003", "lina.test@example.com", "بن علي", "لينا", "Étudiante de test : données de démonstration pour le parcours académique."}
        };

        for (String[] seed : studentSeeds) {
            Student student = database.studentDao().getByMatricule(seed[2]);
            boolean isNew = student == null;
            if (isNew) student = new Student(seed[0], seed[1]);

            student.setLastName(seed[0]);
            student.setFirstName(seed[1]);
            student.setMatricule(seed[2]);
            student.setEmail(seed[3]);
            student.setLastNameArabic(seed[4]);
            student.setFirstNameArabic(seed[5]);
            student.setNotes(seed[6]);
            // card_image_uri reste volontairement vide : aucune photo n’est semée.

            if (isNew) {
                database.studentDao().insert(student);
                inserted[0]++;
            } else {
                database.studentDao().update(student);
            }
        }
    }

    private static AcademicYear findAcademicYear(AppDatabase database, int startYear) {
        List<AcademicYear> years = database.academicYearDao().getAll();
        for (AcademicYear year : years) {
            if (year.getStartYear() == startYear) return year;
        }
        return null;
    }

    private static Specialization ensureSpecialization(AppDatabase database, int[] inserted) {
        List<Specialization> specializations = database.specializationDao().getAll();
        for (Specialization specialization : specializations) {
            if (SPECIALIZATION_CODE.equalsIgnoreCase(specialization.getCode())
                    || SPECIALIZATION_NAME.equalsIgnoreCase(specialization.getName())) return specialization;
        }
        Specialization specialization = new Specialization(
                SPECIALIZATION_CODE,
                SPECIALIZATION_NAME,
                "Formation de test pour le parcours Informatique"
        );
        specialization.setId((int) database.specializationDao().insert(specialization));
        inserted[0]++;
        return specialization;
    }
}

