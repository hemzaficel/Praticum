package com.example.praticum;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.praticum.ui.about.AboutFragment;
import com.example.praticum.ui.academicyears.AcademicYearsFragment;
import com.example.praticum.ui.groups.GroupsFragment;
import com.example.praticum.ui.home.HomeFragment;
import com.example.praticum.ui.levels.LevelsFragment;
import com.example.praticum.ui.modules.ModulesFragment;
import com.example.praticum.ui.reports.ReportsFragment;
import com.example.praticum.ui.sessions.SessionsFragment;
import com.example.praticum.ui.settings.SettingsFragment;
import com.example.praticum.ui.students.StudentsFragment;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigation;

    // AJOUT : hauteur de la barre système du bas (barre de gestes)
    private int insetBas = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(
                        getWindow(),
                        getWindow().getDecorView()
                );

        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(true);

        setContentView(R.layout.activity_main);

        initialiserVues();

        // MODIF : mémorise l'inset du bas et met à jour le padding
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.contentRoot),
                (v, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.displayCutout());

                    insetBas = bars.bottom;
                    v.setPadding(bars.left, bars.top, bars.right, 0);
                    mettreAJourPaddingBas();

                    return WindowInsetsCompat.CONSUMED;
                }
        );

        configurerNavigation();
        configurerBoutonRetour();

        getSupportFragmentManager().addOnBackStackChangedListener(
                this::mettreAJourNavigationInferieure
        );

        if (savedInstanceState == null) {
            afficherDestination(R.id.nav_dashboard);
        } else {
            mettreAJourNavigationInferieure();
        }
    }

    private void initialiserVues() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void configurerNavigation() {
        navigationView.setNavigationItemSelectedListener(this);

        bottomNavigation.setOnItemSelectedListener(
                item -> afficherDestination(item.getItemId())
        );
    }

    public void ouvrirMenuLateral() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    public void ouvrirEcranSecondaire(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        boolean handled = afficherDestination(item.getItemId());

        drawerLayout.closeDrawer(GravityCompat.START);

        return handled;
    }

    private boolean afficherDestination(int menuItemId) {
        Fragment fragment;

        if (menuItemId == R.id.nav_dashboard) {
            fragment = new HomeFragment();

        } else if (menuItemId == R.id.nav_students) {
            fragment = new StudentsFragment();

        } else if (menuItemId == R.id.nav_groups) {
            fragment = new GroupsFragment();

        } else if (menuItemId == R.id.nav_sessions) {
            fragment = new SessionsFragment();

        } else if (menuItemId == R.id.nav_reports) {
            fragment = new ReportsFragment();

        } else if (menuItemId == R.id.nav_settings) {
            fragment = new SettingsFragment();

        }else if (menuItemId == R.id.nav_levels) {
            fragment = new LevelsFragment();

        }else if (menuItemId == R.id.nav_academic_years) {
            fragment = new AcademicYearsFragment();

        }else if (menuItemId == R.id.nav_about) {
            fragment = new AboutFragment();

        }else if (menuItemId == R.id.nav_modules) {
            fragment = new ModulesFragment();

        }

        else {
            Toast.makeText(
                    this,
                    "Écran non disponible pour le moment",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        getSupportFragmentManager().popBackStackImmediate(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
        );

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainContainer, fragment)
                .commit();

        MenuItem drawerItem =
                navigationView.getMenu().findItem(menuItemId);

        if (drawerItem != null) {
            navigationView.setCheckedItem(menuItemId);
        }

        MenuItem bottomItem =
                bottomNavigation.getMenu().findItem(menuItemId);

        if (bottomItem != null) {
            bottomItem.setChecked(true);
        }

        mettreAJourNavigationInferieure();

        return true;
    }

    private void mettreAJourNavigationInferieure() {
        boolean ecranPrincipal =
                getSupportFragmentManager().getBackStackEntryCount() == 0;

        bottomNavigation.setVisibility(
                ecranPrincipal ? View.VISIBLE : View.GONE
        );

        mettreAJourPaddingBas();   // AJOUT
    }

    // AJOUT : gère le padding du bas selon la visibilité de la bottom nav
    private void mettreAJourPaddingBas() {
        boolean navVisible = bottomNavigation.getVisibility() == View.VISIBLE;

        // Bottom nav visible : elle gère elle-même la zone des gestes
        bottomNavigation.setPadding(0, 0, 0, navVisible ? insetBas : 0);

        // Bottom nav cachée : le conteneur remonte au-dessus de la barre de gestes
        findViewById(R.id.mainContainer)
                .setPadding(0, 0, 0, navVisible ? 0 : insetBas);
    }

    private void configurerBoutonRetour() {
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (drawerLayout.isDrawerOpen(
                                GravityCompat.START)) {

                            drawerLayout.closeDrawer(
                                    GravityCompat.START
                            );

                        } else if (
                                getSupportFragmentManager()
                                        .getBackStackEntryCount() > 0) {

                            getSupportFragmentManager().popBackStack();

                        } else {
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                }
        );
    }

    public void retournerAuTableauDeBord() {
        afficherDestination(R.id.nav_dashboard);
    }
}