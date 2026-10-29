
package com.example.praticum.core.common;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.praticum.MainActivity;
import com.example.praticum.R;

public final class CommonHeaderHelper {

    private CommonHeaderHelper() {
        // Classe utilitaire : aucune instance nécessaire.
    }

    /**
     * Configure un en-tête générique.
     */
    public static void configurerEntete(
            View rootView,
            @IdRes int idTitre,
            @IdRes int idSousTitre,
            String titre,
            @Nullable String sousTitre
    ) {
        if (rootView == null) return;

        configurerTextes(
                rootView,
                idTitre,
                idSousTitre,
                titre,
                sousTitre
        );
    }

    /**
     * Configure un en-tête avec un bouton d'action personnalisé.
     */
    public static void configurerEnteteAvecAction(
            View rootView,
            @IdRes int idTitre,
            @IdRes int idSousTitre,
            String titre,
            @Nullable String sousTitre,
            @IdRes int idBouton,
            @Nullable View.OnClickListener action
    ) {
        if (rootView == null) return;

        configurerEntete(
                rootView,
                idTitre,
                idSousTitre,
                titre,
                sousTitre
        );

        View bouton = rootView.findViewById(idBouton);

        if (bouton != null && action != null) {
            bouton.setOnClickListener(action);
        }
    }

    /**
     * Configure un en-tête principal avec son bouton de menu.
     */
    public static void configurerEntetePrincipal(
            View rootView,
            String titre,
            @Nullable String sousTitre,
            @Nullable View.OnClickListener actionMenu
    ) {
        configurerEnteteAvecAction(
                rootView,
                R.id.tvCommonHeaderTitle,
                R.id.tvCommonHeaderSubtitle,
                titre,
                sousTitre,
                R.id.btnCommonMenu,
                actionMenu
        );
    }

    /**
     * Configure un en-tête de sous-écran avec un bouton Retour.
     */
    public static void configurerEnteteSousFenetre(
            View rootView,
            String titre,
            @Nullable String sousTitre,
            FragmentActivity activity
    ) {
        configurerEnteteAvecAction(
                rootView,
                R.id.tvSubscreenHeaderTitle,
                R.id.tvSubscreenHeaderSubtitle,
                titre,
                sousTitre,
                R.id.btnSubscreenHeaderBack,
                v -> gererRetour(activity)
        );
    }

    /**
     * Gère le bouton Retour selon la pile de navigation.
     */
    private static void gererRetour(
            @Nullable FragmentActivity activity
    ) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        FragmentManager fragmentManager =
                activity.getSupportFragmentManager();

        if (fragmentManager.isStateSaved()) {
            return;
        }

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            return;
        }

        // Si la pile est vide, revenir au tableau de bord.
        if (activity instanceof MainActivity) {
            ((MainActivity) activity)
                    .retournerAuTableauDeBord();
        } else {
            activity.finish();
        }
    }

    /**
     * Configure les textes de n'importe quel en-tête.
     */
    private static void configurerTextes(
            View rootView,
            @IdRes int idTitre,
            @IdRes int idSousTitre,
            String titre,
            @Nullable String sousTitre
    ) {
        TextView tvTitre = rootView.findViewById(idTitre);
        TextView tvSousTitre = rootView.findViewById(idSousTitre);

        if (tvTitre != null) {
            tvTitre.setText(titre != null ? titre : "");
        }

        if (tvSousTitre != null) {
            if (sousTitre != null && !sousTitre.trim().isEmpty()) {
                tvSousTitre.setText(sousTitre);
                tvSousTitre.setVisibility(View.VISIBLE);
            } else {
                tvSousTitre.setText("");
                tvSousTitre.setVisibility(View.GONE);
            }
        }
    }
}
