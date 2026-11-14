
package com.example.coen390project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.graphics.Color;

import androidx.annotation.NonNull;
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

public class Option4Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Spinner goalSpinner;
    private TextView tipsTextView;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_option4);

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

        goalSpinner = findViewById(R.id.goalSpinner);
        tipsTextView = findViewById(R.id.tipsTextView);

        String[] goals = {
                "Build Muscle",
                "Lose Weight",
                "Improve Endurance",
                "General Fitness"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                goals
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

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        goalSpinner.setAdapter(adapter);

        SharedPreferences preferences = getSharedPreferences(
                "FitnessPreferences",
                MODE_PRIVATE
        );

        int savedGoal = preferences.getInt("selectedGoal", 0);

        if (savedGoal < 0 || savedGoal >= goals.length) {
            savedGoal = 0;
        }

        goalSpinner.setSelection(savedGoal);

        goalSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        preferences.edit()
                                .putInt("selectedGoal", position)
                                .apply();

                        displayTips(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );
    }

    private void setupToolbar() {

        Toolbar toolbar = findViewById(R.id.optionToolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tips and Advice");
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
        navigationView.setCheckedItem(R.id.nav_tips);
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
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;

        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, Option5Activity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void displayTips(int position) {

        if (position == 0) {

            tipsTextView.setText(
                    "BUILD MUSCLE\n\n" +
                            "1. Focus on proper exercise form.\n\n" +
                            "2. Gradually increase weights or repetitions.\n\n" +
                            "3. Allow your muscles time to recover.\n\n" +
                            "4. Eat enough protein to support muscle growth."
            );

        } else if (position == 1) {

            tipsTextView.setText(
                    "LOSE WEIGHT\n\n" +
                            "1. Combine strength training with cardio.\n\n" +
                            "2. Stay consistent with your workouts.\n\n" +
                            "3. Focus on balanced eating habits.\n\n" +
                            "4. Get enough sleep and recovery."
            );

        } else if (position == 2) {

            tipsTextView.setText(
                    "IMPROVE ENDURANCE\n\n" +
                            "1. Include regular cardio exercises.\n\n" +
                            "2. Gradually increase workout duration.\n\n" +
                            "3. Take recovery days when needed.\n\n" +
                            "4. Track your progress over time."
            );

        } else if (position == 3) {

            tipsTextView.setText(
                    "GENERAL FITNESS\n\n" +
                            "1. Stay physically active regularly.\n\n" +
                            "2. Include both strength and cardio exercises.\n\n" +
                            "3. Warm up before exercising.\n\n" +
                            "4. Maintain a consistent workout routine."
            );
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
