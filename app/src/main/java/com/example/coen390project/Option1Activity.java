
package com.example.coen390project;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class Option1Activity extends AppCompatActivity {

    private static final int REQUEST_EXERCISES = 100;
    private static final int REQUEST_CUSTOM_WORKOUTS = 101;

    // Body-part cards
    private LinearLayout cardAbs;
    private LinearLayout cardBack;
    private LinearLayout cardChest;
    private LinearLayout cardShoulders;
    private LinearLayout cardLegs;
    private LinearLayout cardForearm;
    private LinearLayout cardBiceps;
    private LinearLayout cardTriceps;

    // Custom workouts card
    private LinearLayout cardMyOwnWorkouts;

    private boolean routineSelectionMode;

    private final LinkedHashSet<String> selectedExercises =
            new LinkedHashSet<>();

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_option1);

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

        routineSelectionMode = getIntent().getBooleanExtra(
                "ROUTINE_SELECTION_MODE",
                false
        );

        Toolbar toolbar = findViewById(R.id.optionToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Workouts");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        setupDrawer(toolbar);

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

        // Connect all nine cards to their XML IDs
        cardAbs = findViewById(R.id.cardAbs);
        cardBack = findViewById(R.id.cardBack);
        cardChest = findViewById(R.id.cardChest);
        cardShoulders = findViewById(R.id.cardShoulders);
        cardLegs = findViewById(R.id.cardLegs);
        cardForearm = findViewById(R.id.cardForearm);
        cardBiceps = findViewById(R.id.cardBiceps);
        cardTriceps = findViewById(R.id.cardTriceps);

        cardMyOwnWorkouts = findViewById(R.id.cardMyOwnWorkouts);

        // Body-part card click listeners
        cardAbs.setOnClickListener(v ->
                openExerciseList("Abdomen")
        );

        cardBack.setOnClickListener(v ->
                openExerciseList("Back")
        );

        cardChest.setOnClickListener(v ->
                openExerciseList("Chest")
        );

        cardShoulders.setOnClickListener(v ->
                openExerciseList("Shoulders")
        );

        cardLegs.setOnClickListener(v ->
                openExerciseList("Legs")
        );

        cardForearm.setOnClickListener(v ->
                openExerciseList("Forearms")
        );

        cardBiceps.setOnClickListener(v ->
                openExerciseList("Biceps")
        );

        cardTriceps.setOnClickListener(v ->
                openExerciseList("Triceps")
        );

        // My Own Workouts
        cardMyOwnWorkouts.setOnClickListener(v ->
                openMyOwnWorkouts()
        );
    }

    private void setupDrawer(Toolbar toolbar) {

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        if (drawerLayout == null || navigationView == null) {
            Toast.makeText(
                    this,
                    "Navigation drawer views are missing from activity_option1.xml",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        // Hide navigation while selecting exercises for a routine
        if (routineSelectionMode) {
            navigationView.setVisibility(View.GONE);
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

        navigationView.setCheckedItem(R.id.nav_workouts);

        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_workouts) {
                drawerLayout.closeDrawers();
                return true;
            }

            Intent intent = null;

            if (id == R.id.nav_home) {
                intent = new Intent(this, MainActivity.class);

            } else if (id == R.id.nav_progress) {
                intent = new Intent(this, Option2Activity.class);

            } else if (id == R.id.nav_routine) {
                intent = new Intent(this, Option3Activity.class);

            } else if (id == R.id.nav_tips) {
                intent = new Intent(this, Option4Activity.class);

            } else if (id == R.id.nav_settings) {
                intent = new Intent(this, Option5Activity.class);
            }

            if (intent != null) {
                intent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                );

                startActivity(intent);
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (routineSelectionMode) {
            getMenuInflater().inflate(R.menu.workout_menu, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_done) {
            finishWorkoutSelection();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openExerciseList(String category) {

        Intent intent = new Intent(
                Option1Activity.this,
                ExerciseListActivity.class
        );

        intent.putExtra("CATEGORY", category);
        intent.putExtra("ROUTINE_SELECTION_MODE", routineSelectionMode);

        if (routineSelectionMode) {
            startActivityForResult(intent, REQUEST_EXERCISES);
        } else {
            startActivity(intent);
        }
    }

    private void openMyOwnWorkouts() {

        Intent intent = new Intent(
                Option1Activity.this,
                MyOwnWorkouts.class
        );

        intent.putExtra("ROUTINE_SELECTION_MODE", routineSelectionMode);

        if (routineSelectionMode) {
            startActivityForResult(intent, REQUEST_CUSTOM_WORKOUTS);
        } else {
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == REQUEST_EXERCISES
                || requestCode == REQUEST_CUSTOM_WORKOUTS)
                && resultCode == RESULT_OK
                && data != null) {

            ArrayList<String> exercises =
                    data.getStringArrayListExtra("SELECTED_EXERCISES");

            if (exercises != null) {
                selectedExercises.addAll(exercises);

                Toast.makeText(
                        this,
                        exercises.size() + " exercise(s) added.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private void finishWorkoutSelection() {

        if (selectedExercises.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please add at least one exercise.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Intent result = new Intent();

        result.putStringArrayListExtra(
                "SELECTED_EXERCISES",
                new ArrayList<>(selectedExercises)
        );

        setResult(RESULT_OK, result);
        finish();
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
