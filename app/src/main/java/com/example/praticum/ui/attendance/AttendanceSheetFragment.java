package com.example.praticum.ui.attendance;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.praticum.R;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AttendanceSheetFragment extends Fragment {

    private int sessionId;
    private String sessionTitle;

    private AttendanceSheet currentSheet;
    private String selectedGroup;

    private final List<AttendanceRecordMemory> allRecords = new ArrayList<>();
    private final List<AttendanceRecordMemory> filteredRecords = new ArrayList<>();

    private int currentIndex = 0;

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
    private android.widget.TextView tvSaveInfo;

    public AttendanceSheetFragment() {
        // Constructeur obligatoire
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

        bindViews(view);
        configureHeader();
        configureDateAndTimePickers();
        configureGroupSelector();
        configureStudentSearch();
        configureNavigation();
        configureSaveButton();

        // Sélectionner automatiquement le premier groupe.
        String[] groups = AttendanceDataMemory.getGroups().toArray(new String[0]);

        if (groups != null && groups.length > 0) {
            selectedGroup = groups[0];
            actGroup.setText(selectedGroup, false);
            loadGroup(selectedGroup);
        } else {
            showNoStudents();
            Toast.makeText(
                    requireContext(),
                    "Aucun groupe disponible.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void bindViews(View view) {
        btnBack = view.findViewById(R.id.btnAttendanceSheetBack);
        tvSessionTitle =
                view.findViewById(R.id.tvAttendanceSheetSessionTitle);

        tilDate = view.findViewById(R.id.tilSessionDate);
        tilStartTime =
                view.findViewById(R.id.tilAttendanceStartTime);
        tilEndTime =
                view.findViewById(R.id.tilAttendanceEndTime);
        tilRoom = view.findViewById(R.id.tilAttendanceRoom);

        etDate = view.findViewById(R.id.etAttendanceDate);
        etStartTime =
                view.findViewById(R.id.etAttendanceStartTime);
        etEndTime =
                view.findViewById(R.id.etAttendanceEndTime);
        etRoom = view.findViewById(R.id.etAttendanceRoom);

        actGroup = view.findViewById(R.id.actAttendanceGroup);

        tvGroupStudentCount =
                view.findViewById(R.id.tvAttendanceGroupStudentCount);
        tvProgress =
                view.findViewById(R.id.tvAttendanceSheetProgress);
        progressIndicator =
                view.findViewById(R.id.progressAttendanceSheet);
        tvRemaining =
                view.findViewById(R.id.tvAttendanceSheetRemaining);

        tvPresentCount =
                view.findViewById(R.id.tvAttendanceSheetPresentCount);
        tvAbsentCount =
                view.findViewById(R.id.tvAttendanceSheetAbsentCount);
        tvLateCount =
                view.findViewById(R.id.tvAttendanceSheetLateCount);

        etStudentSearch =
                view.findViewById(R.id.etAttendanceStudentSearch);
        tvStudentPosition =
                view.findViewById(R.id.tvAttendanceSheetStudentPosition);

        cardCurrentStudent =
                view.findViewById(R.id.cardAttendanceCurrentStudent);
        tvInitials =
                view.findViewById(R.id.tvAttendanceStudentInitials);
        tvFullName =
                view.findViewById(R.id.tvAttendanceStudentFullName);
        tvArabicName =
                view.findViewById(R.id.tvAttendanceStudentNameArabic);
        tvStudentGroup =
                view.findViewById(R.id.tvAttendanceStudentGroup);
        tvMatricule =
                view.findViewById(R.id.tvAttendanceStudentMatricule);
        tvGroupOrder =
                view.findViewById(R.id.tvAttendanceStudentGroupOrder);

        rgStatus =
                view.findViewById(R.id.rgAttendanceStudentStatus);
        rbPresent =
                view.findViewById(R.id.rbAttendancePresent);
        rbAbsent =
                view.findViewById(R.id.rbAttendanceAbsent);
        rbLate =
                view.findViewById(R.id.rbAttendanceLate);
        tvUnmarked =
                view.findViewById(R.id.tvAttendanceStudentUnmarked);

        btnPrevious =
                view.findViewById(R.id.btnAttendancePreviousStudent);
        btnNext =
                view.findViewById(R.id.btnAttendanceNextStudent);
        btnSave =
                view.findViewById(R.id.btnSaveAttendanceSheet);
        tvSaveInfo =
                view.findViewById(R.id.tvAttendanceSheetSaveInfo);
    }

    private void configureHeader() {
        tvSessionTitle.setText(
                sessionTitle != null && !sessionTitle.trim().isEmpty()
                        ? sessionTitle
                        : "Feuille de présence"
        );

        btnBack.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });
    }

    // ---------------------------------------------------------
    // Date et heures
    // ---------------------------------------------------------

    private void configureDateAndTimePickers() {
        View.OnClickListener dateClick = v -> openDatePicker();

        etDate.setOnClickListener(dateClick);
        tilDate.setEndIconOnClickListener(v -> openDatePicker());

        View.OnClickListener startClick = v -> openTimePicker(etStartTime);
        etStartTime.setOnClickListener(startClick);
        tilStartTime.setEndIconOnClickListener(
                v -> openTimePicker(etStartTime)
        );

        View.OnClickListener endClick = v -> openTimePicker(etEndTime);
        etEndTime.setOnClickListener(endClick);
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
                // Utiliser la date du jour si le texte est invalide.
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

    // ---------------------------------------------------------
    // Groupes
    // ---------------------------------------------------------

    private void configureGroupSelector() {
        String[] groups = AttendanceDataMemory.getGroups().toArray(new String[0]);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                groups
        );

        actGroup.setAdapter(adapter);

        actGroup.setOnItemClickListener((parent, itemView, position, id) -> {
            String group = String.valueOf(parent.getItemAtPosition(position));

            if (!group.equals(selectedGroup)) {
                saveCurrentSheetFields();
                selectedGroup = group;
                loadGroup(group);
            }
        });
    }

    private void loadGroup(String groupName) {
        currentSheet = AttendanceRepositoryMemory
                .getInstance()
                .getSheet(sessionId, groupName);

        allRecords.clear();
        filteredRecords.clear();

        if (currentSheet.getRecords() != null) {
            allRecords.addAll(currentSheet.getRecords());
        }

        filteredRecords.addAll(allRecords);
        currentIndex = 0;

        populateSheetFields();
        renderCurrentStudent();
        updateCounters();
        updateNavigationButtons();
    }

    private void populateSheetFields() {
        if (currentSheet == null) {
            return;
        }

        etDate.setText(currentSheet.getDate());
        etStartTime.setText(currentSheet.getStartTime());
        etEndTime.setText(currentSheet.getEndTime());
        etRoom.setText(currentSheet.getRoom());

        actGroup.setText(selectedGroup, false);
        tvGroupStudentCount.setText(
                String.format(
                        Locale.getDefault(),
                        "%d étudiants",
                        allRecords.size()
                )
        );
    }

    // ---------------------------------------------------------
    // Recherche d'étudiants
    // ---------------------------------------------------------

    private void configureStudentSearch() {
        etStudentSearch.addTextChangedListener(new TextWatcher() {
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

                filterStudents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterStudents(String query) {
        filteredRecords.clear();

        String normalizedQuery = query == null
                ? ""
                : query.trim().toLowerCase(Locale.ROOT);

        for (AttendanceRecordMemory record : allRecords) {
            boolean matches =
                    normalizedQuery.isEmpty()
                    || safe(record.getStudentName())
                            .toLowerCase(Locale.ROOT)
                            .contains(normalizedQuery)
                    || safe(record.getMatricule())
                            .toLowerCase(Locale.ROOT)
                            .contains(normalizedQuery)
                    || safe(record.getArabicName())
                            .contains(normalizedQuery);

            if (matches) {
                filteredRecords.add(record);
            }
        }

        currentIndex = 0;

        renderCurrentStudent();
        updateNavigationButtons();
    }

    // ---------------------------------------------------------
    // Étudiant courant et statut
    // ---------------------------------------------------------

    private void renderCurrentStudent() {
        if (filteredRecords.isEmpty()) {
            showNoStudents();
            return;
        }

        cardCurrentStudent.setVisibility(View.VISIBLE);

        AttendanceRecordMemory record = filteredRecords.get(currentIndex);

        String fullName = safe(record.getStudentName());

        tvFullName.setText(fullName);
        tvArabicName.setText(safe(record.getArabicName()));
        tvStudentGroup.setText(safe(record.getGroupName()));
        tvMatricule.setText(safe(record.getMatricule()));

        tvGroupOrder.setText(
                String.valueOf(record.getGroupOrder())
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

        // Éviter de modifier le statut pendant le rafraîchissement.
        rgStatus.setOnCheckedChangeListener(null);

        String status = record.getStatus();

        if (AttendanceRecordMemory.PRESENT.equals(status)) {
            rbPresent.setChecked(true);
        } else if (AttendanceRecordMemory.ABSENT.equals(status)) {
            rbAbsent.setChecked(true);
        } else if (AttendanceRecordMemory.LATE.equals(status)) {
            rbLate.setChecked(true);
        } else {
            rgStatus.clearCheck();
        }

        tvUnmarked.setVisibility(
                status == null
                        || AttendanceRecordMemory.UNMARKED.equals(status)
                        ? View.VISIBLE
                        : View.GONE
        );

        rgStatus.setOnCheckedChangeListener(this::onStatusChanged);
    }

    private void onStatusChanged(RadioGroup group, int checkedId) {
        if (filteredRecords.isEmpty()) {
            return;
        }

        AttendanceRecordMemory record = filteredRecords.get(currentIndex);

        if (checkedId == R.id.rbAttendancePresent) {
            record.setStatus(AttendanceRecordMemory.PRESENT);
        } else if (checkedId == R.id.rbAttendanceAbsent) {
            record.setStatus(AttendanceRecordMemory.ABSENT);
        } else if (checkedId == R.id.rbAttendanceLate) {
            record.setStatus(AttendanceRecordMemory.LATE);
        }

        tvUnmarked.setVisibility(View.GONE);

        updateCounters();
    }

    private void showNoStudents() {
        cardCurrentStudent.setVisibility(View.GONE);
        tvStudentPosition.setText("Aucun étudiant trouvé");
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
    // Navigation
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

        btnPrevious.setEnabled(hasStudents && currentIndex > 0);
        btnNext.setEnabled(
                hasStudents && currentIndex < filteredRecords.size() - 1
        );

        btnPrevious.setAlpha(
                hasStudents && currentIndex > 0 ? 1f : 0.4f
        );

        btnNext.setAlpha(
                hasStudents && currentIndex < filteredRecords.size() - 1
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

        for (AttendanceRecordMemory record : allRecords) {
            String status = record.getStatus();

            if (AttendanceRecordMemory.PRESENT.equals(status)) {
                present++;
                marked++;
            } else if (AttendanceRecordMemory.ABSENT.equals(status)) {
                absent++;
                marked++;
            } else if (AttendanceRecordMemory.LATE.equals(status)) {
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
                        : remaining + " étudiants restent à renseigner"
        );

        tvSaveInfo.setText(
                remaining == 0
                        ? "Tous les étudiants ont un statut."
                        : "Les étudiants sans statut resteront incomplets."
        );
    }

    // ---------------------------------------------------------
    // Enregistrement temporaire
    // ---------------------------------------------------------

    private void configureSaveButton() {
        btnSave.setOnClickListener(v -> saveAttendanceSheet());
    }

    private void saveAttendanceSheet() {
        if (currentSheet == null) {
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

        saveCurrentSheetFields();

        AttendanceRepositoryMemory
                .getInstance()
                .saveSheet(currentSheet);

        Toast.makeText(
                requireContext(),
                "Feuille de présence enregistrée.",
                Toast.LENGTH_SHORT
        ).show();

        // Retour aux détails de la session.
        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();

        updateCounters();
    }

    private void saveCurrentSheetFields() {
        if (currentSheet == null) {
            return;
        }

        currentSheet.setDate(getText(etDate));
        currentSheet.setStartTime(getText(etStartTime));
        currentSheet.setEndTime(getText(etEndTime));
        currentSheet.setRoom(getText(etRoom));
        currentSheet.setGroupName(selectedGroup);
    }

    // ---------------------------------------------------------
    // Utilitaires
    // ---------------------------------------------------------

    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "?";
        }

        String[] parts = fullName.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase(Locale.ROOT);
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

            if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
                return -1;
            }

            return hours * 60 + minutes;

        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
