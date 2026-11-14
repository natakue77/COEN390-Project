
package com.example.coen390project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class Option5Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText ageEditText;
    private EditText weightEditText;

    private RadioButton lightModeRadio;
    private RadioButton darkModeRadio;

    private Button saveSettingsButton;

    private SharedPreferences preferences;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_option5);

        View mainContent = findViewById(R.id.mainContent);

        int originalTopPadding = mainContent.getPaddingTop();

        ViewCompat.setOnApplyWindowInsetsListener(mainContent, (view, windowInsets) -> {

            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.statusBars()
                            | WindowInsetsCompat.Type.displayCutout()
            );

            view.setPadding(
                    view.getPaddingLeft(),
                    originalTopPadding + insets.top,
                    view.getPaddingRight(),
                    view.getPaddingBottom()
            );

            return windowInsets;
        });

        ViewCompat.requestApplyInsets(mainContent);

        setupToolbar();
        setupNavigation();

        // Connect Java to XML
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        ageEditText = findViewById(R.id.ageEditText);
        weightEditText = findViewById(R.id.weightEditText);

        lightModeRadio = findViewById(R.id.lightModeRadio);
        darkModeRadio = findViewById(R.id.darkModeRadio);

        saveSettingsButton = findViewById(R.id.saveSettingsButton);

        // Access saved settings
        preferences = getSharedPreferences(
                "UserSettings",
                MODE_PRIVATE
        );

        // Load previously saved information
        nameEditText.setText(preferences.getString("name", ""));
        emailEditText.setText(preferences.getString("email", ""));
        ageEditText.setText(preferences.getString("age", ""));
        weightEditText.setText(preferences.getString("weight", ""));

        // Load saved appearance
        boolean darkMode = preferences.getBoolean("darkMode", false);

        if (darkMode) {
            darkModeRadio.setChecked(true);
        } else {
            lightModeRadio.setChecked(true);
        }

        // Save button
        saveSettingsButton.setOnClickListener(v -> saveSettings());
    }

    private void setupToolbar() {

        Toolbar toolbar = findViewById(R.id.optionToolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
        }

        // Align toolbar title with hamburger button
        for (int i = 0; i < toolbar.getChildCount(); i++) {

            View child = toolbar.getChildAt(i);

            if (child instanceof TextView) {
                child.setTranslationY(
                        -3 * getResources().getDisplayMetrics().density
                );
                break;
            }
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupNavigation() {
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_settings);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(this, MainActivity.class));

        } else if (id == R.id.nav_workouts) {
            startActivity(new Intent(this, Option1Activity.class));

        } else if (id == R.id.nav_progress) {
            startActivity(new Intent(this, Option2Activity.class));

        } else if (id == R.id.nav_routine) {
            startActivity(new Intent(this, Option3Activity.class));

        } else if (id == R.id.nav_tips) {
            startActivity(new Intent(this, Option4Activity.class));

        } else if (id == R.id.nav_settings) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void saveSettings() {

        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String age = ageEditText.getText().toString().trim();
        String weight = weightEditText.getText().toString().trim();

        boolean useDarkMode = darkModeRadio.isChecked();

        // Save information locally
        preferences.edit()
                .putString("name", name)
                .putString("email", email)
                .putString("age", age)
                .putString("weight", weight)
                .putBoolean("darkMode", useDarkMode)
                .apply();

        Toast.makeText(
                this,
                "Settings saved successfully",
                Toast.LENGTH_SHORT
        ).show();

        // Apply selected theme
        int mode = useDarkMode
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO;

        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            AppCompatDelegate.setDefaultNightMode(mode);
        }
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout != null
                && drawerLayout.isDrawerOpen(GravityCompat.START)) {

            drawerLayout.closeDrawer(GravityCompat.START);

        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {

        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }

        return true;
    }
}
