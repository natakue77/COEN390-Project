
package com.example.coen390project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public class Option2Activity extends AppCompatActivity {

    private static final int REQUEST_PROGRESS_WORKOUT = 301;

    private static final String PREF_GOALS = "WorkoutGoals";

    private static final String KEY_HOME_WORKOUT =
            "dashboard_selected_workout";

    private static final String KEY_HOME_GOAL_TYPE =
            "dashboard_goal_type";

    private static final String KEY_SELECTED_WORKOUTS =
            "progress_selected_workouts";

    private static final String KEY_GOAL_WORKOUT =
            "progress_goal_selected_workout";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private Button btnSelectWorkout;
    private Button btnResetWorkouts;

    private TextView tvSelectedWorkout;
    private TextView tvWorkoutGraphTitle;
    private TextView tvGoalGraphTitle;
    private TextView goalDescriptionTextView;

    private Spinner timelineSpinner;
    private Spinner variableSpinner;
    private Spinner goalTypeSpinner;
    private Spinner goalWorkoutSpinner;

    private EditText goalEditText;
    private Button saveGoalButton;

    private ProgressGraphView workoutGraph;
    private ProgressGraphView goalGraph;

    private SharedPreferences preferences;
    private DatabaseReference completedWorkoutsRef;

    private final ArrayList<CompletedSession> completedSessions =
            new ArrayList<>();

    private final ArrayList<String> selectedWorkouts =
            new ArrayList<>();

    private String goalSelectedWorkout = "";

    private boolean updatingGoalSpinner = false;

    private final String[] timelines = {
            "Last 7 days",
            "Last 30 days",
            "Last 3 months",
            "Last 6 months",
            "Last year",
            "All time"
    };

    private final String[] variables = {
            "Workouts per Week",
            "Reps per Set",
            "Weight"
    };

    private final String[] goalTypes = {
            "Reps per Set",
            "Weight (kg)"
    };

    private final int[] graphColors = {
            Color.rgb(239, 83, 80),
            Color.rgb(66, 165, 245),
            Color.rgb(102, 187, 106),
            Color.rgb(255, 202, 40),
            Color.rgb(171, 71, 188),
            Color.rgb(38, 198, 218),
            Color.rgb(255, 167, 38),
            Color.rgb(236, 64, 122)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_option2);

        View mainContent = findViewById(R.id.mainContent);
        int originalTopPadding = mainContent.getPaddingTop();

        ViewCompat.setOnApplyWindowInsetsListener(mainContent,
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
                });

        ViewCompat.requestApplyInsets(mainContent);

        Toolbar toolbar = findViewById(R.id.optionToolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Progress & Analytics");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

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
        getOnBackPressedDispatcher().addCallback(this,
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (drawerLayout != null
                                && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        } else {
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                            setEnabled(true);
                        }
                    }
                });

        btnSelectWorkout = findViewById(R.id.btnSelectWorkout);
        btnResetWorkouts = findViewById(R.id.btnResetWorkouts);

        tvSelectedWorkout = findViewById(R.id.tvSelectedWorkout);

        timelineSpinner = findViewById(R.id.timelineSpinner);
        variableSpinner = findViewById(R.id.variableSpinner);
        goalTypeSpinner = findViewById(R.id.goalTypeSpinner);
        goalWorkoutSpinner = findViewById(R.id.goalWorkoutSpinner);

        goalEditText = findViewById(R.id.goalEditText);
        saveGoalButton = findViewById(R.id.saveGoalButton);

        goalDescriptionTextView =
                findViewById(R.id.goalDescriptionTextView);

        tvWorkoutGraphTitle = findViewById(R.id.tvWorkoutGraphTitle);
        tvGoalGraphTitle = findViewById(R.id.tvGoalGraphTitle);

        workoutGraph = findViewById(R.id.workoutGraph);
        goalGraph = findViewById(R.id.goalGraph);

        preferences = getSharedPreferences(PREF_GOALS, MODE_PRIVATE);
        loadSavedWorkoutSelections();

        completedWorkoutsRef = FirebaseDatabase.getInstance()
                .getReference("completedWorkouts");

        setupSpinners();
        setupButtons();

        updateWorkoutSelectionDisplay();
        updateGoalWorkoutSpinner();

        loadCompletedWorkouts();
    }

    // =================================================
    // RESET SELECTED WORKOUTS
    // =================================================

    private void resetSelectedWorkouts() {

        selectedWorkouts.clear();
        goalSelectedWorkout = "";

        // Clear only the Progress selection.
        // Firebase completed workout history is not changed.
        // Saved goal values are not deleted.
        preferences.edit()
                .remove(KEY_SELECTED_WORKOUTS)
                .remove(KEY_GOAL_WORKOUT)
                .remove(KEY_HOME_WORKOUT)
                .apply();

        // Restore the Choose Workout button.
        btnSelectWorkout.setVisibility(View.VISIBLE);
        btnSelectWorkout.setText("Choose a Workout");

        // Hide the selected workout names.
        tvSelectedWorkout.setText("");
        tvSelectedWorkout.setVisibility(View.GONE);

        // Empty the Goal Workout dropdown.
        updatingGoalSpinner = true;

        ArrayAdapter<String> emptyAdapter =
                createSpinnerAdapter(new ArrayList<>());

        goalWorkoutSpinner.setAdapter(emptyAdapter);

        updatingGoalSpinner = false;

        // Clear the goal input and description.
        goalEditText.setText("");

        goalDescriptionTextView.setText(
                "Choose a workout and set a goal."
        );

        // Clear the graphs.
        workoutGraph.setMessage(
                "Choose workouts to view your progress."
        );

        goalGraph.setMessage(
                "Choose a workout to view goal progress."
        );

        Toast.makeText(
                this,
                "Workout selection reset.",
                Toast.LENGTH_SHORT
        ).show();
    }

    // =================================================
    // RESTORE AND SAVE WORKOUT SELECTIONS
    // =================================================

    private void loadSavedWorkoutSelections() {
        selectedWorkouts.clear();

        String savedJson = preferences.getString(
                KEY_SELECTED_WORKOUTS, ""
        );

        if (!savedJson.isEmpty()) {
            try {
                JSONArray array = new JSONArray(savedJson);

                for (int i = 0; i < array.length(); i++) {
                    String name = array.optString(i, "").trim();

                    if (!name.isEmpty()
                            && !selectedWorkouts.contains(name)) {
                        selectedWorkouts.add(name);
                    }
                }
            } catch (JSONException ignored) {
                selectedWorkouts.clear();
            }
        }

        // Backwards compatibility with the previous single-workout setup.
        if (selectedWorkouts.isEmpty()) {
            String oldWorkout = preferences.getString(
                    KEY_HOME_WORKOUT, ""
            );

            if (oldWorkout != null && !oldWorkout.trim().isEmpty()) {
                selectedWorkouts.add(oldWorkout.trim());
            }
        }

        goalSelectedWorkout = preferences.getString(
                KEY_GOAL_WORKOUT, ""
        );

        if (!selectedWorkouts.contains(goalSelectedWorkout)) {
            goalSelectedWorkout = selectedWorkouts.isEmpty()
                    ? "" : selectedWorkouts.get(0);
        }
    }

    private void saveWorkoutSelections() {
        JSONArray array = new JSONArray();

        for (String workout : selectedWorkouts) {
            array.put(workout);
        }

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(KEY_SELECTED_WORKOUTS, array.toString());

        // Keep the first selected workout available to the Home dashboard.
        if (!selectedWorkouts.isEmpty()) {
            editor.putString(
                    KEY_HOME_WORKOUT,
                    selectedWorkouts.get(0)
            );
        } else {
            editor.remove(KEY_HOME_WORKOUT);
        }

        editor.putString(KEY_GOAL_WORKOUT, goalSelectedWorkout);
        editor.apply();
    }

    // =================================================
    // SELECTED WORKOUT DISPLAY
    // =================================================

    private void updateWorkoutSelectionDisplay() {
        if (selectedWorkouts.isEmpty()) {
            btnSelectWorkout.setText("Choose a Workout");
            tvSelectedWorkout.setVisibility(View.GONE);
            return;
        }

        btnSelectWorkout.setText("Edit Workout Selection");

        tvSelectedWorkout.setVisibility(View.VISIBLE);

        StringBuilder display = new StringBuilder();

        for (int i = 0; i < selectedWorkouts.size(); i++) {
            if (i > 0) {
                display.append(", ");
            }

            display.append(selectedWorkouts.get(i));
        }

        tvSelectedWorkout.setText(display.toString());
        tvSelectedWorkout.setOnClickListener(v -> openWorkoutSelection());
    }

    // =================================================
    // DRAWER
    // =================================================

    private void setupDrawer(Toolbar toolbar) {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        if (drawerLayout == null || navigationView == null) return;

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

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
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                );
                startActivity(intent);
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    // =================================================
    // SPINNERS
    // =================================================

    private void setupSpinners() {
        timelineSpinner.setAdapter(createSpinnerAdapter(
                new ArrayList<>(Arrays.asList(timelines))
        ));

        variableSpinner.setAdapter(createSpinnerAdapter(
                new ArrayList<>(Arrays.asList(variables))
        ));

        goalTypeSpinner.setAdapter(createSpinnerAdapter(
                new ArrayList<>(Arrays.asList(goalTypes))
        ));

        timelineSpinner.setSelection(1);

        int savedGoalType = preferences.getInt(
                KEY_HOME_GOAL_TYPE, 0
        );

        if (savedGoalType < 0 || savedGoalType >= goalTypes.length) {
            savedGoalType = 0;
        }

        goalTypeSpinner.setSelection(savedGoalType);

        timelineSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {
                        refreshGraphs();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );

        variableSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {
                        refreshGraphs();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );

        goalTypeSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        preferences.edit()
                                .putInt(KEY_HOME_GOAL_TYPE, position)
                                .apply();

                        loadGoal();
                        refreshGraphs();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );

        goalWorkoutSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        if (updatingGoalSpinner) return;

                        if (position >= 0
                                && position < selectedWorkouts.size()) {

                            goalSelectedWorkout =
                                    selectedWorkouts.get(position);

                            preferences.edit()
                                    .putString(
                                            KEY_GOAL_WORKOUT,
                                            goalSelectedWorkout
                                    )
                                    .apply();

                            loadGoal();
                            refreshGraphs();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );
    }

    private ArrayAdapter<String> createSpinnerAdapter(
            List<String> values) {

        return new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                values
        ) {
            @Override
            public View getView(
                    int position,
                    View convertView,
                    ViewGroup parent) {

                TextView text = (TextView) super.getView(
                        position, convertView, parent
                );

                text.setTextColor(Color.WHITE);
                return text;
            }

            @Override
            public View getDropDownView(
                    int position,
                    View convertView,
                    ViewGroup parent) {

                TextView text = (TextView) super.getDropDownView(
                        position, convertView, parent
                );

                text.setTextColor(Color.WHITE);
                text.setBackgroundColor(Color.rgb(41, 45, 53));

                return text;
            }
        };
    }

    private void updateGoalWorkoutSpinner() {
        if (goalWorkoutSpinner == null) return;

        updatingGoalSpinner = true;

        ArrayList<String> spinnerItems =
                new ArrayList<>(selectedWorkouts);

        ArrayAdapter<String> adapter =
                createSpinnerAdapter(spinnerItems);

        goalWorkoutSpinner.setAdapter(adapter);

        if (spinnerItems.isEmpty()) {
            goalSelectedWorkout = "";
        } else {
            int selectedPosition =
                    spinnerItems.indexOf(goalSelectedWorkout);

            if (selectedPosition < 0) {
                selectedPosition = 0;
                goalSelectedWorkout = spinnerItems.get(0);
            }

            goalWorkoutSpinner.setSelection(selectedPosition);
        }

        preferences.edit()
                .putString(KEY_GOAL_WORKOUT, goalSelectedWorkout)
                .apply();

        updatingGoalSpinner = false;

        loadGoal();
    }

    // =================================================
    // BUTTONS AND WORKOUT SELECTION
    // =================================================

    private void setupButtons() {
        btnSelectWorkout.setOnClickListener(v ->
                openWorkoutSelection()
        );

        btnResetWorkouts.setOnClickListener(v ->
                resetSelectedWorkouts()
        );

        saveGoalButton.setOnClickListener(v -> saveGoal());
    }

    private void openWorkoutSelection() {
        Intent intent = new Intent(
                Option2Activity.this,
                Option1Activity.class
        );

        intent.putExtra("PROGRESS_SELECTION_MODE", true);

        intent.putStringArrayListExtra(
                "SELECTED_EXERCISES",
                new ArrayList<>(selectedWorkouts)
        );

        startActivityForResult(
                intent,
                REQUEST_PROGRESS_WORKOUT
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PROGRESS_WORKOUT
                && resultCode == RESULT_OK
                && data != null) {

            ArrayList<String> returnedWorkouts =
                    data.getStringArrayListExtra("SELECTED_EXERCISES");

            // Compatibility with the older single-selection result.
            if (returnedWorkouts == null) {
                String singleWorkout =
                        data.getStringExtra("SELECTED_EXERCISE");

                returnedWorkouts = new ArrayList<>();

                if (singleWorkout != null
                        && !singleWorkout.trim().isEmpty()) {
                    returnedWorkouts.add(singleWorkout.trim());
                }
            }

            LinkedHashSet<String> unique =
                    new LinkedHashSet<>();

            for (String workout : returnedWorkouts) {
                if (workout != null && !workout.trim().isEmpty()) {
                    unique.add(workout.trim());
                }
            }

            selectedWorkouts.clear();
            selectedWorkouts.addAll(unique);

            if (!selectedWorkouts.contains(goalSelectedWorkout)) {
                goalSelectedWorkout = selectedWorkouts.isEmpty()
                        ? "" : selectedWorkouts.get(0);
            }

            saveWorkoutSelections();
            updateWorkoutSelectionDisplay();
            updateGoalWorkoutSpinner();
            refreshGraphs();

            Toast.makeText(
                    this,
                    selectedWorkouts.size()
                            + " workout(s) selected.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =================================================
    // LOAD COMPLETED WORKOUTS FROM FIREBASE
    // =================================================

    private void loadCompletedWorkouts() {
        completedWorkoutsRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        completedSessions.clear();

                        for (DataSnapshot sessionSnapshot
                                : snapshot.getChildren()) {

                            CompletedSession session =
                                    new CompletedSession();

                            session.sessionId = sessionSnapshot
                                    .child("sessionId")
                                    .getValue(String.class);

                            if (session.sessionId == null) {
                                session.sessionId =
                                        sessionSnapshot.getKey();
                            }

                            session.date = sessionSnapshot
                                    .child("date")
                                    .getValue(String.class);

                            Boolean completed = sessionSnapshot
                                    .child("completed")
                                    .getValue(Boolean.class);

                            if (completed != null && !completed) continue;
                            if (session.date == null) continue;

                            DataSnapshot exercisesSnapshot =
                                    sessionSnapshot.child("exercises");

                            for (DataSnapshot exerciseSnapshot
                                    : exercisesSnapshot.getChildren()) {

                                ExerciseRecord exercise =
                                        new ExerciseRecord();

                                exercise.name = exerciseSnapshot
                                        .child("name")
                                        .getValue(String.class);

                                exercise.reps = getNumber(
                                        exerciseSnapshot.child("reps")
                                );

                                exercise.sets = getNumber(
                                        exerciseSnapshot.child("sets")
                                );

                                Object weightValue = exerciseSnapshot
                                        .child("weight").getValue();

                                exercise.weight = weightValue == null
                                        ? null : weightValue.toString();

                                exercise.weightUnit = exerciseSnapshot
                                        .child("weightUnit")
                                        .getValue(String.class);

                                if (exercise.name != null) {
                                    session.exercises.add(exercise);
                                }
                            }

                            if (!session.exercises.isEmpty()) {
                                completedSessions.add(session);
                            }
                        }

                        Collections.sort(
                                completedSessions,
                                Comparator.comparingLong(
                                        session -> parseDate(session.date)
                                )
                        );

                        refreshGraphs();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(
                                Option2Activity.this,
                                "Could not load workout history: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private int getNumber(DataSnapshot snapshot) {
        Object value = snapshot.getValue();

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value != null) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException ignored) {
            }
        }

        return 0;
    }

    private long parseDate(String date) {
        try {
            SimpleDateFormat format =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            Date parsed = format.parse(date);
            return parsed == null ? 0 : parsed.getTime();
        } catch (ParseException e) {
            return 0;
        }
    }

    // =================================================
    // TIMELINE FILTERING
    // =================================================

    private int getTimelineDays() {
        int position = timelineSpinner.getSelectedItemPosition();

        switch (position) {
            case 0:
                return 7;
            case 1:
                return 30;
            case 2:
                return 90;
            case 3:
                return 180;
            case 4:
                return 365;
            default:
                return 3650;
        }
    }

    private List<CompletedSession> getFilteredSessions() {
        long now = System.currentTimeMillis();

        if (timelineSpinner.getSelectedItemPosition() == 5) {
            return new ArrayList<>(completedSessions);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        calendar.add(Calendar.DAY_OF_YEAR, -getTimelineDays());

        long startTime = calendar.getTimeInMillis();

        ArrayList<CompletedSession> filtered = new ArrayList<>();

        for (CompletedSession session : completedSessions) {
            long sessionTime = parseDate(session.date);

            if (sessionTime >= startTime && sessionTime <= now) {
                filtered.add(session);
            }
        }

        return filtered;
    }

    // =================================================
    // REFRESH GRAPHS
    // =================================================

    private void refreshGraphs() {
        if (workoutGraph == null || goalGraph == null
                || timelineSpinner == null || variableSpinner == null
                || goalTypeSpinner == null) {
            return;
        }

        List<CompletedSession> filtered = getFilteredSessions();

        int variablePosition =
                variableSpinner.getSelectedItemPosition();

        if (variablePosition == 0) {
            showWorkoutsByWeekday(filtered);
        } else if (variablePosition == 1) {
            showExerciseHistory(filtered, false);
        } else {
            showExerciseHistory(filtered, true);
        }

        showGoalGraph(filtered);
    }

    private void showWorkoutsByWeekday(
            List<CompletedSession> sessions) {

        String[] labels = {
                "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
        };

        float[] values = new float[7];

        for (CompletedSession session : sessions) {
            long time = parseDate(session.date);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);

            int day = calendar.get(Calendar.DAY_OF_WEEK);
            int index = (day + 5) % 7;

            values[index]++;
        }

        tvWorkoutGraphTitle.setText(
                "Completed Workouts by Weekday"
        );

        workoutGraph.setData(
                labels, values, 0, true, "Workouts", false
        );
    }

    // =================================================
    // MULTI-WORKOUT HISTORY GRAPH
    // =================================================

    private void showExerciseHistory(
            List<CompletedSession> sessions,
            boolean showWeight) {

        tvWorkoutGraphTitle.setText(
                showWeight
                        ? "Selected Workouts - Weight (kg)"
                        : "Selected Workouts - Reps per Set"
        );

        if (selectedWorkouts.isEmpty()) {
            workoutGraph.setMessage(
                    "Choose one or more workouts to see their history."
            );
            return;
        }

        TreeSet<String> dateSet = new TreeSet<>();

        LinkedHashMap<String, LinkedHashMap<String, ArrayList<Float>>>
                collected = new LinkedHashMap<>();

        for (String workout : selectedWorkouts) {
            collected.put(workout, new LinkedHashMap<>());
        }

        for (CompletedSession session : sessions) {
            if (session.date == null) continue;

            for (ExerciseRecord exercise : session.exercises) {
                String matchingWorkout = findSelectedWorkout(
                        exercise.name
                );

                if (matchingWorkout == null) continue;

                Float value;

                if (showWeight) {
                    value = parseWeightKg(
                            exercise.weight,
                            exercise.weightUnit
                    );
                } else {
                    value = exercise.reps > 0
                            ? (float) exercise.reps : null;
                }

                if (value == null) continue;

                dateSet.add(session.date);

                LinkedHashMap<String, ArrayList<Float>> byDate =
                        collected.get(matchingWorkout);

                if (!byDate.containsKey(session.date)) {
                    byDate.put(
                            session.date,
                            new ArrayList<>()
                    );
                }

                byDate.get(session.date).add(value);
            }
        }

        if (dateSet.isEmpty()) {
            workoutGraph.setMessage(
                    "No recorded "
                            + (showWeight ? "weight" : "repetition")
                            + " history for the selected workouts."
            );
            return;
        }

        ArrayList<String> sortedDates =
                new ArrayList<>(dateSet);

        String[] labels = new String[sortedDates.size()];

        for (int i = 0; i < sortedDates.size(); i++) {
            labels[i] = shortDate(sortedDates.get(i));
        }

        String[] seriesNames =
                selectedWorkouts.toArray(new String[0]);

        float[][] seriesValues =
                new float[selectedWorkouts.size()][sortedDates.size()];

        int[] colors = new int[selectedWorkouts.size()];

        for (int workoutIndex = 0;
             workoutIndex < selectedWorkouts.size();
             workoutIndex++) {

            String workout = selectedWorkouts.get(workoutIndex);

            colors[workoutIndex] =
                    graphColors[workoutIndex % graphColors.length];

            LinkedHashMap<String, ArrayList<Float>> byDate =
                    collected.get(workout);

            for (int dateIndex = 0;
                 dateIndex < sortedDates.size();
                 dateIndex++) {

                String date = sortedDates.get(dateIndex);

                ArrayList<Float> values = byDate.get(date);

                if (values == null || values.isEmpty()) {
                    seriesValues[workoutIndex][dateIndex] =
                            Float.NaN;
                } else {
                    float total = 0;

                    for (Float value : values) {
                        total += value;
                    }

                    seriesValues[workoutIndex][dateIndex] =
                            total / values.size();
                }
            }
        }

        workoutGraph.setMultiLineData(
                labels,
                seriesNames,
                seriesValues,
                colors
        );
    }

    private String findSelectedWorkout(String exerciseName) {
        if (exerciseName == null) return null;

        for (String selected : selectedWorkouts) {
            if (selected.equalsIgnoreCase(exerciseName.trim())) {
                return selected;
            }
        }

        return null;
    }

    private Float parseWeightKg(String weightText, String unit) {
        if (weightText == null
                || weightText.trim().isEmpty()
                || weightText.equals("--")) {
            return null;
        }

        try {
            float value = Float.parseFloat(
                    weightText.replace(",", ".").trim()
            );

            if (value <= 0) return null;

            if (unit != null
                    && (unit.equalsIgnoreCase("LB")
                    || unit.equalsIgnoreCase("LBS"))) {
                value *= 0.45359237f;
            }

            return value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String shortDate(String date) {
        try {
            SimpleDateFormat input =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            SimpleDateFormat output =
                    new SimpleDateFormat("MM/dd", Locale.US);

            Date parsed = input.parse(date);
            return parsed == null ? date : output.format(parsed);
        } catch (ParseException e) {
            return date;
        }
    }

    private float[] toFloatArray(ArrayList<Float> values) {
        float[] result = new float[values.size()];

        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }

        return result;
    }

    // =================================================
    // GOAL STORAGE
    // =================================================

    private String getGoalKey() {
        String workoutKey = goalSelectedWorkout
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "_");

        String type = goalTypeSpinner.getSelectedItemPosition() == 0
                ? "reps"
                : "weight";

        return workoutKey + "_" + type;
    }

    private void saveGoal() {
        if (goalSelectedWorkout.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please select a Goal Workout first.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String input = goalEditText.getText().toString().trim();

        if (input.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please enter a target.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        float target;

        try {
            target = Float.parseFloat(input.replace(",", "."));
        } catch (NumberFormatException e) {
            Toast.makeText(
                    this,
                    "Please enter a valid number.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (target <= 0) {
            Toast.makeText(
                    this,
                    "Goal must be greater than zero.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        preferences.edit()
                .putFloat(getGoalKey(), target)
                .apply();

        loadGoal();
        refreshGraphs();

        Toast.makeText(
                this,
                "Goal saved successfully.",
                Toast.LENGTH_SHORT
        ).show();
    }

    private float getSavedGoal() {
        if (goalSelectedWorkout.isEmpty()) return 0;
        return preferences.getFloat(getGoalKey(), 0);
    }

    private void loadGoal() {
        if (goalEditText == null || goalDescriptionTextView == null) {
            return;
        }

        if (goalSelectedWorkout.isEmpty()) {
            goalEditText.setText("");
            goalDescriptionTextView.setText(
                    "Choose a Goal Workout to set a goal."
            );
            return;
        }

        float target = getSavedGoal();

        boolean repsGoal =
                goalTypeSpinner.getSelectedItemPosition() == 0;

        if (target > 0) {
            goalEditText.setText(formatNumber(target));

            goalDescriptionTextView.setText(
                    goalSelectedWorkout + " goal: "
                            + formatNumber(target)
                            + (repsGoal ? " reps per set" : " kg")
            );
        } else {
            goalEditText.setText("");

            goalDescriptionTextView.setText(
                    "No " + (repsGoal ? "rep" : "weight")
                            + " goal set for " + goalSelectedWorkout
            );
        }
    }

    private String formatNumber(float value) {
        if (value == (int) value) {
            return String.valueOf((int) value);
        }

        return String.format(Locale.US, "%.1f", value);
    }

    // =================================================
    // SINGLE-WORKOUT GOAL GRAPH
    // =================================================

    private void showGoalGraph(List<CompletedSession> sessions) {
        if (goalSelectedWorkout.isEmpty()) {
            tvGoalGraphTitle.setText("Goal Progress");
            goalGraph.setMessage(
                    "Choose a Goal Workout to see goal progress."
            );
            return;
        }

        boolean repsGoal =
                goalTypeSpinner.getSelectedItemPosition() == 0;

        float target = getSavedGoal();

        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Float> values = new ArrayList<>();

        for (CompletedSession session : sessions) {
            for (ExerciseRecord exercise : session.exercises) {
                if (!exercise.name.equalsIgnoreCase(
                        goalSelectedWorkout)) {
                    continue;
                }

                if (repsGoal) {
                    if (exercise.reps <= 0) continue;
                    values.add((float) exercise.reps);
                } else {
                    Float weight = parseWeightKg(
                            exercise.weight,
                            exercise.weightUnit
                    );

                    if (weight == null) continue;
                    values.add(weight);
                }

                labels.add(shortDate(session.date));
            }
        }

        String unit = repsGoal ? "Reps per Set" : "Weight (kg)";

        tvGoalGraphTitle.setText(
                goalSelectedWorkout + " - Goal Progress"
        );

        if (target <= 0) {
            goalGraph.setMessage(
                    "Set a goal to display your target line."
            );
            return;
        }

        goalGraph.setData(
                labels.toArray(new String[0]),
                toFloatArray(values),
                target,
                false,
                unit,
                true
        );
    }


    // =================================================
    // DATA CLASSES
    // =================================================

    private static class CompletedSession {
        String sessionId;
        String date;
        ArrayList<ExerciseRecord> exercises = new ArrayList<>();
    }

    private static class ExerciseRecord {
        String name;
        int reps;
        int sets;
        String weight;
        String weightUnit;
    }
}
