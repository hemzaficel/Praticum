package com.example.praticum.core.common;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.praticum.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Dialogue partagé pour confirmer l’abandon d’un formulaire. */
public final class FormExitConfirmation {

    private FormExitConfirmation() {
    }

    public static void show(Fragment fragment, Runnable onConfirm) {
        if (fragment == null || !fragment.isAdded()) {
            return;
        }

        View dialogView = LayoutInflater.from(fragment.requireContext())
                .inflate(R.layout.dialog_confirmation, null);

        TextView title = dialogView.findViewById(R.id.tvConfirmationTitle);
        TextView message = dialogView.findViewById(R.id.tvConfirmationMessage);
        MaterialButton cancel = dialogView.findViewById(R.id.btnConfirmationCancel);
        MaterialButton confirm = dialogView.findViewById(R.id.btnConfirmationConfirm);

        title.setText("Abandonner le formulaire ?");
        message.setText("Les informations saisies ne seront pas conservées.");
        cancel.setText("Continuer");
        confirm.setText("Quitter");

        AlertDialog dialog = new MaterialAlertDialogBuilder(fragment.requireContext())
                .setView(dialogView)
                .create();

        cancel.setOnClickListener(v -> dialog.dismiss());
        confirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (onConfirm != null && fragment.isAdded()) {
                onConfirm.run();
            }
        });

        dialog.show();
    }
}
