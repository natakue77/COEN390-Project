
package com.example.coen390project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyOwnWorkouts extends AppCompatActivity {

    private static final int REQUEST_EXERCISES = 301;

    private static final String PREFS_NAME = "CustomWorkoutPreferences";
    private static final String PREFS_KEY = "customWorkoutLists";

    private final ArrayList<String> pendingExercises = new ArrayList<>();

    private LinearLayout workoutContainer;
    private String pendingWorkoutName = "";

    private boolean routineSelectionMode;

    private final int BACKGROUND = Color.rgb(17, 18, 20);
    private final int CARD = Color.rgb(41, 45, 53);
    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(143, 149, 161);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        routineSelectionMode = getIntent().getBooleanExtra(
                "ROUTINE_SELECTION_MODE", false
        );

        buildScreen();
        loadWorkoutCards();
    }

    private void buildScreen() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BACKGROUND);

        // Toolbar-style header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(12), dp(16), dp(12));

        TextView back = new TextView(this);
        back.setText("‹");
        back.setTextColor(WHITE);
        back.setTextSize(38);
        back.setGravity(Gravity.CENTER);
        back.setPadding(0, 0, dp(18), 0);
        back.setOnClickListener(v -> finish());

        TextView title = new TextView(this);
        title.setText("My Own Workouts");
        title.setTextColor(WHITE);
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);

        header.addView(back);
        header.addView(title);

        root.addView(header);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setClipToPadding(false);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(10), dp(16), dp(24));

        TextView description = new TextView(this);
        description.setText(
                "Create your own workout collections and keep all your favourite exercises together."
        );
        description.setTextColor(MUTED);
        description.setTextSize(14);
        description.setPadding(0, 0, 0, dp(20));

        content.addView(description);

        // Create new workout button
        LinearLayout createCard = new LinearLayout(this);
        createCard.setOrientation(LinearLayout.HORIZONTAL);
        createCard.setGravity(Gravity.CENTER_VERTICAL);
        createCard.setPadding(dp(18), dp(18), dp(18), dp(18));
        createCard.setBackground(makeBackground(CARD, 18));
        createCard.setClickable(true);
        createCard.setFocusable(true);

        TextView plus = new TextView(this);
        plus.setText("+");
        plus.setTextColor(WHITE);
        plus.setTextSize(42);
        plus.setGravity(Gravity.CENTER);
        plus.setIncludeFontPadding(false);

        LinearLayout.LayoutParams plusParams =
                new LinearLayout.LayoutParams(dp(65), dp(65));
        createCard.addView(plus, plusParams);

        LinearLayout createTextContainer = new LinearLayout(this);
        createTextContainer.setOrientation(LinearLayout.VERTICAL);
        createTextContainer.setPadding(dp(14), 0, 0, 0);

        TextView createTitle = new TextView(this);
        createTitle.setText("Create a workout");
        createTitle.setTextColor(WHITE);
        createTitle.setTextSize(17);
        createTitle.setTypeface(null, Typeface.BOLD);

        TextView createSubtitle = new TextView(this);
        createSubtitle.setText("Build your own exercise list");
        createSubtitle.setTextColor(MUTED);
        createSubtitle.setTextSize(13);
        createSubtitle.setPadding(0, dp(5), 0, 0);

        createTextContainer.addView(createTitle);
        createTextContainer.addView(createSubtitle);

        createCard.addView(createTextContainer);

        createCard.setOnClickListener(v -> showCreateWorkoutDialog());

        LinearLayout.LayoutParams createParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(105)
                );
        createParams.bottomMargin = dp(24);

        content.addView(createCard, createParams);

        TextView savedTitle = new TextView(this);
        savedTitle.setText("Your Workout Lists");
        savedTitle.setTextColor(WHITE);
        savedTitle.setTextSize(19);
        savedTitle.setTypeface(null, Typeface.BOLD);
        savedTitle.setPadding(0, 0, 0, dp(12));

        content.addView(savedTitle);

        workoutContainer = new LinearLayout(this);
        workoutContainer.setOrientation(LinearLayout.VERTICAL);

        content.addView(workoutContainer);

        scrollView.addView(content);

        root.addView(
                scrollView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        setContentView(root);
    }

    private void showCreateWorkoutDialog() {

        EditText input = new EditText(this);
        input.setHint("Example: Monday Push Day");
        input.setSingleLine(true);
        input.setTextColor(WHITE);
        input.setHintTextColor(MUTED);
        input.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setPadding(dp(20), dp(8), dp(20), 0);
        dialogLayout.addView(input);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Name your workout")
                .setView(dialogLayout)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Continue", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
        ).setOnClickListener(v -> {

            String name = input.getText().toString().trim();

            if (name.isEmpty()) {
                input.setError("Please enter a workout name.");
                return;
            }

            if (nameExists(name)) {
                input.setError("A workout with this name already exists.");
                return;
            }

            pendingWorkoutName = name;
            pendingExercises.clear();

            dialog.dismiss();
            showCategoryPicker();
        }));

        dialog.show();
    }

    private void showCategoryPicker() {

        String[] categories = {
                "Abdomen",
                "Arms",
                "Legs",
                "Chest",
                "Back"
        };

        new AlertDialog.Builder(this)
                .setTitle("Choose an exercise category")
                .setItems(categories, (dialog, which) ->
                        openExerciseCategory(categories[which]))
                .setNegativeButton("Cancel", (dialog, which) ->
                        askToSaveOrDiscard())
                .show();
    }

    private void openExerciseCategory(String category) {

        Intent intent = new Intent(
                MyOwnWorkouts.this,
                ExerciseListActivity.class
        );

        intent.putExtra("CATEGORY", category);

        // ExerciseListActivity uses this mode to show its Done option.
        intent.putExtra("ROUTINE_SELECTION_MODE", true);

        startActivityForResult(intent, REQUEST_EXERCISES);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EXERCISES) {

            if (resultCode == RESULT_OK && data != null) {

                ArrayList<String> exercises =
                        data.getStringArrayListExtra("SELECTED_EXERCISES");

                if (exercises != null) {
                    for (String exercise : exercises) {
                        if (exercise != null
                                && !pendingExercises.contains(exercise)) {
                            pendingExercises.add(exercise);
                        }
                    }
                }
            }

            if (pendingExercises.isEmpty()) {
                askToSaveOrDiscard();
            } else {
                askToAddMoreExercises();
            }
        }
    }

    private void askToAddMoreExercises() {

        new AlertDialog.Builder(this)
                .setTitle("Exercises added")
                .setMessage(
                        pendingExercises.size()
                                + " exercise(s) selected.\n\n"
                                + "Would you like to add exercises from another category?"
                )
                .setPositiveButton("Add More", (dialog, which) ->
                        showCategoryPicker())
                .setNegativeButton("Save Workout", (dialog, which) ->
                        savePendingWorkout())
                .setNeutralButton("Cancel", (dialog, which) ->
                        discardPendingWorkout())
                .show();
    }

    private void askToSaveOrDiscard() {

        if (pendingExercises.isEmpty()) {
            discardPendingWorkout();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Save your workout?")
                .setMessage(
                        "You have selected "
                                + pendingExercises.size()
                                + " exercise(s)."
                )
                .setPositiveButton("Save", (dialog, which) ->
                        savePendingWorkout())
                .setNegativeButton("Discard", (dialog, which) ->
                        discardPendingWorkout())
                .show();
    }

    private void savePendingWorkout() {

        if (pendingExercises.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please add at least one exercise.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        try {
            JSONArray allWorkouts = getSavedWorkouts();

            JSONObject workout = new JSONObject();
            workout.put("name", pendingWorkoutName);

            JSONArray exercises = new JSONArray();

            for (String exercise : pendingExercises) {
                exercises.put(exercise);
            }

            workout.put("exercises", exercises);
            allWorkouts.put(workout);

            SharedPreferences preferences =
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            preferences.edit()
                    .putString(PREFS_KEY, allWorkouts.toString())
                    .apply();

            Toast.makeText(
                    this,
                    "Workout saved!",
                    Toast.LENGTH_SHORT
            ).show();

            if (routineSelectionMode) {
                returnExercisesToRoutine();
            } else {
                pendingExercises.clear();
                pendingWorkoutName = "";
                loadWorkoutCards();
            }

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Could not save workout.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void returnExercisesToRoutine() {

        Intent result = new Intent();

        result.putStringArrayListExtra(
                "SELECTED_EXERCISES",
                new ArrayList<>(pendingExercises)
        );

        setResult(RESULT_OK, result);
        finish();
    }

    private void discardPendingWorkout() {
        pendingExercises.clear();
        pendingWorkoutName = "";
    }

    private void loadWorkoutCards() {

        workoutContainer.removeAllViews();

        JSONArray workouts = getSavedWorkouts();

        if (workouts.length() == 0) {
            TextView empty = new TextView(this);
            empty.setText("No custom workouts yet.\nTap the plus card above to create one.");
            empty.setTextColor(MUTED);
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(16), dp(25), dp(16), dp(25));

            workoutContainer.addView(empty);
            return;
        }

        for (int i = 0; i < workouts.length(); i++) {

            JSONObject workout = workouts.optJSONObject(i);

            if (workout == null) {
                continue;
            }

            String name = workout.optString("name", "My Workout");
            JSONArray exercises = workout.optJSONArray("exercises");

            int count = exercises == null ? 0 : exercises.length();

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(16), dp(16), dp(16), dp(16));
            card.setBackground(makeBackground(CARD, 16));
            card.setClickable(true);
            card.setFocusable(true);

            LinearLayout textArea = new LinearLayout(this);
            textArea.setOrientation(LinearLayout.VERTICAL);

            TextView nameView = new TextView(this);
            nameView.setText(name);
            nameView.setTextColor(WHITE);
            nameView.setTextSize(16);
            nameView.setTypeface(null, Typeface.BOLD);

            TextView countView = new TextView(this);
            countView.setText(count + (count == 1
                    ? " exercise"
                    : " exercises"));
            countView.setTextColor(MUTED);
            countView.setTextSize(13);
            countView.setPadding(0, dp(5), 0, 0);

            textArea.addView(nameView);
            textArea.addView(countView);

            card.addView(
                    textArea,
                    new LinearLayout.LayoutParams(0,
                            LinearLayout.LayoutParams.WRAP_CONTENT, 1)
            );

            TextView arrow = new TextView(this);
            arrow.setText("›");
            arrow.setTextColor(WHITE);
            arrow.setTextSize(30);
            card.addView(arrow);

            final int index = i;

            card.setOnClickListener(v -> openSavedWorkout(index));

            card.setOnLongClickListener(v -> {
                confirmDeleteWorkout(index, name);
                return true;
            });

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(82)
                    );
            params.bottomMargin = dp(12);

            workoutContainer.addView(card, params);
        }
    }

    private void openSavedWorkout(int index) {

        JSONArray workouts = getSavedWorkouts();
        JSONObject workout = workouts.optJSONObject(index);

        if (workout == null) {
            return;
        }

        JSONArray exercises = workout.optJSONArray("exercises");
        ArrayList<String> selected = new ArrayList<>();

        if (exercises != null) {
            for (int i = 0; i < exercises.length(); i++) {
                String exercise = exercises.optString(i, "");
                if (!exercise.isEmpty()) {
                    selected.add(exercise);
                }
            }
        }

        if (routineSelectionMode) {

            if (selected.isEmpty()) {
                Toast.makeText(
                        this,
                        "This workout has no exercises.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Intent result = new Intent();
            result.putStringArrayListExtra("SELECTED_EXERCISES", selected);
            setResult(RESULT_OK, result);
            finish();

        } else {

            StringBuilder message = new StringBuilder();

            for (int i = 0; i < selected.size(); i++) {
                message.append("• ").append(selected.get(i));
                if (i < selected.size() - 1) {
                    message.append("\n");
                }
            }

            if (message.length() == 0) {
                message.append("No exercises in this list yet.");
            }

            new AlertDialog.Builder(this)
                    .setTitle(workout.optString("name", "My Workout"))
                    .setMessage(message.toString())
                    .setPositiveButton("Close", null)
                    .setNeutralButton("Delete", (dialog, which) ->
                            confirmDeleteWorkout(
                                    index,
                                    workout.optString("name", "My Workout")
                            ))
                    .show();
        }
    }

    private void confirmDeleteWorkout(int index, String name) {

        new AlertDialog.Builder(this)
                .setTitle("Delete workout?")
                .setMessage("Do you want to delete \"" + name + "\"?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    JSONArray workouts = getSavedWorkouts();

                    if (index >= 0 && index < workouts.length()) {
                        workouts.remove(index);
                        saveWorkoutArray(workouts);
                        loadWorkoutCards();

                        Toast.makeText(
                                this,
                                "Workout deleted.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .show();
    }

    private boolean nameExists(String name) {

        JSONArray workouts = getSavedWorkouts();

        for (int i = 0; i < workouts.length(); i++) {
            JSONObject workout = workouts.optJSONObject(i);

            if (workout != null
                    && workout.optString("name")
                    .equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    private JSONArray getSavedWorkouts() {

        SharedPreferences preferences =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String saved = preferences.getString(PREFS_KEY, "[]");

        try {
            return new JSONArray(saved);
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private void saveWorkoutArray(JSONArray workouts) {

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(PREFS_KEY, workouts.toString())
                .apply();
    }

    private GradientDrawable makeBackground(int color, int radius) {

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));

        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }
}
