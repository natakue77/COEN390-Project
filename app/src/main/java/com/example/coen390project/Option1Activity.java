
package com.example.coen390project;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class Option1Activity extends AppCompatActivity {

    private static final int REQUEST_EXERCISES = 100;

    private LinearLayout cardAbs, cardArms, cardLegs, cardChest, cardBack;

    private boolean routineSelectionMode;

    private final LinkedHashSet<String> selectedExercises =
            new LinkedHashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option1);

        Toolbar toolbar = findViewById(R.id.optionToolbar);
        setSupportActionBar(toolbar);

        routineSelectionMode = getIntent().getBooleanExtra(
                "ROUTINE_SELECTION_MODE", false
        );

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Workouts");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        cardAbs = findViewById(R.id.cardAbs);
        cardArms = findViewById(R.id.cardArms);
        cardLegs = findViewById(R.id.cardLegs);
        cardChest = findViewById(R.id.cardChest);
        cardBack = findViewById(R.id.cardBack);

        cardAbs.setOnClickListener(v -> openExerciseList("Abdomen"));
        cardArms.setOnClickListener(v -> openExerciseList("Arms"));
        cardLegs.setOnClickListener(v -> openExerciseList("Legs"));
        cardChest.setOnClickListener(v -> openExerciseList("Chest"));
        cardBack.setOnClickListener(v -> openExerciseList("Back"));
    }

    // Show Done only when selecting exercises for My Routine
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

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Open the exercise list for the selected category
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

    // Receive selected exercises and return to Workouts
    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EXERCISES
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

    // Return all selected exercises to My Routine
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
}
