package com.example.praticum.ui.enrollments;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.praticum.data.database.AppDatabase;
import com.example.praticum.data.database.DatabaseProvider;
import com.example.praticum.data.entity.AcademicYear;
import com.example.praticum.data.entity.Enrollment;
import com.example.praticum.data.entity.EnrollmentListItem;
import com.example.praticum.data.entity.Level;
import com.example.praticum.data.entity.Level;
import com.example.praticum.data.entity.Specialization;
import com.example.praticum.data.entity.Student;
import com.example.praticum.data.entity.StudentGroup;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EnrollmentViewModel extends AndroidViewModel {
    private final AppDatabase database;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<EnrollmentListItem>> enrollments = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Student>> students = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<StudentGroup>> groups = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<AcademicYear>> years = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Level>> levels = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Enrollment> enrollmentForEdit = new MutableLiveData<>();
    private final MutableLiveData<EnrollmentListItem> enrollmentDetails = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);

    public EnrollmentViewModel(@NonNull Application application) {
        super(application);
        database = DatabaseProvider.getDatabase(application);
    }

    public LiveData<List<EnrollmentListItem>> getEnrollments() { return enrollments; }
    public LiveData<List<Student>> getStudents() { return students; }
    public LiveData<List<StudentGroup>> getGroups() { return groups; }
    public LiveData<List<AcademicYear>> getYears() { return years; }
    public LiveData<List<Level>> getLevels() { return levels; }
    public LiveData<Enrollment> getEnrollmentForEdit() { return enrollmentForEdit; }
    public LiveData<EnrollmentListItem> getEnrollmentDetails() { return enrollmentDetails; }
    public LiveData<String> getError() { return error; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> isSaving() { return saving; }

    public void loadData() {
        executor.execute(() -> {
            enrollments.postValue(database.enrollmentDao().getAllWithDetails());
            students.postValue(database.studentDao().getAll());
            groups.postValue(database.studentGroupDao().getAll());
            years.postValue(database.academicYearDao().getAll());
            levels.postValue(database.levelDao().getAll());
        });
    }

    public void loadEnrollmentDetails(int enrollmentId) {
        executor.execute(() -> enrollmentDetails.postValue(database.enrollmentDao().getDetailsById(enrollmentId)));
    }

    public void loadEnrollmentForEdit(int enrollmentId) {
        executor.execute(() -> enrollmentForEdit.postValue(database.enrollmentDao().getById(enrollmentId)));
    }

    public void updateEnrollment(int enrollmentId, String studentId, int groupId, String status) {
        saving.postValue(true);
        error.postValue(null);
        message.postValue(null);
        executor.execute(() -> {
            try {
                if (studentId == null || studentId.trim().isEmpty() || groupId <= 0) {
                    throw new IllegalArgumentException("Sélectionnez un étudiant et un groupe.");
                }
                if (!isKnownStatus(status)) throw new IllegalArgumentException("Sélectionnez un statut valide.");
                database.runInTransaction(() -> {
                    Enrollment current = database.enrollmentDao().getById(enrollmentId);
                    if (current == null) throw new IllegalArgumentException("Cette inscription n’existe plus.");
                    Student student = database.studentDao().getById(studentId);
                    StudentGroup group = database.studentGroupDao().getById(groupId);
                    if (student == null) throw new IllegalArgumentException("Étudiant introuvable.");
                    if (group == null || group.getLevelId() == null) throw new IllegalArgumentException("Le groupe doit avoir un niveau valide.");
                    AcademicYear year = database.academicYearDao().getById(group.getAcademicYearId());
                    Level level = database.levelDao().getById(group.getLevelId());
                    if (year == null || level == null) throw new IllegalArgumentException("Année ou niveau du groupe introuvable.");
                    if (database.enrollmentDao().countMatchingGroup(groupId, year.getId(), level.getId()) == 0) {
                        throw new IllegalArgumentException("Le groupe ne correspond pas à son année et son niveau.");
                    }
                    Enrollment duplicate = database.enrollmentDao().getByStudentAndYear(studentId, year.getId());
                    if (duplicate != null && duplicate.getId() != enrollmentId) {
                        throw new IllegalArgumentException("Cet étudiant possède déjà une inscription pour cette année.");
                    }
                    String name = group.getSubject() == null ? "" : group.getSubject().trim();
                    if (name.isEmpty()) throw new IllegalArgumentException("La formation associée au groupe est manquante.");
                    Specialization specialization = database.specializationDao().getByName(name);
                    if (specialization == null) {
                        specialization = new Specialization("SPEC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT), name, null);
                        specialization.setId((int) database.specializationDao().insert(specialization));
                    }
                    current.setStudentId(studentId);
                    current.setAcademicYearId(year.getId());
                    current.setSpecializationId(specialization.getId());
                    current.setLevelId(level.getId());
                    if (current.getGroupId() != groupId) {
                        current.setGroupId(groupId);
                        current.setGroupOrder(database.enrollmentDao().getNextGroupOrder(groupId));
                    }
                    current.setStatus(status);
                    database.enrollmentDao().update(current);
                });
                message.postValue("Inscription modifiée.");
                enrollments.postValue(database.enrollmentDao().getAllWithDetails());
            } catch (Exception e) {
                String detail = e.getMessage();
                error.postValue(detail == null || detail.trim().isEmpty() ? "Impossible de modifier l’inscription." : detail);
            } finally {
                saving.postValue(false);
            }
        });
    }

    public void createEnrollment(String studentId, int groupId, String status) {
        saving.postValue(true);
        error.postValue(null);
        message.postValue(null);
        executor.execute(() -> {
            try {
                if (studentId == null || studentId.trim().isEmpty() || groupId <= 0) {
                    throw new IllegalArgumentException("Sélectionnez un étudiant et un groupe.");
                }
                if (!isKnownStatus(status)) throw new IllegalArgumentException("Sélectionnez un statut valide.");
                database.runInTransaction(() -> {
                    Student student = database.studentDao().getById(studentId);
                    StudentGroup group = database.studentGroupDao().getById(groupId);
                    if (student == null) throw new IllegalArgumentException("Étudiant introuvable.");
                    if (group == null || group.getLevelId() == null) {
                        throw new IllegalArgumentException("Le groupe sélectionné doit avoir un niveau valide.");
                    }
                    AcademicYear year = database.academicYearDao().getById(group.getAcademicYearId());
                    Level level = database.levelDao().getById(group.getLevelId());
                    if (year == null || level == null) throw new IllegalArgumentException("Année ou niveau du groupe introuvable.");
                    if (database.enrollmentDao().getByStudentAndYear(studentId, year.getId()) != null) {
                        throw new IllegalArgumentException("Cet étudiant possède déjà une inscription pour cette année.");
                    }
                    String name = group.getSubject() == null ? "" : group.getSubject().trim();
                    if (name.isEmpty()) throw new IllegalArgumentException("La formation associée au groupe est manquante.");
                    Specialization specialization = database.specializationDao().getByName(name);
                    if (specialization == null) {
                        specialization = new Specialization("SPEC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT), name, null);
                        specialization.setId((int) database.specializationDao().insert(specialization));
                    }
                    String date = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(new Date());
                    Enrollment enrollment = new Enrollment(studentId, year.getId(), specialization.getId(),
                            level.getId(), group.getId(), 0, status, date);
                    database.enrollmentDao().insertAssigningGroupOrder(enrollment);
                });
                message.postValue("Inscription créée.");
                enrollments.postValue(database.enrollmentDao().getAllWithDetails());
            } catch (Exception e) {
                String detail = e.getMessage();
                error.postValue(detail == null || detail.trim().isEmpty() ? "Impossible de créer l'inscription." : detail);
            } finally {
                saving.postValue(false);
            }
        });
    }

    public void deleteEnrollment(int enrollmentId) {
        saving.postValue(true);
        error.postValue(null);
        message.postValue(null);
        executor.execute(() -> {
            try {
                Enrollment enrollment = database.enrollmentDao().getById(enrollmentId);
                if (enrollment == null) throw new IllegalArgumentException("Cette inscription n’existe plus.");
                database.enrollmentDao().delete(enrollment);
                message.postValue("Inscription supprimée.");
                enrollments.postValue(database.enrollmentDao().getAllWithDetails());
            } catch (Exception e) {
                String detail = e.getMessage();
                error.postValue(detail == null || detail.trim().isEmpty() ? "Impossible de supprimer l’inscription." : detail);
            } finally {
                saving.postValue(false);
            }
        });
    }

    private boolean isKnownStatus(String status) {
        return Enrollment.STATUS_IN_PROGRESS.equals(status) || Enrollment.STATUS_VALIDATED.equals(status)
                || Enrollment.STATUS_REPEAT.equals(status) || Enrollment.STATUS_WITHDRAWN.equals(status);
    }

    @Override protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
