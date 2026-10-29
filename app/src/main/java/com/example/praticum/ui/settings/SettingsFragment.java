
package com.example.praticum.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.praticum.R;
import com.example.praticum.core.common.CommonHeaderHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "praticum_settings";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";

    private SwitchMaterial switchLightMode;
    private TextView tvAppearanceDescription;

    private SharedPreferences preferences;

    public SettingsFragment() {
        // Constructeur requis par Android.
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_settings,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        // Initialiser les préférences.
        preferences = requireContext().getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );

        // Configurer le titre, le sous-titre et le bouton Retour.
        CommonHeaderHelper.configurerEnteteSousFenetre(
                view,
                "Paramètres",
                "Personnalisez votre application",
                requireActivity()
        );

        // Récupérer les vues du mode d'affichage.
        switchLightMode = view.findViewById(R.id.switchLightMode);

        tvAppearanceDescription = view.findViewById(
                R.id.tvSettingsAppearanceDescription
        );

        // Initialiser le mode d'affichage.
        initialiserModeAffichage();
    }

    /**
     * Initialise le switch selon le mode enregistré.
     */
    private void initialiserModeAffichage() {

        boolean darkModeEnabled = preferences.getBoolean(
                KEY_DARK_MODE,
                false
        );

        // Le switch représente le mode clair.
        switchLightMode.setChecked(!darkModeEnabled);

        mettreAJourDescription(!darkModeEnabled);

        switchLightMode.setOnCheckedChangeListener(
                (buttonView, isLightMode) -> {

                    boolean darkMode = !isLightMode;

                    // Enregistrer le choix de l'utilisateur.
                    preferences.edit()
                            .putBoolean(KEY_DARK_MODE, darkMode)
                            .apply();

                    // Actualiser le texte descriptif.
                    mettreAJourDescription(isLightMode);

                    // Appliquer le thème à toute l'application.
                    AppCompatDelegate.setDefaultNightMode(
                            darkMode
                                    ? AppCompatDelegate.MODE_NIGHT_YES
                                    : AppCompatDelegate.MODE_NIGHT_NO
                    );
                }
        );
    }

    /**
     * Actualise la description du mode d'affichage.
     */
    private void mettreAJourDescription(boolean isLightMode) {

        if (tvAppearanceDescription != null) {
            tvAppearanceDescription.setText(
                    isLightMode ? "Mode clair" : "Mode sombre"
            );
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Libérer les références aux vues détruites.
        switchLightMode = null;
        tvAppearanceDescription = null;
    }
}
