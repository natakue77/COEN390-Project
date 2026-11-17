
package com.example.coen390project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
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

    private RadioGroup weightUnitRadioGroup;
    private RadioButton kgRadioButton;
    private RadioButton lbRadioButton;

    private Button saveSettingsButton;

    private SharedPreferences preferences;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private static final double KG_TO_LB = 2.2046226218;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_option5);

        View mainContent = findViewById(R.id.mainContent);

        int originalTopPadding = mainContent.getPaddingTop();

        ViewCompat.setOnApplyWindowInsetsListener(
                mainContent,
                (view, windowInsets) -> {

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
                }
        );

        ViewCompat.requestApplyInsets(mainContent);

        preferences = getSharedPreferences(
                "UserSettings",
                MODE_PRIVATE
        );

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        ageEditText = findViewById(R.id.ageEditText);
        weightEditText = findViewById(R.id.weightEditText);

        weightUnitRadioGroup = findViewById(R.id.weightUnitRadioGroup);
        kgRadioButton = findViewById(R.id.kgRadioButton);
        lbRadioButton = findViewById(R.id.lbRadioButton);

        saveSettingsButton = findViewById(R.id.saveSettingsButton);

        setupToolbar();
        setupNavigation();
        loadSettings();

        saveSettingsButton.setOnClickListener(v -> saveSettings());
    }

    // =================================================
    // TOOLBAR
    // =================================================

    private void setupToolbar() {

        Toolbar toolbar = findViewById(R.id.optionToolbar);

        setSupportActionBar(toolbar);

        TextView toolbarTitle = null;

        for (int i = 0; i < toolbar.getChildCount(); i++) {

            View child = toolbar.getChildAt(i);

            if (child instanceof TextView) {
                toolbarTitle = (TextView) child;
                break;
            }
        }

        if (toolbarTitle != null) {
            toolbarTitle.setTranslationY(
                    -3 * getResources().getDisplayMetrics().density
            );
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
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

    // =================================================
    // NAVIGATION
    // =================================================

    private void setupNavigation() {

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_settings);
    }

    @Override
    public boolean onNavigationItemSelected(
            android.view.MenuItem item
    ) {

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

    // =================================================
    // LOAD SETTINGS
    // =================================================

    private void loadSettings() {

        nameEditText.setText(
                preferences.getString("name", "")
        );

        emailEditText.setText(
                preferences.getString("email", "")
        );

        ageEditText.setText(
                preferences.getString("age", "")
        );

        weightEditText.setText(
                preferences.getString("weight", "")
        );

        String unit = preferences.getString("weightUnit", "KG");

        if (unit.equalsIgnoreCase("LB")) {
            lbRadioButton.setChecked(true);
            weightEditText.setHint("Weight (lb)");
        } else {
            kgRadioButton.setChecked(true);
            weightEditText.setHint("Weight (kg)");
        }

        weightUnitRadioGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    if (checkedId == R.id.kgRadioButton) {
                        weightEditText.setHint("Weight (kg)");
                    } else if (checkedId == R.id.lbRadioButton) {
                        weightEditText.setHint("Weight (lb)");
                    }
                }
        );
    }

    // =================================================
    // SAVE SETTINGS
    // =================================================

    private void saveSettings() {

        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String age = ageEditText.getText().toString().trim();
        String weight = weightEditText.getText().toString().trim();

        String oldUnit = preferences.getString("weightUnit", "KG");

        String newUnit = lbRadioButton.isChecked() ? "LB" : "KG";

        if (!weight.isEmpty()) {

            try {

                double parsedWeight = Double.parseDouble(weight);

                if (parsedWeight <= 0) {
                    throw new NumberFormatException();
                }

            } catch (NumberFormatException e) {

                weightEditText.setError("Enter a valid weight");
                weightEditText.requestFocus();
                return;
            }
        }

        /*
         * Convert the profile body weight when the selected unit changes.
         * Dashboard exercise weights are NOT converted or modified here.
         */

        if (!oldUnit.equalsIgnoreCase(newUnit) && !weight.isEmpty()) {

            double value = Double.parseDouble(weight);

            value = convertWeight(value, oldUnit, newUnit);

            weight = formatWeight(value);

            weightEditText.setText(weight);
        }

        preferences.edit()
                .putString("name", name)
                .putString("email", email)
                .putString("age", age)
                .putString("weight", weight)
                .putString("weightUnit", newUnit)
                .apply();

        weightEditText.setHint(
                newUnit.equals("LB") ? "Weight (lb)" : "Weight (kg)"
        );

        Toast.makeText(
                this,
                "Settings saved successfully!",
                Toast.LENGTH_SHORT
        ).show();
    }

    // =================================================
    // PROFILE WEIGHT CONVERSION
    // =================================================

    private double convertWeight(
            double weight,
            String oldUnit,
            String newUnit
    ) {

        if (oldUnit.equalsIgnoreCase(newUnit)) {
            return weight;
        }

        if (oldUnit.equalsIgnoreCase("KG")
                && newUnit.equalsIgnoreCase("LB")) {

            return weight * KG_TO_LB;
        }

        if (oldUnit.equalsIgnoreCase("LB")
                && newUnit.equalsIgnoreCase("KG")) {

            return weight / KG_TO_LB;
        }

        return weight;
    }

    private String formatWeight(double weight) {
        return String.valueOf((long) Math.ceil(weight));
    }

    // =================================================
    // BACK BUTTON
    // =================================================

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
