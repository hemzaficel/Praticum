package com.example.praticum.ui.enrollments;

import android.content.Context;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;

import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.example.praticum.data.importer.EnrollmentExcelImporter;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.*;

public class EnrollmentImportFragment extends Fragment {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private MaterialButton choose;
    private ProgressBar bar;
    private TextView status;
    private final ActivityResultLauncher<String[]> picker = registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::selected);

    public EnrollmentImportFragment() {
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle b) {
        return i.inflate(R.layout.fragment_enrollment_import, c, false);
    }

    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        CommonHeaderHelper.configurerEnteteSousFenetre(v, "Importer des inscriptions", "Fichier Excel .xlsx", requireActivity());
        choose = v.findViewById(R.id.btnChooseEnrollmentExcel);
        bar = v.findViewById(R.id.progressEnrollmentImport);
        status = v.findViewById(R.id.tvEnrollmentImportStatus);
        choose.setOnClickListener(x -> picker.launch(new String[]{"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/octet-stream"}));
    }

    private void selected(Uri uri) {
        if (uri == null) return;
        Context appContext = requireContext().getApplicationContext();
        running(true, "Lecture du fichier…");
        executor.execute(() -> {
            try {
                EnrollmentExcelImporter.Result r = EnrollmentExcelImporter.importFile(appContext, uri, (d, t, m) -> main.post(() -> {
                    if (isAdded()) {
                        bar.setMax(Math.max(1, t));
                        bar.setProgress(d);
                        status.setText(m);
                    }
                }));
                main.post(() -> {
                    if (!isAdded()) return;
                    StringBuilder m = new StringBuilder("Import terminé.\nInscriptions créées : ").append(r.enrollments).append("\nNouveaux étudiants : ").append(r.created).append("\nÉtudiants existants réutilisés : ").append(r.reused).append("\nDéjà inscrits (ignorés) : ").append(r.duplicates).append("\nLignes en erreur : ").append(r.errorCount);
                    for (String e : r.errors) m.append("\n").append(e);
                    if (r.errorCount > r.errors.size())
                        m.append("\n… autres erreurs non affichées.");
                    running(false, m.toString());
                });
            } catch (Exception e) {
                main.post(() -> {
                    if (isAdded())
                        running(false, e.getMessage() == null ? "Échec de l’importation." : e.getMessage());
                });
            }
        });
    }

    private void running(boolean yes, String msg) {
        if (choose == null) return;
        choose.setEnabled(!yes);
        bar.setVisibility(yes ? View.VISIBLE : View.GONE);
        if (yes) {
            bar.setIndeterminate(false);
            bar.setMax(1);
            bar.setProgress(0);
        }
        status.setText(msg);
    }

    public void onDestroyView() {
        choose = null;
        bar = null;
        status = null;
        super.onDestroyView();
    }

    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}