package com.example.praticum.ui.attendance;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.net.Uri;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.praticum.R;
import com.example.praticum.data.database.AppDatabase;
import com.example.praticum.data.database.DatabaseProvider;
import com.example.praticum.data.entity.AttendanceRecord;
import com.example.praticum.data.entity.AcademicYear;
import com.example.praticum.data.entity.Level;
import com.example.praticum.data.export.AttendanceExcelExporter;
import com.example.praticum.ui.groups.GroupsFragment;
import com.example.praticum.data.entity.SessionGroup;
import com.example.praticum.data.entity.Student;
import com.example.praticum.data.entity.StudentGroup;
import com.example.praticum.data.repository.AttendanceRecordRepository;
import com.example.praticum.data.repository.SessionGroupRepository;
import com.example.praticum.data.repository.StudentGroupRepository;
import com.example.praticum.data.repository.StudentRepository;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AttendanceSheetFragment extends Fragment {

    private int sessionId = -1;
    private String sessionTitle;

    private AppDatabase database;

    private AttendanceRecordRepository attendanceRepository;
    private SessionGroupRepository sessionGroupRepository;
    private StudentGroupRepository studentGroupRepository;
    private StudentRepository studentRepository;

    private ExecutorService databaseExecutor;

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private StudentGroup selectedStudentGroup;
    private SessionGroup currentSessionGroup;

    private final List<AttendanceRow> allRecords =
            new ArrayList<>();

    private final List<AttendanceRow> filteredRecords =
            new ArrayList<>();

    /*
     * Conservation temporaire des données des groupes visités.
     * Les entités persistantes restent SessionGroup et AttendanceRecord.
     */
    private final Map<Integer, SessionGroup> sessionGroupsByGroupId =
            new LinkedHashMap<>();

    private final Map<Integer, List<AttendanceRow>> recordsByGroupId =
            new LinkedHashMap<>();

    /*
     * Groupes dont les informations ou les statuts ont changé
     * depuis l'ouverture de l'écran.
     */
    private final Set<Integer> dirtyGroupIds =
            new LinkedHashSet<>();

    private int currentIndex = 0;
    private int groupLoadGeneration = 0;

    private boolean ignoreSearchChanges = false;
    private boolean isSaving = false;
    private boolean isExporting = false;
    private AttendanceExcelExporter.Data pendingExportData;
    private ActivityResultLauncher<String> exportFileLauncher;

    // En-tête
    private View btnBack;
    private android.widget.TextView tvSessionTitle;

    // Informations de la séance
    private TextInputLayout tilDate;
    private TextInputLayout tilStartTime;
    private TextInputLayout tilEndTime;
    private TextInputLayout tilRoom;

    private TextInputEditText etDate;
    private TextInputEditText etStartTime;
    private TextInputEditText etEndTime;
    private TextInputEditText etRoom;

    private MaterialAutoCompleteTextView actGroup;
    private View btnChooseGroup;

    // Progression et compteurs
    private android.widget.TextView tvGroupStudentCount;
    private android.widget.TextView tvProgress;
    private LinearProgressIndicator progressIndicator;
    private android.widget.TextView tvRemaining;

    private android.widget.TextView tvPresentCount;
    private android.widget.TextView tvAbsentCount;
    private android.widget.TextView tvLateCount;

    // Recherche et étudiant courant
    private TextInputEditText etStudentSearch;
    private android.widget.TextView tvStudentPosition;

    private View cardCurrentStudent;
    private android.widget.TextView tvInitials;
    private android.widget.TextView tvFullName;
    private android.widget.TextView tvArabicName;
    private android.widget.TextView tvStudentGroup;
    private android.widget.TextView tvMatricule;
    private android.widget.TextView tvGroupOrder;

    // Statut
    private RadioGroup rgStatus;
    private RadioButton rbPresent;
    private RadioButton rbAbsent;
    private RadioButton rbLate;
    private android.widget.TextView tvUnmarked;

    // Navigation et sauvegarde
    private View btnPrevious;
    private View btnNext;
    private View btnSave;
    private View btnExport;
    private android.widget.TextView tvSaveInfo;

    public AttendanceSheetFragment() {
        exportFileLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                this::onExportDestinationSelected
        );
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_attendance_sheet,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();

        if (args != null) {
            sessionId = args.getInt("session_id", -1);
            sessionTitle = args.getString(
                    "session_title",
                    "Feuille de présence"
            );
        }

        database = DatabaseProvider.getDatabase(
                requireContext()
        );

        attendanceRepository = new AttendanceRecordRepository(
                database,
                database.attendanceRecordDao()
        );

        sessionGroupRepository = new SessionGroupRepository(
                database.sessionGroupDao()
        );

        studentGroupRepository = new StudentGroupRepository(
                database.studentGroupDao()
        );

        studentRepository = new StudentRepository(
                database,
                database.studentDao()
        );

        if (databaseExecutor == null
                || databaseExecutor.isShutdown()) {
            databaseExecutor =
                    Executors.newSingleThreadExecutor();
        }

        bindViews(view);
        configureHeader();
        configureDateAndTimePickers();
        configureGroupSelector();
        configureStudentSearch();
        configureNavigation();
        configureSaveButton();
        configureExportButton();

        if (sessionId <= 0) {
            showNoStudents();

            actGroup.setEnabled(false);
            btnChooseGroup.setEnabled(false);
            btnSave.setEnabled(false);

            Toast.makeText(
                    requireContext(),
                    "Séance invalide.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        afficherAucunGroupeSelectionne();
    }

    private void bindViews(View view) {
        btnBack = view.findViewById(
                R.id.btnAttendanceSheetBack
        );

        tvSessionTitle = view.findViewById(
                R.id.tvAttendanceSheetSessionTitle
        );

        tilDate = view.findViewById(
                R.id.tilSessionDate
        );

        tilStartTime = view.findViewById(
                R.id.tilAttendanceStartTime
        );

        tilEndTime = view.findViewById(
                R.id.tilAttendanceEndTime
        );

        tilRoom = view.findViewById(
                R.id.tilAttendanceRoom
        );

        etDate = view.findViewById(
                R.id.etAttendanceDate
        );

        etStartTime = view.findViewById(
                R.id.etAttendanceStartTime
        );

        etEndTime = view.findViewById(
                R.id.etAttendanceEndTime
        );

        etRoom = view.findViewById(
                R.id.etAttendanceRoom
        );

        actGroup = view.findViewById(
                R.id.actAttendanceGroup
        );

        btnChooseGroup = view.findViewById(
                R.id.btnChooseAttendanceGroup
        );

        tvGroupStudentCount = view.findViewById(
                R.id.tvAttendanceGroupStudentCount
        );

        tvProgress = view.findViewById(
                R.id.tvAttendanceSheetProgress
        );

        progressIndicator = view.findViewById(
                R.id.progressAttendanceSheet
        );

        tvRemaining = view.findViewById(
                R.id.tvAttendanceSheetRemaining
        );

        tvPresentCount = view.findViewById(
                R.id.tvAttendanceSheetPresentCount
        );

        tvAbsentCount = view.findViewById(
                R.id.tvAttendanceSheetAbsentCount
        );

        tvLateCount = view.findViewById(
                R.id.tvAttendanceSheetLateCount
        );

        etStudentSearch = view.findViewById(
                R.id.etAttendanceStudentSearch
        );

        tvStudentPosition = view.findViewById(
                R.id.tvAttendanceSheetStudentPosition
        );

        cardCurrentStudent = view.findViewById(
                R.id.cardAttendanceCurrentStudent
        );

        tvInitials = view.findViewById(
                R.id.tvAttendanceStudentInitials
        );

        tvFullName = view.findViewById(
                R.id.tvAttendanceStudentFullName
        );

        tvArabicName = view.findViewById(
                R.id.tvAttendanceStudentNameArabic
        );

        tvStudentGroup = view.findViewById(
                R.id.tvAttendanceStudentGroup
        );

        tvMatricule = view.findViewById(
                R.id.tvAttendanceStudentMatricule
        );

        tvGroupOrder = view.findViewById(
                R.id.tvAttendanceStudentGroupOrder
        );

        rgStatus = view.findViewById(
                R.id.rgAttendanceStudentStatus
        );

        rbPresent = view.findViewById(
                R.id.rbAttendancePresent
        );

        rbAbsent = view.findViewById(
                R.id.rbAttendanceAbsent
        );

        rbLate = view.findViewById(
                R.id.rbAttendanceLate
        );

        tvUnmarked = view.findViewById(
                R.id.tvAttendanceStudentUnmarked
        );

        btnPrevious = view.findViewById(
                R.id.btnAttendancePreviousStudent
        );

        btnNext = view.findViewById(
                R.id.btnAttendanceNextStudent
        );

        btnSave = view.findViewById(
                R.id.btnSaveAttendanceSheet
        );
        btnExport = view.findViewById(R.id.btnExportAttendanceExcel);

        tvSaveInfo = view.findViewById(
                R.id.tvAttendanceSheetSaveInfo
        );
    }

    private void configureHeader() {
        tvSessionTitle.setText(
                sessionTitle != null
                        && !sessionTitle.trim().isEmpty()
                        ? sessionTitle
                        : "Feuille de présence"
        );

        btnBack.setOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack()
        );
    }

    // ---------------------------------------------------------
    // Chargement des groupes depuis Room
    // ---------------------------------------------------------

    private void configureGroupSelector() {
        btnChooseGroup.setOnClickListener(v -> ouvrirSelecteurGroupes());

        getParentFragmentManager().setFragmentResultListener(
                GroupsFragment.RESULT_KEY,
                getViewLifecycleOwner(),
                (key, result) -> {
                    int groupId = result.getInt(
                            GroupsFragment.RESULT_GROUP_ID,
                            -1
                    );
                    if (groupId > 0) {
                        chargerGroupeSelectionne(groupId);
                    }
                }
        );
    }

    private void ouvrirSelecteurGroupes() {
        if (isSaving || !isAdded()) {
            return;
        }

        saveCurrentGroupFields();
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.mainContainer,
                        GroupsFragment.newInstanceForSelection()
                )
                .addToBackStack("attendance_choose_group")
                .commit();
    }

    private void chargerGroupeSelectionne(int groupId) {
        databaseExecutor.execute(() -> {
            try {
                StudentGroup group = studentGroupRepository.getById(groupId);
                mainHandler.post(() -> {
                    if (!isViewReady()) {
                        return;
                    }
                    if (group == null) {
                        Toast.makeText(
                                requireContext(),
                                "Le groupe sélectionné est introuvable.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }
                    loadGroup(group);
                });
            } catch (Exception exception) {
                mainHandler.post(() -> {
                    if (isViewReady()) {
                        Toast.makeText(
                                requireContext(),
                                errorMessage(exception),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }
        });
    }

    private void afficherAucunGroupeSelectionne() {
        selectedStudentGroup = null;
        currentSessionGroup = null;
        actGroup.setText("", false);
        actGroup.setEnabled(true);
        btnChooseGroup.setEnabled(true);
        btnSave.setEnabled(false);
        cardCurrentStudent.setVisibility(View.GONE);
        tvStudentPosition.setText("Sélectionnez un groupe pour commencer.");
        tvGroupStudentCount.setText("Aucun groupe sélectionné.");
        allRecords.clear();
        filteredRecords.clear();
        currentIndex = 0;
        updateCounters();
        updateNavigationButtons();
    }

    private void loadGroup(StudentGroup group) {
        if (group == null) {
            return;
        }

        saveCurrentGroupFields();

        selectedStudentGroup = group;

        actGroup.setText(
                safe(group.getName()),
                false
        );

        int groupId = group.getId();
        int requestId = ++groupLoadGeneration;

        /*
         * Si le groupe a déjà été chargé, on réutilise son état
         * temporaire sans relire les données.
         */
        if (sessionGroupsByGroupId.containsKey(groupId)
                && recordsByGroupId.containsKey(groupId)) {

            displayGroup(
                    group,
                    sessionGroupsByGroupId.get(groupId),
                    recordsByGroupId.get(groupId)
            );

            return;
        }

        actGroup.setEnabled(false);
        btnChooseGroup.setEnabled(false);
        btnSave.setEnabled(false);

        cardCurrentStudent.setVisibility(View.GONE);
        tvStudentPosition.setText("Chargement des étudiants…");

        databaseExecutor.execute(() -> {
            try {
                SessionGroup sessionGroup =
                        sessionGroupRepository.getBySessionAndGroup(
                                sessionId,
                                groupId
                        );

                List<Student> students =
                        studentRepository.getByGroupId(groupId);

                Map<String, Integer> groupOrders = new HashMap<>();
                for (com.example.praticum.data.entity.Enrollment enrollment : database.enrollmentDao().getByGroup(groupId)) {
                    groupOrders.put(enrollment.getStudentId(), enrollment.getGroupOrder());
                }

                List<AttendanceRecord> savedRecords =
                        sessionGroup != null
                                && sessionGroup.getId() > 0
                                ? attendanceRepository
                                .getBySessionGroupId(
                                        sessionGroup.getId()
                                )
                                : Collections.emptyList();

                Map<String, String> savedStatuses =
                        new HashMap<>();

                for (AttendanceRecord record : savedRecords) {
                    savedStatuses.put(
                            record.getStudentId(),
                            record.getStatus()
                    );
                }

                List<AttendanceRow> rows =
                        new ArrayList<>();

                if (students != null) {
                    for (Student student : students) {
                        String status = savedStatuses.get(
                                student.getStudentId()
                        );

                        rows.add(
                                new AttendanceRow(student, status, groupOrders.containsKey(student.getStudentId()) ? groupOrders.get(student.getStudentId()) : 0)
                        );
                    }
                }

                if (sessionGroup == null) {
                    sessionGroup = new SessionGroup(
                            sessionId,
                            groupId
                    );
                }

                SessionGroup loadedSessionGroup = sessionGroup;

                mainHandler.post(() -> {
                    if (!isViewReady()
                            || requestId != groupLoadGeneration) {
                        return;
                    }

                    sessionGroupsByGroupId.put(
                            groupId,
                            loadedSessionGroup
                    );

                    recordsByGroupId.put(
                            groupId,
                            rows
                    );

                    displayGroup(
                            group,
                            loadedSessionGroup,
                            rows
                    );
                });

            } catch (Exception exception) {
                mainHandler.post(() -> {
                    if (!isViewReady()
                            || requestId != groupLoadGeneration) {
                        return;
                    }

                    actGroup.setEnabled(true);
                    btnChooseGroup.setEnabled(true);
                    btnSave.setEnabled(false);

                    Toast.makeText(
                            requireContext(),
                            errorMessage(exception),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private void displayGroup(
            StudentGroup group,
            SessionGroup sessionGroup,
            List<AttendanceRow> rows) {

        selectedStudentGroup = group;
        currentSessionGroup = sessionGroup;

        sessionGroupsByGroupId.put(
                group.getId(),
                sessionGroup
        );

        recordsByGroupId.put(
                group.getId(),
                rows
        );

        allRecords.clear();
        allRecords.addAll(rows);

        filteredRecords.clear();
        filteredRecords.addAll(allRecords);

        currentIndex = 0;

        ignoreSearchChanges = true;
        etStudentSearch.setText("");
        ignoreSearchChanges = false;

        actGroup.setText(
                safe(group.getName()),
                false
        );

        actGroup.setEnabled(true);
        btnChooseGroup.setEnabled(true);
        btnSave.setEnabled(true);

        populateGroupFields();
        renderCurrentStudent();
        updateCounters();
        updateNavigationButtons();
    }

    private void populateGroupFields() {
        if (currentSessionGroup == null) {
            return;
        }

        etDate.setText(
                toDisplayDate(
                        currentSessionGroup.getSessionDate()
                )
        );

        etStartTime.setText(
                safe(currentSessionGroup.getStartTime())
        );

        etEndTime.setText(
                safe(currentSessionGroup.getEndTime())
        );

        etRoom.setText(
                safe(currentSessionGroup.getRoom())
        );

        actGroup.setText(
                selectedStudentGroup == null
                        ? ""
                        : safe(selectedStudentGroup.getName()),
                false
        );

        tvGroupStudentCount.setText(
                String.format(
                        Locale.getDefault(),
                        "%d étudiants",
                        allRecords.size()
                )
        );
    }

    // ---------------------------------------------------------
    // Date et heures
    // ---------------------------------------------------------

    private void configureDateAndTimePickers() {
        etDate.setOnClickListener(
                v -> openDatePicker()
        );

        tilDate.setEndIconOnClickListener(
                v -> openDatePicker()
        );

        etStartTime.setOnClickListener(
                v -> openTimePicker(etStartTime)
        );

        tilStartTime.setEndIconOnClickListener(
                v -> openTimePicker(etStartTime)
        );

        etEndTime.setOnClickListener(
                v -> openTimePicker(etEndTime)
        );

        tilEndTime.setEndIconOnClickListener(
                v -> openTimePicker(etEndTime)
        );
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();

        String currentDate = getText(etDate);

        if (!currentDate.isEmpty()) {
            try {
                String[] parts = currentDate.split("/");

                if (parts.length == 3) {
                    calendar.set(
                            Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[1]) - 1,
                            Integer.parseInt(parts[0])
                    );
                }
            } catch (NumberFormatException ignored) {
                // Utiliser la date actuelle.
            }
        }

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (picker, year, month, day) -> {

                    String value = String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            day,
                            month + 1,
                            year
                    );

                    etDate.setText(value);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void openTimePicker(TextInputEditText target) {
        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        String value = getText(target);

        if (!value.isEmpty()) {
            try {
                String[] parts = value.split(":");

                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);

            } catch (Exception ignored) {
                // Conserver l'heure actuelle.
            }
        }

        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (picker, selectedHour, selectedMinute) -> {

                    String time = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            selectedHour,
                            selectedMinute
                    );

                    target.setText(time);
                },
                hour,
                minute,
                true
        );

        dialog.show();
    }

    /*
     * L'interface affiche dd/MM/yyyy.
     * Les dates normalisées en base sont conservées sous yyyy-MM-dd.
     */

    private String toDisplayDate(String storedDate) {
        if (storedDate == null || storedDate.trim().isEmpty()) {
            return "";
        }

        String[] parts = storedDate.split("-");

        if (parts.length == 3 && parts[0].length() == 4) {
            return parts[2]
                    + "/"
                    + parts[1]
                    + "/"
                    + parts[0];
        }

        return storedDate;
    }

    private String toStorageDate(String displayDate) {
        if (displayDate == null || displayDate.trim().isEmpty()) {
            return "";
        }

        String[] parts = displayDate.split("/");

        if (parts.length == 3 && parts[2].length() == 4) {
            return parts[2]
                    + "-"
                    + parts[1]
                    + "-"
                    + parts[0];
        }

        return displayDate;
    }

    // ---------------------------------------------------------
    // Recherche
    // ---------------------------------------------------------

    private void configureStudentSearch() {
        etStudentSearch.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        if (!ignoreSearchChanges) {
                            filterStudents(
                                    s == null ? "" : s.toString()
                            );
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );
    }

    private void filterStudents(String query) {
        filteredRecords.clear();

        String normalizedQuery = query == null
                ? ""
                : query.trim().toLowerCase(Locale.ROOT);

        for (AttendanceRow row : allRecords) {
            Student student = row.student;

            String fullName = safe(student.getFullName());
            String arabicName = safe(student.getFullNameArabic());
            String matricule = safe(student.getMatricule());

            boolean matches =
                    normalizedQuery.isEmpty()
                            || fullName.toLowerCase(Locale.ROOT)
                            .contains(normalizedQuery)
                            || matricule.toLowerCase(Locale.ROOT)
                            .contains(normalizedQuery)
                            || arabicName.contains(normalizedQuery);

            if (matches) {
                filteredRecords.add(row);
            }
        }

        currentIndex = 0;

        renderCurrentStudent();
        updateNavigationButtons();
    }

    // ---------------------------------------------------------
    // Affichage de l'étudiant et statut
    // ---------------------------------------------------------

    private void renderCurrentStudent() {
        if (filteredRecords.isEmpty()) {
            showNoStudents();
            return;
        }

        cardCurrentStudent.setVisibility(View.VISIBLE);

        AttendanceRow row = filteredRecords.get(currentIndex);
        Student student = row.student;

        String fullName = safe(student.getFullName());

        tvFullName.setText(fullName);
        tvArabicName.setText(
                safe(student.getFullNameArabic())
        );

        tvStudentGroup.setText(
                selectedStudentGroup == null
                        ? ""
                        : safe(selectedStudentGroup.getName())
        );

        tvMatricule.setText(
                safe(student.getMatricule())
        );

        tvGroupOrder.setText(
                String.valueOf(row.groupOrder)
        );

        tvInitials.setText(getInitials(fullName));

        tvStudentPosition.setText(
                String.format(
                        Locale.getDefault(),
                        "Étudiant %d sur %d",
                        currentIndex + 1,
                        filteredRecords.size()
                )
        );

        rgStatus.setOnCheckedChangeListener(null);

        if (AttendanceRecord.PRESENT.equals(row.status)) {
            rbPresent.setChecked(true);

        } else if (AttendanceRecord.ABSENT.equals(row.status)) {
            rbAbsent.setChecked(true);

        } else if (AttendanceRecord.LATE.equals(row.status)) {
            rbLate.setChecked(true);

        } else {
            rgStatus.clearCheck();
        }

        tvUnmarked.setVisibility(
                row.isUnmarked()
                        ? View.VISIBLE
                        : View.GONE
        );

        rgStatus.setOnCheckedChangeListener(
                this::onStatusChanged
        );
    }

    private void onStatusChanged(
            RadioGroup group,
            int checkedId) {

        if (filteredRecords.isEmpty()
                || selectedStudentGroup == null) {
            return;
        }

        AttendanceRow row = filteredRecords.get(currentIndex);

        if (checkedId == R.id.rbAttendancePresent) {
            row.status = AttendanceRecord.PRESENT;

        } else if (checkedId == R.id.rbAttendanceAbsent) {
            row.status = AttendanceRecord.ABSENT;

        } else if (checkedId == R.id.rbAttendanceLate) {
            row.status = AttendanceRecord.LATE;

        } else {
            return;
        }

        dirtyGroupIds.add(
                selectedStudentGroup.getId()
        );

        tvUnmarked.setVisibility(View.GONE);

        updateCounters();
    }

    private void showNoStudents() {
        cardCurrentStudent.setVisibility(View.GONE);

        tvStudentPosition.setText(
                "Aucun étudiant trouvé"
        );

        tvGroupStudentCount.setText(
                String.format(
                        Locale.getDefault(),
                        "%d étudiants",
                        allRecords.size()
                )
        );

        updateNavigationButtons();
        updateCounters();
    }

    // ---------------------------------------------------------
    // Navigation entre étudiants
    // ---------------------------------------------------------

    private void configureNavigation() {
        btnPrevious.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;

                renderCurrentStudent();
                updateNavigationButtons();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentIndex < filteredRecords.size() - 1) {
                currentIndex++;

                renderCurrentStudent();
                updateNavigationButtons();
            }
        });
    }

    private void updateNavigationButtons() {
        boolean hasStudents = !filteredRecords.isEmpty();

        btnPrevious.setEnabled(
                hasStudents && currentIndex > 0
        );

        btnNext.setEnabled(
                hasStudents
                        && currentIndex < filteredRecords.size() - 1
        );

        btnPrevious.setAlpha(
                hasStudents && currentIndex > 0 ? 1f : 0.4f
        );

        btnNext.setAlpha(
                hasStudents
                        && currentIndex < filteredRecords.size() - 1
                        ? 1f
                        : 0.4f
        );
    }

    // ---------------------------------------------------------
    // Compteurs et progression
    // ---------------------------------------------------------

    private void updateCounters() {
        int present = 0;
        int absent = 0;
        int late = 0;
        int marked = 0;

        for (AttendanceRow row : allRecords) {
            if (AttendanceRecord.PRESENT.equals(row.status)) {
                present++;
                marked++;

            } else if (AttendanceRecord.ABSENT.equals(row.status)) {
                absent++;
                marked++;

            } else if (AttendanceRecord.LATE.equals(row.status)) {
                late++;
                marked++;
            }
        }

        int total = allRecords.size();
        int remaining = total - marked;

        tvPresentCount.setText(String.valueOf(present));
        tvAbsentCount.setText(String.valueOf(absent));
        tvLateCount.setText(String.valueOf(late));

        tvProgress.setText(
                String.format(
                        Locale.getDefault(),
                        "%d / %d",
                        marked,
                        total
                )
        );

        int percentage = total == 0
                ? 0
                : Math.round(marked * 100f / total);

        progressIndicator.setMax(100);
        progressIndicator.setProgress(percentage);

        tvRemaining.setText(
                remaining == 1
                        ? "1 étudiant reste à renseigner"
                        : remaining
                        + " étudiants restent à renseigner"
        );

        tvSaveInfo.setText(
                remaining == 0
                        ? "Tous les étudiants ont un statut."
                        : "Les étudiants sans statut resteront incomplets."
        );
    }

    // ---------------------------------------------------------
    // Enregistrement Room
    // ---------------------------------------------------------

    private void configureSaveButton() {
        btnSave.setOnClickListener(
                v -> saveAttendanceSheet()
        );
    }

    private void configureExportButton() {
        btnExport.setOnClickListener(v -> exportCurrentGroup());
    }

    private void exportCurrentGroup() {
        if (isSaving || isExporting) return;
        if (selectedStudentGroup == null || currentSessionGroup == null) {
            Toast.makeText(requireContext(), "Sélectionnez un groupe avant l’export.", Toast.LENGTH_SHORT).show();
            return;
        }
        saveCurrentGroupFields();
        final StudentGroup group = selectedStudentGroup;
        final String groupCode = safe(group.getCode());
        final String groupName = safe(group.getName());
        final String specialty = safe(group.getSubject());
        final String date = getText(etDate);
        final String start = getText(etStartTime);
        final String end = getText(etEndTime);
        final String room = getText(etRoom);
        final List<AttendanceExcelExporter.StudentLine> students = new ArrayList<>();
        for (AttendanceRow row : allRecords) {
            Student student = row.student;
            students.add(new AttendanceExcelExporter.StudentLine(row.groupOrder,
                    student.getMatricule(), student.getLastName(), student.getFirstName(),
                    student.getLastNameArabic(), student.getFirstNameArabic(), row.status));
        }
        isExporting = true;
        btnExport.setEnabled(false);
        databaseExecutor.execute(() -> {
            try {
                Level level = group.getLevelId() == null ? null : database.levelDao().getById(group.getLevelId());
                AcademicYear year = database.academicYearDao().getById(group.getAcademicYearId());
                String levelName = level == null ? "" : safe(level.getCode())
                        + (safe(level.getName()).isEmpty() ? "" : " — " + safe(level.getName()));
                AttendanceExcelExporter.Data data = new AttendanceExcelExporter.Data(
                        sessionTitle, groupName, groupCode, specialty, levelName,
                        year == null ? "" : year.getLabel(), date, start, end, room, students);
                String fileName = "Presence_" + safeFilePart(groupCode.isEmpty() ? groupName : groupCode)
                        + "_" + safeFilePart(date.isEmpty() ? "seance" : date.replace('/', '-')) + ".xlsx";
                mainHandler.post(() -> {
                    if (!isViewReady()) return;
                    pendingExportData = data;
                    exportFileLauncher.launch(fileName);
                });
            } catch (Exception exception) {
                mainHandler.post(() -> finishExport(errorMessage(exception)));
            }
        });
    }

    private void onExportDestinationSelected(Uri uri) {
        AttendanceExcelExporter.Data data = pendingExportData;
        pendingExportData = null;
        if (uri == null) { finishExport("Export annulé."); return; }
        if (data == null || !isAdded()) { finishExport("Impossible de préparer le fichier Excel."); return; }
        ContentResolver resolver = requireContext().getContentResolver();
        databaseExecutor.execute(() -> {
            try (OutputStream output = resolver.openOutputStream(uri, "w")) {
                if (output == null) throw new IllegalStateException("Impossible d’écrire le fichier sélectionné.");
                AttendanceExcelExporter.write(output, data);
                mainHandler.post(() -> finishExport("Feuille de présence exportée en Excel."));
            } catch (Exception exception) {
                mainHandler.post(() -> finishExport(errorMessage(exception)));
            }
        });
    }

    private void finishExport(String message) {
        isExporting = false;
        if (!isViewReady()) return;
        if (btnExport != null) btnExport.setEnabled(currentSessionGroup != null);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private String safeFilePart(String value) {
        String cleaned = value == null ? "" : value.trim().replaceAll("[\\\\/:*?\\\"<>|]", "_");
        return cleaned.isEmpty() ? "groupe" : cleaned;
    }
    private void saveAttendanceSheet() {
        if (isSaving) {
            return;
        }

        if (currentSessionGroup == null
                || selectedStudentGroup == null) {

            Toast.makeText(
                    requireContext(),
                    "Aucun groupe sélectionné.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String date = getText(etDate);
        String startTime = getText(etStartTime);
        String endTime = getText(etEndTime);
        String room = getText(etRoom);

        if (date.isEmpty()
                || startTime.isEmpty()
                || endTime.isEmpty()
                || room.isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Renseignez la date, les heures et la salle.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        int startMinutes = parseTimeToMinutes(startTime);
        int endMinutes = parseTimeToMinutes(endTime);

        if (startMinutes < 0 || endMinutes < 0) {
            Toast.makeText(
                    requireContext(),
                    "Format d'heure invalide.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (endMinutes <= startMinutes) {
            tilEndTime.setError(
                    "L'heure de fin doit être après l'heure de début."
            );

            return;
        }

        tilEndTime.setError(null);

        saveCurrentGroupFields();

        /*
         * Le groupe actuellement affiché doit toujours être enregistré.
         */
        dirtyGroupIds.add(selectedStudentGroup.getId());

        List<Integer> groupIdsToSave =
                new ArrayList<>(dirtyGroupIds);

        List<SessionGroup> groupsToSave =
                new ArrayList<>();

        for (Integer groupId : groupIdsToSave) {
            SessionGroup sessionGroup =
                    sessionGroupsByGroupId.get(groupId);

            if (sessionGroup != null) {
                groupsToSave.add(sessionGroup);
            }
        }

        isSaving = true;

        btnSave.setEnabled(false);
        actGroup.setEnabled(false);
        btnChooseGroup.setEnabled(false);

        databaseExecutor.execute(() -> {
            try {
                for (SessionGroup sessionGroup : groupsToSave) {
                    saveGroupToDatabase(sessionGroup);
                }

                mainHandler.post(() -> {
                    if (!isViewReady()) {
                        return;
                    }

                    dirtyGroupIds.removeAll(groupIdsToSave);

                    isSaving = false;

                    Toast.makeText(
                            requireContext(),
                            "Feuille de présence enregistrée.",
                            Toast.LENGTH_SHORT
                    ).show();

                    requireActivity()
                            .getSupportFragmentManager()
                            .popBackStack();
                });

            } catch (Exception exception) {
                mainHandler.post(() -> {
                    if (!isViewReady()) {
                        return;
                    }

                    isSaving = false;

                    btnSave.setEnabled(true);
                    actGroup.setEnabled(true);
                    btnChooseGroup.setEnabled(true);

                    Toast.makeText(
                            requireContext(),
                            errorMessage(exception),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private void saveGroupToDatabase(
            SessionGroup sessionGroup) {

        int groupId = sessionGroup.getGroupId();

        /*
         * Créer l'association séance-groupe si elle n'existe pas.
         * Sinon, mettre à jour ses informations.
         */
        if (sessionGroup.getId() <= 0) {
            SessionGroup existing =
                    sessionGroupRepository.getBySessionAndGroup(
                            sessionGroup.getSessionId(),
                            groupId
                    );

            if (existing == null) {
                long generatedId =
                        sessionGroupRepository.insert(
                                sessionGroup
                        );

                sessionGroup.setId((int) generatedId);

            } else {
                sessionGroup.setId(existing.getId());

                sessionGroupRepository.update(
                        sessionGroup
                );
            }

        } else {
            sessionGroupRepository.update(sessionGroup);
        }

        int sessionGroupId = sessionGroup.getId();

        List<AttendanceRow> rows =
                recordsByGroupId.get(groupId);

        if (rows == null) {
            return;
        }

        for (AttendanceRow row : rows) {
            if (row.isUnmarked()) {
                /*
                 * L'absence de ligne Room représente
                 * le statut « Non renseigné ».
                 */
                attendanceRepository.setUnreported(
                        sessionGroupId,
                        row.student.getStudentId()
                );

            } else {
                AttendanceRecord record =
                        new AttendanceRecord(
                                sessionGroupId,
                                row.student.getStudentId(),
                                row.status
                        );

                attendanceRepository.save(record);
            }
        }
    }

    private void saveCurrentGroupFields() {
        if (currentSessionGroup == null
                || selectedStudentGroup == null) {
            return;
        }

        String date = toStorageDate(
                getText(etDate)
        );

        String startTime = getText(etStartTime);
        String endTime = getText(etEndTime);
        String room = getText(etRoom);

        boolean changed =
                !same(
                        currentSessionGroup.getSessionDate(),
                        date
                )
                        || !same(
                        currentSessionGroup.getStartTime(),
                        startTime
                )
                        || !same(
                        currentSessionGroup.getEndTime(),
                        endTime
                )
                        || !same(
                        currentSessionGroup.getRoom(),
                        room
                );

        currentSessionGroup.setSessionDate(date);
        currentSessionGroup.setStartTime(startTime);
        currentSessionGroup.setEndTime(endTime);
        currentSessionGroup.setRoom(room);

        sessionGroupsByGroupId.put(
                selectedStudentGroup.getId(),
                currentSessionGroup
        );

        if (changed) {
            dirtyGroupIds.add(
                    selectedStudentGroup.getId()
            );
        }
    }

    // ---------------------------------------------------------
    // Modèle temporaire pour l'affichage d'un étudiant
    // ---------------------------------------------------------

    private static class AttendanceRow {

        private final Student student;
        private final int groupOrder;
        private String status;

        AttendanceRow(
                Student student,
                String status, int groupOrder) {

            this.student = student;
            this.status = status;
            this.groupOrder = groupOrder;
        }

        boolean isUnmarked() {
            return status == null
                    || status.trim().isEmpty();
        }
    }

    // ---------------------------------------------------------
    // Utilitaires
    // ---------------------------------------------------------

    private String getText(TextInputEditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }

        return editText.getText()
                .toString()
                .trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean same(String first, String second) {
        if (first == null) {
            return second == null;
        }

        return first.equals(second);
    }

    private boolean isViewReady() {
        return isAdded() && getView() != null;
    }

    private String errorMessage(Exception exception) {
        if (exception == null) {
            return "Une erreur est survenue.";
        }

        String message = exception.getMessage();

        return message == null || message.trim().isEmpty()
                ? "Une erreur est survenue."
                : message;
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "?";
        }

        String[] parts = fullName.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0]
                    .substring(0, 1)
                    .toUpperCase(Locale.ROOT);
        }

        return (
                parts[0].substring(0, 1)
                        + parts[parts.length - 1].substring(0, 1)
        ).toUpperCase(Locale.ROOT);
    }

    private int parseTimeToMinutes(String time) {
        try {
            String[] parts = time.split(":");

            if (parts.length != 2) {
                return -1;
            }

            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);

            if (hours < 0 || hours > 23
                    || minutes < 0 || minutes > 59) {
                return -1;
            }

            return hours * 60 + minutes;

        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    @Override
    public void onDestroy() {
        if (databaseExecutor != null) {
            databaseExecutor.shutdown();
        }

        super.onDestroy();
    }
}
