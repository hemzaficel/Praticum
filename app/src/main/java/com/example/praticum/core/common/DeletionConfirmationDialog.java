package com.example.praticum.core.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.praticum.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Dialogue de confirmation cohérent pour les suppressions. */
public final class DeletionConfirmationDialog {

    private DeletionConfirmationDialog() {
    }

    public static void show(
            Context context,
            String titleText,
            String messageText,
            Runnable onConfirm
    ) {
        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_confirmation_2, null);

        TextView title = dialogView.findViewById(R.id.tvConfirmationTitle);
        TextView message = dialogView.findViewById(R.id.tvConfirmationMessage);
        MaterialButton cancel = dialogView.findViewById(R.id.btnConfirmationCancel);
        MaterialButton action = dialogView.findViewById(R.id.btnConfirmationAction);

        title.setText(titleText);
        message.setText(messageText);
        cancel.setText("Annuler");
        action.setText("Supprimer");

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(dialogView)
                .create();

        cancel.setOnClickListener(v -> dialog.dismiss());
        action.setOnClickListener(v -> {
            dialog.dismiss();
            if (onConfirm != null) {
                onConfirm.run();
            }
        });

        dialog.show();
    }
}
