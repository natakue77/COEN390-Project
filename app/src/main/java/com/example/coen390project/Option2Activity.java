
package com.example.coen390project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

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

public class Option2Activity extends AppCompatActivity {

    private Spinner workoutSpinner;
    private Spinner timelineSpinner;
    private EditText goalEditText;
    private Button saveGoalButton;
    private TextView goalDescriptionTextView;

    private SharedPreferences preferences;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_option2);

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

        Toolbar toolbar = findViewById(R.id.optionToolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Progress & Analytics");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        // Align toolbar title with the hamburger button
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i);

            if (child instanceof TextView) {
                child.setTranslationY(
                        -3 * getResources().getDisplayMetrics().density
                );
                break;
            }
        }

        setupDrawer(toolbar);

        workoutSpinner = findViewById(R.id.workoutSpinner);
        timelineSpinner = findViewById(R.id.timelineSpinner);
        goalEditText = findViewById(R.id.goalEditText);
        saveGoalButton = findViewById(R.id.saveGoalButton);
        goalDescriptionTextView =
                findViewById(R.id.goalDescriptionTextView);

        preferences = getSharedPreferences(
                "WorkoutGoals",
                MODE_PRIVATE
        );

        setupSpinners();
        setupGoalSaving();
    }

    private void setupDrawer(Toolbar toolbar) {

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        if (drawerLayout == null || navigationView == null) {
            Toast.makeText(
                    this,
                    "Navigation drawer views are missing from activity_option2.xml",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView.setCheckedItem(R.id.nav_progress);

        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_progress) {
                drawerLayout.closeDrawers();
                return true;
            }

            Intent intent = null;

            if (id == R.id.nav_home) {
                intent = new Intent(this, MainActivity.class);
            } else if (id == R.id.nav_workouts) {
                intent = new Intent(this, Option1Activity.class);
            } else if (id == R.id.nav_routine) {
                intent = new Intent(this, Option3Activity.class);
            } else if (id == R.id.nav_tips) {
                intent = new Intent(this, Option4Activity.class);
            } else if (id == R.id.nav_settings) {
                intent = new Intent(this, Option5Activity.class);
            }

            if (intent != null) {
                intent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                );

                startActivity(intent);
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void setupSpinners() {

        String[] workouts = {
                "Squats",
                "Push-ups",
                "Bicep Curls"
        };

        String[] timelines = {
                "Day",
                "Week",
                "Month"
        };

        ArrayAdapter<String> workoutAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                workouts
        ) {
            @Override
            public View getDropDownView(
                    int position,
                    View convertView,
                    ViewGroup parent) {

                TextView text = (TextView) super.getDropDownView(
                        position,
                        convertView,
                        parent
                );

                text.setTextColor(Color.WHITE);
                return text;
            }
        };

        workoutAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        workoutSpinner.setAdapter(workoutAdapter);

        ArrayAdapter<String> timelineAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                timelines
        ) {
            @Override
            public View getDropDownView(
                    int position,
                    View convertView,
                    ViewGroup parent) {

                TextView text = (TextView) super.getDropDownView(
                        position,
                        convertView,
                        parent
                );

                text.setTextColor(Color.WHITE);
                return text;
            }
        };

        timelineAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        timelineSpinner.setAdapter(timelineAdapter);

        workoutSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        loadGoal();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );
    }

    private void setupGoalSaving() {

        saveGoalButton.setOnClickListener(v -> {

            String input = goalEditText.getText().toString().trim();

            if (input.isEmpty()) {
                Toast.makeText(
                        this,
                        "Please enter a goal",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            int target;

            try {
                target = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                Toast.makeText(
                        this,
                        "Please enter a valid number",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (target <= 0) {
                Toast.makeText(
                        this,
                        "Goal must be greater than zero",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            String workout = workoutSpinner.getSelectedItem().toString();

            preferences.edit()
                    .putInt(workout, target)
                    .apply();

            loadGoal();

            Toast.makeText(
                    this,
                    "Goal saved successfully",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void loadGoal() {

        if (workoutSpinner.getSelectedItem() == null) {
            return;
        }

        String workout = workoutSpinner.getSelectedItem().toString();

        int target = preferences.getInt(workout, 0);

        if (target > 0) {

            goalEditText.setText(String.valueOf(target));

            goalDescriptionTextView.setText(
                    workout + " goal: " + target + " repetitions"
            );

        } else {

            goalEditText.setText("");

            goalDescriptionTextView.setText(
                    "No goal set for " + workout
            );
        }
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout != null &&
                drawerLayout.isDrawerOpen(GravityCompat.START)) {

            drawerLayout.closeDrawer(GravityCompat.START);

        } else {
            super.onBackPressed();
        }
    }
}
