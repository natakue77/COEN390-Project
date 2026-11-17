
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class ExerciseListActivity extends AppCompatActivity {

    private LinearLayout exerciseContainer;
    private TextView tvCategoryTitle;

    private Button btnAddSelected;
    private Button btnAddCustomWorkout;

    private boolean routineSelectionMode = false;
    private boolean progressSelectionMode = false;

    private String currentCategory = "Abdomen";

    private final ArrayList<CheckBox> exerciseCheckBoxes =
            new ArrayList<>();

    // Every exercise displayed in the current category.
    private final ArrayList<String> displayedExerciseNames =
            new ArrayList<>();

    // Exercises selected before opening this category.
    private final ArrayList<String> selectedExercisesBefore =
            new ArrayList<>();

    private static final String PREF_NAME =
            "CustomWorkoutPreferences";

    private static final String KEY_CATEGORY_PREFIX =
            "custom_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_exercise_list);

        Toolbar toolbar = findViewById(R.id.exerciseToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                toolbar,
                (view, windowInsets) -> {

                    Insets insets = windowInsets.getInsets(
                            WindowInsetsCompat.Type.statusBars()
                    );

                    view.setPadding(
                            view.getPaddingLeft(),
                            insets.top,
                            view.getPaddingRight(),
                            view.getPaddingBottom()
                    );

                    return windowInsets;
                }
        );

        routineSelectionMode = getIntent().getBooleanExtra(
                "ROUTINE_SELECTION_MODE",
                false
        );

        progressSelectionMode = getIntent().getBooleanExtra(
                "PROGRESS_SELECTION_MODE",
                false
        );

        currentCategory = getIntent().getStringExtra("CATEGORY");

        if (currentCategory == null) {
            currentCategory = "Abdomen";
        }

        // Receive the existing selection from Option1Activity.
        ArrayList<String> previousSelection =
                getIntent().getStringArrayListExtra("SELECTED_EXERCISES");

        if (previousSelection != null) {
            selectedExercisesBefore.addAll(previousSelection);
        }

        exerciseContainer = findViewById(R.id.exerciseContainer);
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);

        btnAddSelected = findViewById(R.id.btnAddSelected);
        btnAddCustomWorkout = findViewById(R.id.btnAddCustomWorkout);

        tvCategoryTitle.setText(currentCategory + " Exercises");

        btnAddCustomWorkout.setOnClickListener(v ->
                showAddWorkoutDialog()
        );

        if (routineSelectionMode || progressSelectionMode) {

            btnAddSelected.setVisibility(View.VISIBLE);

            if (progressSelectionMode) {
                btnAddSelected.setText("Update Selection");
            } else {
                btnAddSelected.setText("Add Selected Exercises");
            }

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

    // =================================================
    // DISPLAY EXERCISES
    // =================================================

    private void displayExercises() {

        exerciseContainer.removeAllViews();
        exerciseCheckBoxes.clear();
        displayedExerciseNames.clear();

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

            case "shoulders":
                exercises = new String[]{
                        "Shoulder Press",
                        "Dumbbell Lateral Raises",
                        "Front Raises",
                        "Reverse Flyes",
                        "Arnold Press",
                        "Upright Rows",
                        "Face Pulls"
                };
                break;

            case "forearms":
            case "forearm":
                exercises = new String[]{
                        "Wrist Curls",
                        "Reverse Wrist Curls",
                        "Hammer Curls",
                        "Reverse Curls",
                        "Farmer's Carries",
                        "Dead Hangs",
                        "Wrist Roller"
                };
                break;

            case "biceps":
                exercises = new String[]{
                        "Standard Bicep Curls",
                        "Hammer Curls",
                        "Concentration Curls",
                        "Preacher Curls",
                        "Incline Dumbbell Curls",
                        "Cable Curls",
                        "EZ Bar Curls"
                };
                break;

            case "triceps":
                exercises = new String[]{
                        "Tricep Pushdowns",
                        "Overhead Tricep Extensions",
                        "Tricep Dips",
                        "Skull Crushers",
                        "Close-Grip Bench Press",
                        "Diamond Push-Ups",
                        "Dumbbell Kickbacks"
                };
                break;

            case "legs":
                exercises = new String[]{
                        "Squats",
                        "Lunges",
                        "Leg Press",
                        "Calf Raises",
                        "Glute Bridges",
                        "Romanian Deadlifts",
                        "Leg Extensions",
                        "Leg Curls"
                };
                break;

            case "chest":
                exercises = new String[]{
                        "Push-Ups",
                        "Bench Press",
                        "Incline Dumbbell Press",
                        "Chest Flyes",
                        "Cable Crossovers",
                        "Decline Bench Press",
                        "Dumbbell Pullovers"
                };
                break;

            case "back":
                exercises = new String[]{
                        "Pull-Ups",
                        "Lat Pulldowns",
                        "Bent-Over Rows",
                        "Seated Cable Rows",
                        "Deadlifts",
                        "Single-Arm Dumbbell Rows",
                        "Straight-Arm Pulldowns",
                        "T-Bar Rows"
                };
                break;

            case "abdomen":
            case "abs":
            default:
                exercises = new String[]{
                        "Crunches",
                        "Plank",
                        "Leg Raises",
                        "Russian Twists",
                        "Mountain Climbers",
                        "Bicycle Crunches",
                        "Reverse Crunches",
                        "Flutter Kicks"
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

    // =================================================
    // EXERCISE CARD
    // =================================================

    private void addExerciseCard(
            String exerciseName,
            boolean isCustom) {

        // Track every exercise in this category.
        displayedExerciseNames.add(exerciseName);

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
                isCustom
                        ? "Your custom exercise"
                        : currentCategory + " exercise"
        );
        description.setTextColor(Color.rgb(143, 149, 161));
        description.setTextSize(12);

        textLayout.addView(exerciseNameText);
        textLayout.addView(description);

        card.addView(textLayout);

        if (routineSelectionMode || progressSelectionMode) {

            CheckBox checkBox = new CheckBox(this);

            checkBox.setButtonTintList(
                    ColorStateList.valueOf(Color.WHITE)
            );

            LinearLayout.LayoutParams checkParams =
                    new LinearLayout.LayoutParams(dp(40), dp(48));

            checkBox.setLayoutParams(checkParams);
            checkBox.setTag(exerciseName);

            // Restore the previous selection for this exercise.
            if (progressSelectionMode) {
                checkBox.setChecked(
                        containsIgnoreCase(
                                selectedExercisesBefore,
                                exerciseName
                        )
                );
            }

            card.addView(checkBox);
            exerciseCheckBoxes.add(checkBox);

            // Progress mode intentionally allows MULTIPLE checkboxes.
            // There is no listener that unchecks other exercises.

            card.setOnClickListener(v ->
                    checkBox.setChecked(!checkBox.isChecked())
            );

        } else if (isCustom) {

            Button removeButton = new Button(this);
            removeButton.setText("×");
            removeButton.setTextColor(Color.rgb(180, 185, 195));
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

    private boolean containsIgnoreCase(
            ArrayList<String> list,
            String value) {

        for (String item : list) {
            if (item != null && item.equalsIgnoreCase(value)) {
                return true;
            }
        }

        return false;
    }

    // =================================================
    // RETURN SELECTED EXERCISES
    // =================================================

    private void returnSelectedExercises() {

        ArrayList<String> selectedExercises =
                new ArrayList<>();

        if (progressSelectionMode) {

            // Start with selections from all the OTHER categories.
            LinkedHashSet<String> updatedSelection =
                    new LinkedHashSet<>(selectedExercisesBefore);

            // Remove the old selections belonging to this category.
            // This also allows the user to uncheck an exercise.
            for (String displayedName : displayedExerciseNames) {
                removeIgnoreCase(updatedSelection, displayedName);
            }

            // Add the currently checked exercises from this category.
            for (CheckBox checkBox : exerciseCheckBoxes) {

                if (checkBox.isChecked()) {

                    String name = checkBox.getTag().toString();

                    if (!containsIgnoreCase(
                            new ArrayList<>(updatedSelection),
                            name
                    )) {
                        updatedSelection.add(name);
                    }
                }
            }

            selectedExercises.addAll(updatedSelection);

            // Returning an empty list is allowed in Progress mode.
            // It means the user intentionally removed the selections
            // from this category.
            Intent resultIntent = new Intent();

            resultIntent.putStringArrayListExtra(
                    "SELECTED_EXERCISES",
                    selectedExercises
            );

            setResult(RESULT_OK, resultIntent);
            finish();
            return;
        }

        // Existing Routine selection behavior.
        for (CheckBox checkBox : exerciseCheckBoxes) {

            if (checkBox.isChecked()) {
                selectedExercises.add(checkBox.getTag().toString());
            }
        }

        if (selectedExercises.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please select an exercise.",
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

    private void removeIgnoreCase(
            LinkedHashSet<String> set,
            String value) {

        String matchingItem = null;

        for (String item : set) {
            if (item.equalsIgnoreCase(value)) {
                matchingItem = item;
                break;
            }
        }

        if (matchingItem != null) {
            set.remove(matchingItem);
        }
    }

    // =================================================
    // CUSTOM EXERCISES STORAGE
    // =================================================

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

    // =================================================
    // ADD CUSTOM WORKOUT
    // =================================================

    private void showAddWorkoutDialog() {

        EditText input = new EditText(this);
        input.setHint("Enter exercise name");
        input.setSingleLine(true);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.rgb(143, 149, 161));

        int padding = dp(16);
        input.setPadding(padding, padding, padding, padding);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Your Own Workout")
                .setMessage("Enter the name of your custom exercise.")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", (dialogInterface, which) -> {

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
                .create();

        dialog.setOnShowListener(d -> {

            if (dialog.getWindow() != null) {

                GradientDrawable background = new GradientDrawable();
                background.setColor(Color.rgb(41, 45, 53));
                background.setCornerRadius(dp(16));

                dialog.getWindow().setBackgroundDrawable(background);
            }

            TextView title = dialog.findViewById(
                    androidx.appcompat.R.id.alertTitle
            );

            if (title != null) {
                title.setTextColor(Color.WHITE);
            }

            TextView message = dialog.findViewById(android.R.id.message);

            if (message != null) {
                message.setTextColor(Color.rgb(143, 149, 161));
            }

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(Color.WHITE);

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(Color.WHITE);
        });

        dialog.show();
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

    // =================================================
    // REMOVE CUSTOM WORKOUT
    // =================================================

    private void confirmRemoveWorkout(String name) {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Remove Exercise")
                .setMessage("Are you sure you want to remove " + name + "?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (dialogInterface, which) ->
                        removeCustomWorkout(name)
                )
                .create();

        dialog.setOnShowListener(d -> {

            if (dialog.getWindow() != null) {

                GradientDrawable background = new GradientDrawable();
                background.setColor(Color.rgb(41, 45, 53));
                background.setCornerRadius(dp(16));

                dialog.getWindow().setBackgroundDrawable(background);
            }

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(Color.WHITE);

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(Color.WHITE);

            TextView title = dialog.findViewById(
                    androidx.appcompat.R.id.alertTitle
            );

            if (title != null) {
                title.setTextColor(Color.WHITE);
            }

            TextView message = dialog.findViewById(android.R.id.message);

            if (message != null) {
                message.setTextColor(Color.rgb(143, 149, 161));
            }
        });

        dialog.show();
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

    // =================================================
    // DP HELPER
    // =================================================

    private int dp(int value) {

        return (int) (
                value * getResources().getDisplayMetrics().density
                        + 0.5f
        );
    }
}
