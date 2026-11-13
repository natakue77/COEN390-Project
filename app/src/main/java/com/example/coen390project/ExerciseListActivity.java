
package com.example.coen390project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class ExerciseListActivity extends AppCompatActivity {

    private LinearLayout exerciseContainer;
    private TextView tvCategoryTitle;

    private Button btnAddSelected;
    private Button btnAddCustomWorkout;

    private boolean routineSelectionMode = false;
    private String currentCategory = "Abdomen";

    private final ArrayList<CheckBox> exerciseCheckBoxes =
            new ArrayList<>();

    private static final String PREF_NAME =
            "CustomWorkoutPreferences";

    private static final String KEY_CATEGORY_PREFIX = "custom_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_list);

        Toolbar toolbar = findViewById(R.id.exerciseToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        routineSelectionMode = getIntent().getBooleanExtra(
                "ROUTINE_SELECTION_MODE", false
        );

        currentCategory = getIntent().getStringExtra("CATEGORY");

        if (currentCategory == null) {
            currentCategory = "Abdomen";
        }

        exerciseContainer = findViewById(R.id.exerciseContainer);
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);

        btnAddSelected = findViewById(R.id.btnAddSelected);
        btnAddCustomWorkout = findViewById(R.id.btnAddCustomWorkout);

        tvCategoryTitle.setText(currentCategory + " Exercises");

        btnAddCustomWorkout.setOnClickListener(v ->
                showAddWorkoutDialog()
        );

        if (routineSelectionMode) {
            btnAddSelected.setVisibility(View.VISIBLE);

            btnAddSelected.setOnClickListener(v ->
                    returnSelectedExercises()
            );
        } else {
            btnAddSelected.setVisibility(View.GONE);
        }

        displayExercises();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayExercises() {

        exerciseContainer.removeAllViews();
        exerciseCheckBoxes.clear();

        String[] exercises;

        switch (currentCategory.toLowerCase()) {

            case "arms":
                exercises = new String[]{
                        "Bicep Curls",
                        "Tricep Extensions",
                        "Hammer Curls",
                        "Shoulder Press",
                        "Lateral Raises"
                };
                break;

            case "legs":
                exercises = new String[]{
                        "Squats",
                        "Lunges",
                        "Leg Press",
                        "Calf Raises",
                        "Glute Bridges"
                };
                break;

            case "chest":
                exercises = new String[]{
                        "Push-Ups",
                        "Bench Press",
                        "Incline Dumbbell Press",
                        "Chest Flyes",
                        "Cable Crossovers"
                };
                break;

            case "back":
                exercises = new String[]{
                        "Pull-Ups",
                        "Lat Pulldowns",
                        "Bent-Over Rows",
                        "Seated Cable Rows",
                        "Deadlifts"
                };
                break;

            default:
                exercises = new String[]{
                        "Crunches",
                        "Plank",
                        "Leg Raises",
                        "Russian Twists",
                        "Mountain Climbers"
                };
                break;
        }

        for (String exercise : exercises) {
            addExerciseCard(exercise, false);
        }

        JSONArray customExercises = getCustomExercises();

        for (int i = 0; i < customExercises.length(); i++) {
            try {
                String exerciseName = customExercises.getString(i);
                addExerciseCard(exerciseName, true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void addExerciseCard(String exerciseName, boolean isCustom) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(12), dp(12), dp(10), dp(12));

        GradientDrawable cardBackground = new GradientDrawable();
        cardBackground.setColor(Color.rgb(32, 35, 42));
        cardBackground.setCornerRadius(dp(14));
        card.setBackground(cardBackground);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);
        card.setMinimumHeight(dp(82));

        // Dumbbell icon background
        LinearLayout iconBackground = new LinearLayout(this);
        iconBackground.setGravity(Gravity.CENTER);

        GradientDrawable iconShape = new GradientDrawable();
        iconShape.setColor(Color.rgb(57, 63, 75));
        iconShape.setCornerRadius(dp(12));

        iconBackground.setBackground(iconShape);

        LinearLayout.LayoutParams iconParams =
                new LinearLayout.LayoutParams(dp(48), dp(48));

        iconParams.setMargins(0, 0, dp(12), 0);
        iconBackground.setLayoutParams(iconParams);

        ImageView dumbbell = new ImageView(this);
        dumbbell.setImageResource(R.drawable.ic_dumbbell);
        dumbbell.setScaleType(ImageView.ScaleType.FIT_CENTER);

        iconBackground.addView(
                dumbbell,
                new LinearLayout.LayoutParams(dp(29), dp(29))
        );

        card.addView(iconBackground);

        // Exercise name and description
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams textParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        textLayout.setLayoutParams(textParams);

        TextView exerciseNameText = new TextView(this);
        exerciseNameText.setText(exerciseName);
        exerciseNameText.setTextColor(Color.WHITE);
        exerciseNameText.setTextSize(15);
        exerciseNameText.setTypeface(null, Typeface.BOLD);

        TextView description = new TextView(this);
        description.setText(
                isCustom ? "Your custom exercise" :
                        currentCategory + " exercise"
        );
        description.setTextColor(Color.rgb(143, 149, 161));
        description.setTextSize(12);

        textLayout.addView(exerciseNameText);
        textLayout.addView(description);

        card.addView(textLayout);

        // Selection checkbox
        if (routineSelectionMode) {

            CheckBox checkBox = new CheckBox(this);

            checkBox.setButtonTintList(
                    ColorStateList.valueOf(Color.WHITE)
            );

            LinearLayout.LayoutParams checkParams =
                    new LinearLayout.LayoutParams(dp(40), dp(48));

            checkBox.setLayoutParams(checkParams);
            checkBox.setTag(exerciseName);

            card.addView(checkBox);
            exerciseCheckBoxes.add(checkBox);

            // Allow tapping the card to select the exercise
            card.setOnClickListener(v ->
                    checkBox.setChecked(!checkBox.isChecked())
            );

        } else if (isCustom) {

            Button removeButton = new Button(this);
            removeButton.setText("×");
            removeButton.setTextColor(
                    Color.rgb(180, 185, 195)
            );
            removeButton.setTextSize(22);
            removeButton.setAllCaps(false);

            removeButton.setBackgroundTintList(
                    ColorStateList.valueOf(Color.TRANSPARENT)
            );

            card.addView(
                    removeButton,
                    new LinearLayout.LayoutParams(dp(42), dp(48))
            );

            removeButton.setOnClickListener(v ->
                    confirmRemoveWorkout(exerciseName)
            );
        }

        exerciseContainer.addView(card);
    }

    private void returnSelectedExercises() {

        ArrayList<String> selectedExercises = new ArrayList<>();

        for (CheckBox checkBox : exerciseCheckBoxes) {

            if (checkBox.isChecked()) {
                selectedExercises.add(checkBox.getTag().toString());
            }
        }

        if (selectedExercises.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please select at least one exercise.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Intent resultIntent = new Intent();

        resultIntent.putStringArrayListExtra(
                "SELECTED_EXERCISES",
                selectedExercises
        );

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    // CUSTOM EXERCISE STORAGE

    private JSONArray getCustomExercises() {

        SharedPreferences preferences =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        String savedExercises = preferences.getString(
                KEY_CATEGORY_PREFIX + currentCategory,
                "[]"
        );

        try {
            return new JSONArray(savedExercises);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    private void saveCustomExercises(JSONArray exercises) {

        SharedPreferences preferences =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        preferences.edit()
                .putString(
                        KEY_CATEGORY_PREFIX + currentCategory,
                        exercises.toString()
                )
                .apply();
    }

    private void showAddWorkoutDialog() {

        EditText input = new EditText(this);
        input.setHint("Enter exercise name");
        input.setSingleLine(true);

        int padding = dp(16);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle("Add Your Own Workout")
                .setMessage("Enter the name of your custom exercise.")
                .setView(input)
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .setPositiveButton("Add", (dialog, which) -> {

                    String name = input.getText()
                            .toString()
                            .trim();

                    if (name.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Please enter an exercise name.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    saveCustomWorkout(name);
                })
                .show();
    }

    private void saveCustomWorkout(String name) {

        JSONArray exercises = getCustomExercises();

        for (int i = 0; i < exercises.length(); i++) {
            if (exercises.optString(i).equalsIgnoreCase(name)) {
                Toast.makeText(
                        this,
                        "This exercise already exists.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }
        }

        exercises.put(name);
        saveCustomExercises(exercises);

        displayExercises();

        Toast.makeText(
                this,
                "Custom exercise added!",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void confirmRemoveWorkout(String name) {

        new AlertDialog.Builder(this)
                .setTitle("Remove Exercise")
                .setMessage("Are you sure you want to remove " + name + "?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (dialog, which) ->
                        removeCustomWorkout(name)
                )
                .show();
    }

    private void removeCustomWorkout(String name) {

        JSONArray oldExercises = getCustomExercises();
        JSONArray newExercises = new JSONArray();

        for (int i = 0; i < oldExercises.length(); i++) {

            String exercise = oldExercises.optString(i);

            if (!exercise.equals(name)) {
                newExercises.put(exercise);
            }
        }

        saveCustomExercises(newExercises);
        displayExercises();

        Toast.makeText(
                this,
                "Exercise removed.",
                Toast.LENGTH_SHORT
        ).show();
    }

    private int dp(int value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }
}
