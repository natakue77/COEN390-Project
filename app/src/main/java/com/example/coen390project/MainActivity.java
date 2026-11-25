
package com.example.coen390project;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.DashPathEffect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements BLEManager.Listener {

    // =================================================
    // DASHBOARD AND NAVIGATION
    // =================================================

    private DrawerLayout drawerLayout;
    private LinearLayout dashboardContainer;

    private TextView workoutName, workoutDetails, workoutWeight;
    private TextView workoutWeightUnit, workoutReps, workoutSets;
    private TextView weeklyNumber, weeklyDescription, upcomingDescription;

    private Button checkButton, setButton;

    private final ArrayList<ExerciseEntry> todayExerciseEntries =
            new ArrayList<>();

    private int currentExercise = 0;
    private int progressPage = 0;

    private SharedPreferences weightPreferences;
    private DatabaseReference completedWorkoutsRef;

    private ProgressGraphView homeProgressGraph;
    private TextView homeProgressHeading;
    private TextView homeProgressHint;

    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(143, 149, 161);
    private final int CARD_COLOR = Color.rgb(41, 45, 53);

    private static final double KG_TO_LB = 2.2046226218;

    private static final String PREF_GOALS = "WorkoutGoals";
    private static final String KEY_HOME_WORKOUT = "dashboard_selected_workout";
    private static final String KEY_HOME_GOAL_TYPE = "dashboard_goal_type";

    // =================================================
    // ESP32 BLUETOOTH HARDWARE
    // =================================================

    private BLEManager bleManager;
    private TextView heartRateText;
    private TextView heartRateStatus;
    private TextView heartRateDescription;

    private int currentHeartRate = 0;
    private final android.os.Handler heartRateHandler =
            new android.os.Handler(android.os.Looper.getMainLooper());

    private final Runnable heartRateTimeout = () -> {
        currentHeartRate = 0;
        updateHeartRateDisplay();
    };

    private SensorProcess sensorProcess = SensorProcess.getInstance();
    // Temporary test mode for the Android emulator
    private static final boolean SIMULATE_IMU = false;
    // Timer for simulated IMU readings
    private final android.os.Handler simulationHandler =
            new android.os.Handler(android.os.Looper.getMainLooper());

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        if (bleManager != null && bleManager.hasPermissions()) {
                            bleManager.scanAndConnect();
                        } else {
                            Toast.makeText(
                                    this,
                                    "Bluetooth permissions denied",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    // =================================================
    // DATA CLASSES
    // =================================================

    private static class ExerciseEntry {
        String name;
        String sessionKey;
        String sessionDate;
        String sessionTime;
        int exerciseIndex;
        int sessionExerciseCount;

        ExerciseEntry(String name, String sessionKey, String sessionDate,
                      String sessionTime, int exerciseIndex,
                      int sessionExerciseCount) {
            this.name = name;
            this.sessionKey = sessionKey;
            this.sessionDate = sessionDate;
            this.sessionTime = sessionTime;
            this.exerciseIndex = exerciseIndex;
            this.sessionExerciseCount = sessionExerciseCount;
        }
    }

    private static class HomeExerciseRecord {
        String name;
        int reps;
        String weight;
        String weightUnit;
    }

    private static class HomeCompletedSession {
        String date;
        ArrayList<HomeExerciseRecord> exercises = new ArrayList<>();
    }

    // =================================================
    // DASHBOARD IMAGE SIZES AND POSITIONS
    // =================================================

    private static final int DUMBBELL_WIDTH = 40;
    private static final int DUMBBELL_HEIGHT = 38;

    private static final int WEEKLY_WIDTH = 38;
    private static final int WEEKLY_HEIGHT = 38;

    private static final int PROGRESS_WIDTH = 34;
    private static final int PROGRESS_HEIGHT = 34;

    private static final int UPCOMING_WIDTH = 32;
    private static final int UPCOMING_HEIGHT = 32;

    private static final int HEART_WIDTH = 32;
    private static final int HEART_HEIGHT = 32;

    private static final int WEEKLY_PLACEMENT = Gravity.CENTER;
    private static final int PROGRESS_PLACEMENT = Gravity.CENTER;

    private static final int UPCOMING_PLACEMENT =
            Gravity.START | Gravity.CENTER_VERTICAL;

    private static final int WEEKLY_LEFT = -27;
    private static final int WEEKLY_TOP = 0;
    private static final int WEEKLY_RIGHT = 0;
    private static final int WEEKLY_BOTTOM = 0;

    private static final int PROGRESS_LEFT = 0;
    private static final int PROGRESS_TOP = 0;
    private static final int PROGRESS_RIGHT = 0;
    private static final int PROGRESS_BOTTOM = 0;

    private static final int UPCOMING_LEFT = -3;
    private static final int UPCOMING_TOP = 0;
    private static final int UPCOMING_RIGHT = 9;
    private static final int UPCOMING_BOTTOM = 0;

    // =================================================
    // ON CREATE
    // =================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(
                getWindow(), false
        );

        setContentView(R.layout.activity_main);

        // Initialize ESP32 Bluetooth manager.
        bleManager = new BLEManager(this, this);

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

        weightPreferences = getSharedPreferences(
                "DashboardExerciseWeights", MODE_PRIVATE
        );

        completedWorkoutsRef = FirebaseDatabase.getInstance()
                .getReference("completedWorkouts");

        dashboardContainer = findViewById(R.id.dashboardContainer);

        setupToolbar();
        buildDashboard();


        // Start searching for the ESP32 tracker.
        if (SIMULATE_IMU) {
            simulationHandler.post(simulationRunnable);
        } else {
            connectToTracker();
        }
    }

    // =================================================
    // TOOLBAR AND NAVIGATION
    // =================================================

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar =
                findViewById(R.id.mainToolbar);

        setSupportActionBar(toolbar);

        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i);

            if (child instanceof TextView) {
                child.setTranslationY(
                        -7 * getResources().getDisplayMetrics().density
                );
                break;
            }
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Home");
        }

        drawerLayout = findViewById(R.id.drawerLayout);

        NavigationView navigationView =
                findViewById(R.id.navigationView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            } else if (id == R.id.nav_workouts) {
                openActivity(Option1Activity.class);

            } else if (id == R.id.nav_progress) {
                openActivity(Option2Activity.class);

            } else if (id == R.id.nav_routine) {
                openActivity(Option3Activity.class);

            } else if (id == R.id.nav_tips) {
                openActivity(Option4Activity.class);

            } else if (id == R.id.nav_settings) {
                openActivity(Option5Activity.class);
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        } else {
                            finish();
                        }
                    }
                }
        );
    }

    private void openActivity(Class<?> activity) {
        startActivity(new Intent(MainActivity.this, activity));
    }

    // =================================================
    // DASHBOARD
    // =================================================

    private void buildDashboard() {
        dashboardContainer.removeAllViews();

        loadTodayExercises();

        addWelcomeHeader();
        addWorkoutWidget();
        addWeeklyAndUpcomingRow();
        addProgressWidget();
        addHeartRateWidget();
    }

    private void addWelcomeHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(0, dp(8), 0, dp(20));

        header.addView(createText(
                "Welcome Back!", 25, WHITE, true
        ));

        header.addView(createText(
                "READY FOR YOUR NEXT SESSION?", 11, MUTED, true
        ));

        dashboardContainer.addView(header);
    }

    // =================================================
    // TODAY'S ROUTINE EXERCISES
    // =================================================

    private void loadTodayExercises() {
        todayExerciseEntries.clear();

        String today = formatDate(System.currentTimeMillis());

        JSONArray sessions =
                RoutineStorage.getSessionsForDate(this, today);

        for (int i = 0; i < sessions.length(); i++) {
            JSONObject session = sessions.optJSONObject(i);

            if (session == null) continue;

            JSONArray exercises = session.optJSONArray("exercises");

            if (exercises == null) continue;

            String sessionId = session.optString("id", "");
            String sessionTime = session.optString("time", "");

            String sessionKey;

            if (!sessionId.isEmpty()) {
                sessionKey = today + "_" + sessionId;
            } else {
                sessionKey = today + "_"
                        + sessionTime.replaceAll("[^a-zA-Z0-9]", "_")
                        + "_" + i;
            }

            sessionKey = sessionKey.replaceAll(
                    "[^a-zA-Z0-9_-]", "_"
            );

            for (int j = 0; j < exercises.length(); j++) {
                String name = exercises.optString(j, "").trim();

                if (name.isEmpty()) continue;

                todayExerciseEntries.add(new ExerciseEntry(
                        name,
                        sessionKey,
                        today,
                        sessionTime,
                        j,
                        exercises.length()
                ));
            }
        }

        currentExercise = findNextUncompletedExercise(-1);
    }

    private String exercisePreferenceKey(
            String type, ExerciseEntry entry) {

        return type + "_" + entry.sessionKey
                + "_" + entry.exerciseIndex;
    }

    private int getExerciseReps(ExerciseEntry entry) {
        return weightPreferences.getInt(
                exercisePreferenceKey("reps", entry), 0
        );
    }

    private int getExerciseSets(ExerciseEntry entry) {
        return weightPreferences.getInt(
                exercisePreferenceKey("sets", entry), 0
        );
    }

    private boolean isExerciseCompleted(ExerciseEntry entry) {
        return weightPreferences.getBoolean(
                exercisePreferenceKey("done", entry), false
        );
    }

    private int findNextUncompletedExercise(int startIndex) {
        int size = todayExerciseEntries.size();

        if (size == 0) return -1;

        for (int offset = 1; offset <= size; offset++) {
            int index = (startIndex + offset) % size;

            if (!isExerciseCompleted(todayExerciseEntries.get(index))) {
                return index;
            }
        }

        return -1;
    }

    // =================================================
    // TODAY'S WORKOUT WIDGET
    // =================================================

    private void addWorkoutWidget() {
        LinearLayout card = createCard();
        card.setPadding(dp(12), dp(10), dp(12), dp(10));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        addTitleIcon(
                topRow,
                R.drawable.img_workouts,
                "Dumbbell icon",
                DUMBBELL_WIDTH,
                DUMBBELL_HEIGHT,
                -10, 2, 0, 0
        );

        TextView heading = createText(
                "TODAY'S WORKOUT", 12, WHITE, true
        );

        topRow.addView(heading, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        Button menuButton = createSmallButton("⋮");
        menuButton.setTextSize(21);
        menuButton.setOnClickListener(v -> showWorkoutMenu(menuButton));

        topRow.addView(menuButton);
        card.addView(topRow);

        LinearLayout playerRow = new LinearLayout(this);
        playerRow.setOrientation(LinearLayout.HORIZONTAL);
        playerRow.setGravity(Gravity.CENTER_VERTICAL);
        playerRow.setPadding(0, dp(5), 0, dp(3));

        Button previous = createArrowButton("‹");
        previous.setOnClickListener(v -> changeExercise(-1));

        playerRow.addView(previous);

        LinearLayout center = new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setGravity(Gravity.CENTER);
        center.setPadding(dp(3), 0, dp(3), 0);

        workoutName = createText("", 18, WHITE, true);
        workoutName.setGravity(Gravity.CENTER);
        center.addView(workoutName);

        workoutDetails = createText("", 11, MUTED, false);
        workoutDetails.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams detailsParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        detailsParams.topMargin = dp(2);
        center.addView(workoutDetails, detailsParams);

        LinearLayout statsRow = new LinearLayout(this);
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        statsRow.setGravity(Gravity.CENTER);
        statsRow.setPadding(0, dp(5), 0, dp(5));

        workoutReps = createText("12 reps", 12, WHITE, true);
        workoutReps.setGravity(Gravity.CENTER);

        workoutSets = createText("0 sets", 12, WHITE, true);
        workoutSets.setGravity(Gravity.CENTER);

        statsRow.addView(workoutReps);
        statsRow.addView(createText("  |  ", 12, MUTED, false));
        statsRow.addView(workoutSets);

        center.addView(statsRow);

        LinearLayout controlsRow = new LinearLayout(this);
        controlsRow.setOrientation(LinearLayout.HORIZONTAL);
        controlsRow.setGravity(Gravity.CENTER);

        setButton = createSquareActionButton("S");
        setButton.setOnClickListener(v -> incrementSets());

        checkButton = createSquareActionButton("✔");
        checkButton.setTextSize(15);
        checkButton.setOnClickListener(v -> completeCurrentExercise());

        LinearLayout.LayoutParams actionParams =
                new LinearLayout.LayoutParams(dp(36), dp(36));

        actionParams.setMargins(dp(4), 0, dp(4), 0);

        controlsRow.addView(setButton, actionParams);
        controlsRow.addView(checkButton, actionParams);

        LinearLayout.LayoutParams controlsParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        controlsParams.topMargin = dp(1);
        center.addView(controlsRow, controlsParams);

        playerRow.addView(center, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        LinearLayout weightLayout = new LinearLayout(this);
        weightLayout.setOrientation(LinearLayout.VERTICAL);
        weightLayout.setGravity(Gravity.CENTER);
        weightLayout.setPadding(dp(3), 0, dp(3), 0);

        workoutWeight = createText("--", 19, WHITE, true);
        workoutWeight.setGravity(Gravity.CENTER);

        workoutWeightUnit = createText(
                getPreferredWeightUnit(), 9, MUTED, true
        );
        workoutWeightUnit.setGravity(Gravity.CENTER);

        weightLayout.addView(workoutWeight);
        weightLayout.addView(workoutWeightUnit);

        playerRow.addView(weightLayout);

        Button next = createArrowButton("›");
        next.setOnClickListener(v -> changeExercise(1));

        playerRow.addView(next);

        card.addView(playerRow);
        dashboardContainer.addView(card);

        updateWorkoutDisplay();
    }

    private void changeExercise(int direction) {
        if (todayExerciseEntries.isEmpty()
                || currentExercise == -1) return;

        currentExercise += direction;

        if (currentExercise < 0) {
            currentExercise = todayExerciseEntries.size() - 1;
        }

        if (currentExercise >= todayExerciseEntries.size()) {
            currentExercise = 0;
        }

        updateWorkoutDisplay();
    }

    private void updateWorkoutDisplay() {
        String preferredUnit = getPreferredWeightUnit();

        workoutWeightUnit.setText(preferredUnit);

        if (todayExerciseEntries.isEmpty()) {
            workoutName.setText("No workout scheduled");

            workoutDetails.setText(
                    "Add a workout in My Routine to see it here."
            );

            workoutWeight.setText("--");
            workoutReps.setText("-- reps");
            workoutSets.setText("-- sets");

            checkButton.setEnabled(false);
            setButton.setEnabled(false);
            return;
        }

        if (currentExercise == -1) {
            workoutName.setText("All workouts completed!");
            workoutDetails.setText("Great work today!");

            workoutWeight.setText("--");
            workoutReps.setText("-- reps");
            workoutSets.setText("-- sets");

            checkButton.setEnabled(false);
            setButton.setEnabled(false);
            return;
        }

        ExerciseEntry entry =
                todayExerciseEntries.get(currentExercise);

        workoutName.setText(entry.name);

        workoutDetails.setText(
                "Exercise " + (currentExercise + 1)
                        + " of " + todayExerciseEntries.size()
        );

        workoutReps.setText(getExerciseReps(entry) + " reps");
        workoutSets.setText(getExerciseSets(entry) + " sets");

        String key = weightKey(entry.name);

        String savedWeight =
                weightPreferences.getString(key, "--");

        String savedUnit =
                weightPreferences.getString("unit_" + key, "KG");

        String displayWeight = savedWeight;

        if (!savedWeight.equals("--") && !savedWeight.isEmpty()
                && !savedUnit.equalsIgnoreCase(preferredUnit)) {

            try {
                double weight = Double.parseDouble(savedWeight);

                if (savedUnit.equalsIgnoreCase("KG")
                        && preferredUnit.equalsIgnoreCase("LB")) {

                    weight *= KG_TO_LB;

                } else if (savedUnit.equalsIgnoreCase("LB")
                        && preferredUnit.equalsIgnoreCase("KG")) {

                    weight /= KG_TO_LB;
                }

                displayWeight = String.valueOf(Math.round(weight));

            } catch (NumberFormatException ignored) {
                displayWeight = savedWeight;
            }
        }

        workoutWeight.setText(displayWeight);
        workoutWeightUnit.setText(preferredUnit);

        boolean completed = isExerciseCompleted(entry);

        checkButton.setEnabled(!completed);
        setButton.setEnabled(!completed);

        checkButton.setAlpha(completed ? 0.4f : 1f);
        setButton.setAlpha(completed ? 0.4f : 1f);
    }

    private void incrementSets() {
        if (currentExercise < 0
                || currentExercise >= todayExerciseEntries.size()) return;

        ExerciseEntry entry =
                todayExerciseEntries.get(currentExercise);

        if (isExerciseCompleted(entry)) return;

        int sets = getExerciseSets(entry) + 1;

        weightPreferences.edit()
                .putInt(exercisePreferenceKey("sets", entry), sets)
                .apply();

        updateWorkoutDisplay();
    }

    private void completeCurrentExercise() {
        if (currentExercise < 0
                || currentExercise >= todayExerciseEntries.size()) return;

        int completedIndex = currentExercise;

        ExerciseEntry entry =
                todayExerciseEntries.get(completedIndex);

        if (isExerciseCompleted(entry)) return;

        weightPreferences.edit()
                .putBoolean(exercisePreferenceKey("done", entry), true)
                .apply();

        boolean sessionComplete = true;

        for (ExerciseEntry other : todayExerciseEntries) {
            if (other.sessionKey.equals(entry.sessionKey)
                    && !isExerciseCompleted(other)) {

                sessionComplete = false;
                break;
            }
        }

        if (sessionComplete) {
            saveCompletedWorkout(entry.sessionKey);
        }

        currentExercise =
                findNextUncompletedExercise(completedIndex);

        updateWorkoutDisplay();

        Toast.makeText(
                this,
                sessionComplete
                        ? "Scheduled workout completed!"
                        : "Exercise completed!",
                Toast.LENGTH_SHORT
        ).show();

        if (currentExercise == -1) {
            Toast.makeText(
                    this,
                    "All today's exercises completed!",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =================================================
    // FIREBASE COMPLETED WORKOUT SAVING
    // =================================================

    private void saveCompletedWorkout(String sessionKey) {
        if (weightPreferences.getBoolean(
                "uploaded_" + sessionKey, false)) {
            return;
        }

        Map<String, Object> workoutData = new HashMap<>();
        workoutData.put("sessionId", sessionKey);
        workoutData.put("date", formatDate(System.currentTimeMillis()));
        workoutData.put("completionTime", System.currentTimeMillis());
        workoutData.put("completed", true);

        ArrayList<Map<String, Object>> exercises = new ArrayList<>();

        for (ExerciseEntry entry : todayExerciseEntries) {
            if (!entry.sessionKey.equals(sessionKey)) continue;

            Map<String, Object> exerciseData = new HashMap<>();

            exerciseData.put("name", entry.name);
            exerciseData.put("exerciseIndex", entry.exerciseIndex);
            exerciseData.put("reps", getExerciseReps(entry));
            exerciseData.put("sets", getExerciseSets(entry));

            String key = weightKey(entry.name);

            exerciseData.put(
                    "weight",
                    weightPreferences.getString(key, "--")
            );

            exerciseData.put(
                    "weightUnit",
                    weightPreferences.getString("unit_" + key, "KG")
            );

            exerciseData.put("completed", true);

            exercises.add(exerciseData);
        }

        workoutData.put("exercises", exercises);

        completedWorkoutsRef.child(sessionKey).setValue(workoutData)
                .addOnSuccessListener(unused -> {
                    weightPreferences.edit()
                            .putBoolean("uploaded_" + sessionKey, true)
                            .apply();

                    Toast.makeText(
                            MainActivity.this,
                            "Workout saved to Firebase!",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(error ->
                        Toast.makeText(
                                MainActivity.this,
                                "Firebase save failed: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    // =================================================
    // WORKOUT MENU AND EDIT DIALOGS
    // =================================================

    private void showWorkoutMenu(View anchor) {
        if (todayExerciseEntries.isEmpty() || currentExercise < 0) {
            Toast.makeText(
                    this,
                    "There is no exercise to edit yet.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        PopupMenu popup = new PopupMenu(
                new android.view.ContextThemeWrapper(
                        this,
                        androidx.appcompat.R.style.Theme_AppCompat
                ),
                anchor
        );

        popup.getMenu().add("Edit Weight");
        popup.getMenu().add("Edit Reps");
        popup.getMenu().add("Reset Reps");
        popup.getMenu().add("Reset Sets");
        popup.getMenu().add("Reset Weight");

        popup.setOnMenuItemClickListener(item -> {
            String selected = item.getTitle().toString();

            ExerciseEntry entry =
                    todayExerciseEntries.get(currentExercise);

            if (selected.equals("Edit Weight")) {
                showEditWeightDialog();

            } else if (selected.equals("Edit Reps")) {
                showEditRepsDialog();

            } else if (selected.equals("Reset Reps")) {
                weightPreferences.edit()
                        .remove(exercisePreferenceKey("reps", entry))
                        .apply();

                updateWorkoutDisplay();

                Toast.makeText(
                        this,
                        "Reps reset to zero.",
                        Toast.LENGTH_SHORT
                ).show();

            } else if (selected.equals("Reset Sets")) {
                weightPreferences.edit()
                        .remove(exercisePreferenceKey("sets", entry))
                        .apply();

                updateWorkoutDisplay();

                Toast.makeText(
                        this,
                        "Sets reset to zero.",
                        Toast.LENGTH_SHORT
                ).show();

            } else if (selected.equals("Reset Weight")) {
                String key = weightKey(entry.name);

                weightPreferences.edit()
                        .remove(key)
                        .remove("unit_" + key)
                        .apply();

                updateWorkoutDisplay();

                Toast.makeText(
                        this,
                        "Weight reset.",
                        Toast.LENGTH_SHORT
                ).show();
            }

            return true;
        });

        popup.show();
    }

    private void showEditWeightDialog() {
        if (currentExercise < 0
                || currentExercise >= todayExerciseEntries.size()) return;

        String exercise =
                todayExerciseEntries.get(currentExercise).name;

        String key = weightKey(exercise);

        String currentWeight =
                weightPreferences.getString(key, "");

        String savedUnit =
                weightPreferences.getString("unit_" + key, "KG");

        String preferredUnit = getPreferredWeightUnit();

        if (!currentWeight.isEmpty() && !currentWeight.equals("--")
                && !savedUnit.equalsIgnoreCase(preferredUnit)) {

            try {
                double value = Double.parseDouble(currentWeight);

                if (savedUnit.equalsIgnoreCase("KG")
                        && preferredUnit.equalsIgnoreCase("LB")) {

                    value *= KG_TO_LB;

                } else if (savedUnit.equalsIgnoreCase("LB")
                        && preferredUnit.equalsIgnoreCase("KG")) {

                    value /= KG_TO_LB;
                }

                currentWeight = String.valueOf(Math.round(value));

            } catch (NumberFormatException ignored) {
            }
        }

        EditText input = createDialogInput(
                "Enter weight in " + preferredUnit
        );

        input.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        input.setText(
                currentWeight.equals("--") ? "" : currentWeight
        );

        AlertDialog dialog = new AlertDialog.Builder(
                new android.view.ContextThemeWrapper(
                        this,
                        androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert
                )
        )
                .setTitle("Edit Weight")
                .setMessage("Weight for " + exercise)
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, which) -> {
                    String value =
                            input.getText().toString().trim();

                    try {
                        double weight = Double.parseDouble(value);

                        if (weight < 0) {
                            throw new NumberFormatException();
                        }

                        weightPreferences.edit()
                                .putString(key, value)
                                .putString("unit_" + key, preferredUnit)
                                .apply();

                        updateWorkoutDisplay();

                    } catch (NumberFormatException e) {
                        Toast.makeText(
                                this,
                                "Enter a valid weight.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .create();

        styleDarkDialog(dialog, input);
        dialog.show();
    }

    private void showEditRepsDialog() {
        if (currentExercise < 0
                || currentExercise >= todayExerciseEntries.size()) return;

        ExerciseEntry entry =
                todayExerciseEntries.get(currentExercise);

        EditText input = createDialogInput("Enter reps");

        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(getExerciseReps(entry)));

        AlertDialog dialog = new AlertDialog.Builder(
                new android.view.ContextThemeWrapper(
                        this,
                        androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert
                )
        )
                .setTitle("Edit Reps")
                .setMessage("Reps for " + entry.name)
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, which) -> {
                    try {
                        int reps = Integer.parseInt(
                                input.getText().toString().trim()
                        );

                        if (reps <= 0) {
                            throw new NumberFormatException();
                        }

                        weightPreferences.edit()
                                .putInt(
                                        exercisePreferenceKey("reps", entry),
                                        reps
                                )
                                .apply();

                        updateWorkoutDisplay();

                    } catch (NumberFormatException e) {
                        Toast.makeText(
                                this,
                                "Enter a valid number of reps.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .create();

        styleDarkDialog(dialog, input);
        dialog.show();
    }

    private EditText createDialogInput(String hint) {
        EditText input = new EditText(this);

        input.setHint(hint);
        input.setHintTextColor(MUTED);
        input.setTextColor(Color.WHITE);
        input.setSelectAllOnFocus(true);
        input.setPadding(dp(16), dp(12), dp(16), dp(12));

        return input;
    }

    private void styleDarkDialog(
            AlertDialog dialog, EditText input) {

        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(
                        makeBackground(CARD_COLOR, 16)
                );
            }

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(Color.WHITE);

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(Color.WHITE);

            TextView message =
                    dialog.findViewById(android.R.id.message);

            if (message != null) {
                message.setTextColor(Color.rgb(200, 205, 215));
            }

            input.setTextColor(Color.WHITE);
            input.setHintTextColor(MUTED);
        });
    }

    private String getPreferredWeightUnit() {
        SharedPreferences settings = getSharedPreferences(
                "UserSettings", MODE_PRIVATE
        );

        return settings.getString("weightUnit", "KG");
    }

    private String weightKey(String exercise) {
        return "weight_" + exercise.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "_");
    }

    // =================================================
    // WEEKLY ACTIVITY AND UPCOMING ROUTINE
    // =================================================

    private void addWeeklyAndUpcomingRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);

        LinearLayout weeklyCard = createCard();
        LinearLayout upcomingCard = createCard();

        int cardHeight = dp(155);

        weeklyCard.setMinimumHeight(cardHeight);
        upcomingCard.setMinimumHeight(cardHeight);

        LinearLayout.LayoutParams weeklyParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        weeklyParams.setMargins(0, 0, dp(7), dp(14));

        LinearLayout.LayoutParams upcomingParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        upcomingParams.setMargins(dp(7), 0, 0, dp(14));

        weeklyCard.setLayoutParams(weeklyParams);
        upcomingCard.setLayoutParams(upcomingParams);

        addWeeklyActivityContent(weeklyCard);
        addUpcomingRoutineContent(upcomingCard);

        row.addView(weeklyCard);
        row.addView(upcomingCard);

        dashboardContainer.addView(row);
    }

    private void addWeeklyActivityContent(LinearLayout card) {
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(WEEKLY_PLACEMENT);
        titleRow.setPadding(0, 0, 0, dp(8));

        addTitleIcon(
                titleRow,
                R.drawable.img_weekly_activity,
                "Weekly activity icon",
                WEEKLY_WIDTH,
                WEEKLY_HEIGHT,
                WEEKLY_LEFT,
                WEEKLY_TOP,
                WEEKLY_RIGHT,
                WEEKLY_BOTTOM
        );

        titleRow.addView(createText(
                "Weekly Activity", 15, WHITE, true
        ));

        card.addView(titleRow);

        weeklyNumber = createText("0", 29, WHITE, true);

        weeklyDescription = createText(
                "Scheduled workouts", 12, MUTED, false
        );

        card.addView(weeklyNumber);
        card.addView(weeklyDescription);

        updateWeeklyActivity();

        card.setOnClickListener(
                v -> openActivity(Option3Activity.class)
        );
    }

    private void updateWeeklyActivity() {
        Calendar calendar = Calendar.getInstance();

        int today = calendar.get(Calendar.DAY_OF_WEEK);
        int daysSinceMonday = (today + 5) % 7;

        calendar.add(Calendar.DAY_OF_MONTH, -daysSinceMonday);

        int totalSessions = 0;

        for (int i = 0; i < 7; i++) {
            String date = formatDate(calendar.getTimeInMillis());

            JSONArray sessions =
                    RoutineStorage.getSessionsForDate(this, date);

            totalSessions += sessions.length();

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        weeklyNumber.setText(String.valueOf(totalSessions));

        weeklyDescription.setText(
                totalSessions == 1
                        ? "Workout scheduled this week"
                        : "Workouts scheduled this week"
        );
    }

    private void addUpcomingRoutineContent(LinearLayout card) {
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(UPCOMING_PLACEMENT);
        titleRow.setPadding(0, 0, 0, dp(6));

        addTitleIcon(
                titleRow,
                R.drawable.img_upcoming_routine,
                "Upcoming routine icon",
                UPCOMING_WIDTH,
                UPCOMING_HEIGHT,
                UPCOMING_LEFT,
                UPCOMING_TOP,
                UPCOMING_RIGHT,
                UPCOMING_BOTTOM
        );

        titleRow.addView(createText(
                "Upcoming Routine", 15, WHITE, true
        ));

        card.addView(titleRow);

        upcomingDescription = createText("", 12, MUTED, false);

        card.addView(upcomingDescription);

        updateUpcomingRoutine();

        card.setOnClickListener(
                v -> openActivity(Option3Activity.class)
        );
    }

    private void updateUpcomingRoutine() {
        Calendar searchDate = Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        JSONObject nextSession = null;
        String nextDate = null;

        for (int day = 0; day <= 365; day++) {
            String date = formatDate(searchDate.getTimeInMillis());

            JSONArray sessions =
                    RoutineStorage.getSessionsForDate(this, date);

            for (int i = 0; i < sessions.length(); i++) {
                JSONObject session = sessions.optJSONObject(i);

                if (session == null) continue;

                Calendar sessionDateTime = createSessionDateTime(
                        date,
                        session.optString("time", "")
                );

                if (sessionDateTime != null
                        && sessionDateTime.after(now)) {

                    nextSession = session;
                    nextDate = date;
                    break;
                }
            }

            if (nextSession != null) break;

            searchDate.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (nextSession == null) {
            upcomingDescription.setText(
                    "No upcoming workouts scheduled.\n"
                            + "Add a session in My Routine."
            );
            return;
        }

        JSONArray exercises =
                nextSession.optJSONArray("exercises");

        int count = exercises == null ? 0 : exercises.length();

        upcomingDescription.setText(
                formatReadableDate(nextDate)
                        + "\n" + nextSession.optString("time", "")
                        + "\n" + count
                        + (count == 1 ? " exercise" : " exercises")
        );
    }

    private Calendar createSessionDateTime(
            String date, String time) {

        try {
            SimpleDateFormat dateFormat =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            dateFormat.setLenient(false);

            Date parsedDate = dateFormat.parse(date);

            if (parsedDate == null) return null;

            SimpleDateFormat timeFormat =
                    new SimpleDateFormat("h:mm a", Locale.getDefault());

            timeFormat.setLenient(false);

            Date parsedTime = timeFormat.parse(time);

            if (parsedTime == null) {
                timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
                parsedTime = timeFormat.parse(time);
            }

            if (parsedTime == null) return null;

            Calendar result = Calendar.getInstance();
            Calendar timeCalendar = Calendar.getInstance();

            timeCalendar.setTime(parsedTime);
            result.setTime(parsedDate);

            result.set(Calendar.HOUR_OF_DAY,
                    timeCalendar.get(Calendar.HOUR_OF_DAY));

            result.set(Calendar.MINUTE,
                    timeCalendar.get(Calendar.MINUTE));

            result.set(Calendar.SECOND, 0);
            result.set(Calendar.MILLISECOND, 0);

            return result;

        } catch (Exception e) {
            return null;
        }
    }

    // =================================================
    // HOME PROGRESS CARD AND GRAPHS
    // =================================================

    private void addProgressWidget() {
        LinearLayout card = createCard();

        addProgressContent(card);

        dashboardContainer.addView(card);
    }

    private void addProgressContent(LinearLayout card) {
        progressPage = 0;

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);

        titleRow.setGravity(
                Gravity.START | Gravity.CENTER_VERTICAL
        );

        titleRow.setPadding(0, 0, 0, dp(8));

        addTitleIcon(
                titleRow,
                R.drawable.img_progress,
                "Progress icon",
                PROGRESS_WIDTH,
                PROGRESS_HEIGHT,
                PROGRESS_LEFT,
                PROGRESS_TOP,
                PROGRESS_RIGHT,
                PROGRESS_BOTTOM
        );

        titleRow.addView(createText("Progress", 17, WHITE, true));

        card.addView(titleRow);

        homeProgressHeading = createText(
                "Completed Workouts", 14, WHITE, true
        );

        homeProgressHeading.setPadding(0, dp(12), 0, dp(8));

        card.addView(homeProgressHeading);

        homeProgressGraph = new ProgressGraphView(this);

        LinearLayout.LayoutParams graphParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(185)
                );

        homeProgressGraph.setLayoutParams(graphParams);

        card.addView(homeProgressGraph);

        homeProgressHint = createText(
                "↑ Swipe for goals", 11, MUTED, true
        );

        homeProgressHint.setGravity(Gravity.CENTER);
        homeProgressHint.setPadding(0, dp(10), 0, 0);

        card.addView(homeProgressHint);

        card.setOnClickListener(
                v -> openActivity(Option2Activity.class)
        );

        homeProgressGraph.setOnTouchListener(
                new View.OnTouchListener() {

                    private float startX;
                    private float startY;

                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        switch (event.getAction()) {

                            case MotionEvent.ACTION_DOWN:
                                startX = event.getX();
                                startY = event.getY();

                                view.getParent()
                                        .requestDisallowInterceptTouchEvent(true);

                                return true;

                            case MotionEvent.ACTION_MOVE:
                                view.getParent()
                                        .requestDisallowInterceptTouchEvent(true);

                                return true;

                            case MotionEvent.ACTION_UP:
                                float differenceY =
                                        event.getY() - startY;

                                float differenceX =
                                        event.getX() - startX;

                                view.getParent()
                                        .requestDisallowInterceptTouchEvent(false);

                                if (Math.abs(differenceY) > dp(25)
                                        && Math.abs(differenceY)
                                        > Math.abs(differenceX)) {

                                    progressPage =
                                            differenceY < 0 ? 1 : 0;

                                    updateHomeProgressPage();

                                } else if (Math.abs(differenceY) < dp(10)
                                        && Math.abs(differenceX) < dp(10)) {

                                    card.performClick();
                                }

                                return true;

                            case MotionEvent.ACTION_CANCEL:
                                view.getParent()
                                        .requestDisallowInterceptTouchEvent(false);

                                return true;
                        }

                        return true;
                    }
                }
        );

        loadHomeProgressData();
    }

    private void updateHomeProgressPage() {
        if (progressPage == 0) {
            homeProgressHeading.setText("Completed Workouts");
            homeProgressHint.setText("↑ Swipe for goals");
        } else {
            homeProgressHeading.setText("Goal Progress");
            homeProgressHint.setText("↓ Swipe for workouts");
        }

        loadHomeProgressData();
    }

    private void loadHomeProgressData() {
        if (homeProgressGraph == null) return;

        completedWorkoutsRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        ArrayList<HomeCompletedSession> sessions =
                                new ArrayList<>();

                        for (DataSnapshot sessionSnapshot
                                : snapshot.getChildren()) {

                            Boolean completed = sessionSnapshot
                                    .child("completed")
                                    .getValue(Boolean.class);

                            if (completed != null && !completed) continue;

                            String date = sessionSnapshot.child("date")
                                    .getValue(String.class);

                            if (date == null || date.trim().isEmpty()) {
                                continue;
                            }

                            HomeCompletedSession session =
                                    new HomeCompletedSession();

                            session.date = date;

                            for (DataSnapshot exerciseSnapshot
                                    : sessionSnapshot.child("exercises")
                                    .getChildren()) {

                                HomeExerciseRecord exercise =
                                        new HomeExerciseRecord();

                                exercise.name = exerciseSnapshot
                                        .child("name")
                                        .getValue(String.class);

                                exercise.reps = readHomeNumber(
                                        exerciseSnapshot.child("reps")
                                );

                                Object weight = exerciseSnapshot
                                        .child("weight").getValue();

                                exercise.weight = weight == null
                                        ? null : weight.toString();

                                exercise.weightUnit = exerciseSnapshot
                                        .child("weightUnit")
                                        .getValue(String.class);

                                if (exercise.name != null) {
                                    session.exercises.add(exercise);
                                }
                            }

                            sessions.add(session);
                        }

                        Collections.sort(
                                sessions,
                                Comparator.comparingLong(
                                        s -> parseHomeDate(s.date)
                                )
                        );

                        if (progressPage == 0) {
                            showHomeWeeklyWorkoutGraph(sessions);
                        } else {
                            showHomeGoalGraph(sessions);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        homeProgressGraph.setMessage(
                                "Could not load progress data."
                        );
                    }
                }
        );
    }

    private int readHomeNumber(DataSnapshot snapshot) {
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

    private long parseHomeDate(String date) {
        try {
            SimpleDateFormat format =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            Date parsed = format.parse(date);

            return parsed == null ? 0 : parsed.getTime();

        } catch (Exception e) {
            return 0;
        }
    }

    private void showHomeWeeklyWorkoutGraph(
            ArrayList<HomeCompletedSession> sessions) {

        String[] labels = new String[7];
        float[] values = new float[7];

        Calendar weekStart = Calendar.getInstance();

        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        weekStart.set(Calendar.HOUR_OF_DAY, 0);
        weekStart.set(Calendar.MINUTE, 0);
        weekStart.set(Calendar.SECOND, 0);
        weekStart.set(Calendar.MILLISECOND, 0);

        weekStart.add(Calendar.WEEK_OF_YEAR, -6);

        SimpleDateFormat labelFormat =
                new SimpleDateFormat("MMM d", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            labels[i] = labelFormat.format(weekStart.getTime());

            weekStart.add(Calendar.WEEK_OF_YEAR, 1);
        }

        Calendar firstWeek = Calendar.getInstance();

        firstWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        firstWeek.set(Calendar.HOUR_OF_DAY, 0);
        firstWeek.set(Calendar.MINUTE, 0);
        firstWeek.set(Calendar.SECOND, 0);
        firstWeek.set(Calendar.MILLISECOND, 0);

        firstWeek.add(Calendar.WEEK_OF_YEAR, -6);

        long firstWeekMillis = firstWeek.getTimeInMillis();

        for (HomeCompletedSession session : sessions) {
            long sessionMillis = parseHomeDate(session.date);

            if (sessionMillis < firstWeekMillis) continue;

            Calendar sessionCalendar = Calendar.getInstance();

            sessionCalendar.setTimeInMillis(sessionMillis);
            sessionCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            sessionCalendar.set(Calendar.HOUR_OF_DAY, 0);
            sessionCalendar.set(Calendar.MINUTE, 0);
            sessionCalendar.set(Calendar.SECOND, 0);
            sessionCalendar.set(Calendar.MILLISECOND, 0);

            long difference = sessionCalendar.getTimeInMillis()
                    - firstWeekMillis;

            int index = (int) (difference
                    / (7L * 24L * 60L * 60L * 1000L));

            if (index >= 0 && index < 7) {
                values[index]++;
            }
        }

        homeProgressGraph.setData(
                labels, values, 0, true, "Workouts", false
        );
    }

    private void showHomeGoalGraph(
            ArrayList<HomeCompletedSession> sessions) {

        SharedPreferences goalPreferences = getSharedPreferences(
                PREF_GOALS, MODE_PRIVATE
        );

        String selectedWorkout = goalPreferences.getString(
                KEY_HOME_WORKOUT, ""
        );

        int goalType = goalPreferences.getInt(
                KEY_HOME_GOAL_TYPE, 0
        );

        if (selectedWorkout == null
                || selectedWorkout.trim().isEmpty()) {

            homeProgressGraph.setMessage(
                    "Select a workout in Progress to see its goal."
            );

            return;
        }

        String workoutKey = selectedWorkout.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "_");

        String goalKey = workoutKey
                + (goalType == 0 ? "_reps" : "_weight");

        float target = goalPreferences.getFloat(goalKey, 0);

        if (target <= 0) {
            homeProgressGraph.setMessage(
                    "Set a goal for " + selectedWorkout
                            + " in the Progress section."
            );

            return;
        }

        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Float> values = new ArrayList<>();

        for (HomeCompletedSession session : sessions) {
            for (HomeExerciseRecord exercise : session.exercises) {

                if (exercise.name == null
                        || !exercise.name.equalsIgnoreCase(selectedWorkout)) {
                    continue;
                }

                if (goalType == 0) {
                    if (exercise.reps <= 0) continue;

                    values.add((float) exercise.reps);

                } else {
                    Float weight = parseHomeWeightKg(
                            exercise.weight,
                            exercise.weightUnit
                    );

                    if (weight == null) continue;

                    values.add(weight);
                }

                labels.add(shortHomeDate(session.date));
            }
        }

        String unit = goalType == 0 ? "Reps" : "kg";

        if (values.isEmpty()) {
            homeProgressGraph.setMessage(
                    "No completed history found for "
                            + selectedWorkout + "."
            );

            return;
        }

        homeProgressGraph.setData(
                labels.toArray(new String[0]),
                toHomeFloatArray(values),
                target,
                false,
                unit,
                true
        );
    }

    private Float parseHomeWeightKg(String text, String unit) {
        if (text == null || text.trim().isEmpty()
                || text.equals("--")) {
            return null;
        }

        try {
            float value = Float.parseFloat(
                    text.replace(",", ".").trim()
            );

            if (value <= 0) return null;

            if (unit != null && (unit.equalsIgnoreCase("LB")
                    || unit.equalsIgnoreCase("LBS"))) {

                value *= 0.45359237f;
            }

            return value;

        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String shortHomeDate(String date) {
        try {
            SimpleDateFormat input =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            SimpleDateFormat output =
                    new SimpleDateFormat("MM/dd", Locale.US);

            Date parsed = input.parse(date);

            return parsed == null ? date : output.format(parsed);

        } catch (Exception e) {
            return date;
        }
    }

    private float[] toHomeFloatArray(ArrayList<Float> values) {
        float[] result = new float[values.size()];

        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }

        return result;
    }

    // =================================================
    // HOME GRAPH VIEW
    // =================================================

    private class ProgressGraphView extends View {

        private final Paint paint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        private String[] labels = new String[0];
        private float[] values = new float[0];

        private float target = 0;

        private boolean barChart = false;
        private boolean showTarget = false;

        private String unit = "";
        private String message = "";

        private final int GRAPH_COLOR =
                Color.rgb(240, 107, 120);

        private final int GOAL_COLOR =
                Color.rgb(255, 193, 7);

        private final int GRID_COLOR =
                Color.rgb(65, 70, 80);

        private static final int GRID_INTERVALS = 4;

        ProgressGraphView(android.content.Context context) {
            super(context);

            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        void setData(
                String[] labels,
                float[] values,
                float target,
                boolean barChart,
                String unit,
                boolean showTarget) {

            this.labels = labels == null
                    ? new String[0] : labels;

            this.values = values == null
                    ? new float[0] : values;

            this.target = target;
            this.barChart = barChart;

            this.unit = unit == null ? "" : unit;

            this.showTarget = showTarget;
            this.message = "";

            invalidate();
        }

        void setMessage(String message) {
            this.message = message == null ? "" : message;

            this.labels = new String[0];
            this.values = new float[0];

            invalidate();
        }

        private float calculateAxisMaximum(float rawMax) {
            if (barChart) {
                return Math.max(
                        4f,
                        (float) Math.ceil(rawMax / 4f) * 4f
                );
            }

            if (rawMax <= 0) return 1f;

            double magnitude = Math.pow(
                    10,
                    Math.floor(Math.log10(rawMax))
            );

            double normalized = rawMax / magnitude;
            double niceNumber;

            if (normalized <= 1) {
                niceNumber = 1;
            } else if (normalized <= 2) {
                niceNumber = 2;
            } else if (normalized <= 2.5) {
                niceNumber = 2.5;
            } else if (normalized <= 5) {
                niceNumber = 5;
            } else {
                niceNumber = 10;
            }

            return (float) (niceNumber * magnitude);
        }

        private float getBarCenter(
                int index, float left, float chartWidth) {

            float slot = chartWidth / values.length;

            return left + slot * index + slot / 2f;
        }

        private float getLineX(
                int index, float left, float right) {

            if (values.length == 1) {
                return (left + right) / 2f;
            }

            float slot = (right - left) / (values.length - 1);

            return left + slot * index;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float width = getWidth();
            float height = getHeight();

            if (!message.isEmpty()) {
                drawCenteredMessage(
                        canvas, message, width, height
                );
                return;
            }

            if (values.length == 0) {
                drawCenteredMessage(
                        canvas,
                        "No workout data yet",
                        width,
                        height
                );
                return;
            }

            float left = dp(42);
            float right = width - dp(14);
            float top = dp(20);
            float bottom = height - dp(38);

            float chartWidth = right - left;
            float chartHeight = bottom - top;

            if (chartWidth <= 0 || chartHeight <= 0) return;

            float rawMax = 0;

            for (float value : values) {
                rawMax = Math.max(rawMax, value);
            }

            if (showTarget) {
                rawMax = Math.max(rawMax, target);
            }

            float axisMax = calculateAxisMaximum(rawMax);

            drawGridAndYAxis(
                    canvas,
                    left,
                    right,
                    top,
                    bottom,
                    chartHeight,
                    axisMax
            );

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(MUTED);

            canvas.drawLine(left, top, left, bottom, paint);
            canvas.drawLine(left, bottom, right, bottom, paint);

            if (showTarget && target > 0) {
                drawTargetLine(
                        canvas,
                        left,
                        right,
                        top,
                        bottom,
                        chartHeight,
                        axisMax
                );
            }

            if (barChart) {
                drawBars(
                        canvas,
                        left,
                        right,
                        bottom,
                        chartWidth,
                        chartHeight,
                        axisMax,
                        height
                );
            } else {
                drawLineGraph(
                        canvas,
                        left,
                        right,
                        bottom,
                        chartHeight,
                        axisMax,
                        height
                );
            }
        }

        private void drawCenteredMessage(
                Canvas canvas,
                String text,
                float width,
                float height) {

            paint.setColor(MUTED);
            paint.setTextSize(dp(12));
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setStyle(Paint.Style.FILL);

            canvas.drawText(
                    text,
                    width / 2f,
                    height / 2f,
                    paint
            );
        }

        private void drawGridAndYAxis(
                Canvas canvas,
                float left,
                float right,
                float top,
                float bottom,
                float chartHeight,
                float axisMax) {

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(GRID_COLOR);

            for (int i = 0; i <= GRID_INTERVALS; i++) {
                float y = top
                        + chartHeight * i / GRID_INTERVALS;

                canvas.drawLine(left, y, right, y, paint);
            }

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(MUTED);
            paint.setTextSize(dp(9));
            paint.setTextAlign(Paint.Align.RIGHT);

            for (int i = 0; i <= GRID_INTERVALS; i++) {
                float value = axisMax
                        - axisMax * i / GRID_INTERVALS;

                float y = top
                        + chartHeight * i / GRID_INTERVALS;

                String label;

                if (barChart) {
                    label = String.valueOf(Math.round(value));
                } else {
                    label = String.format(
                            Locale.US, "%.1f", value
                    );
                }

                canvas.drawText(
                        label,
                        left - dp(7),
                        y + dp(3),
                        paint
                );
            }
        }

        private void drawTargetLine(
                Canvas canvas,
                float left,
                float right,
                float top,
                float bottom,
                float chartHeight,
                float axisMax) {

            float targetY = bottom
                    - (target / axisMax) * chartHeight;

            paint.setColor(GOAL_COLOR);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));

            paint.setPathEffect(
                    new DashPathEffect(
                            new float[]{dp(6), dp(4)}, 0
                    )
            );

            canvas.drawLine(
                    left, targetY, right, targetY, paint
            );

            paint.setPathEffect(null);
        }

        private void drawBars(
                Canvas canvas,
                float left,
                float right,
                float bottom,
                float chartWidth,
                float chartHeight,
                float axisMax,
                float height) {

            float slot = chartWidth / values.length;

            float barWidth = Math.min(
                    dp(22), slot * 0.55f
            );

            for (int i = 0; i < values.length; i++) {
                float x = getBarCenter(
                        i, left, chartWidth
                );

                float barHeight =
                        (values[i] / axisMax) * chartHeight;

                paint.setStyle(Paint.Style.FILL);
                paint.setColor(GRAPH_COLOR);

                canvas.drawRoundRect(
                        x - barWidth / 2f,
                        bottom - barHeight,
                        x + barWidth / 2f,
                        bottom,
                        dp(4),
                        dp(4),
                        paint
                );

                drawXLabel(canvas, labels[i], x, height);
            }
        }

        private void drawLineGraph(
                Canvas canvas,
                float left,
                float right,
                float bottom,
                float chartHeight,
                float axisMax,
                float height) {

            Path path = new Path();

            for (int i = 0; i < values.length; i++) {
                float x = getLineX(i, left, right);

                float y = bottom
                        - (values[i] / axisMax) * chartHeight;

                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }

            paint.setColor(GRAPH_COLOR);
            paint.setStrokeWidth(dp(2.5f));
            paint.setStyle(Paint.Style.STROKE);
            paint.setPathEffect(null);

            canvas.drawPath(path, paint);

            for (int i = 0; i < values.length; i++) {
                float x = getLineX(i, left, right);

                float y = bottom
                        - (values[i] / axisMax) * chartHeight;

                paint.setStyle(Paint.Style.FILL);
                paint.setColor(GRAPH_COLOR);

                canvas.drawCircle(x, y, dp(4), paint);

                if (values.length <= 8
                        || i == 0
                        || i == values.length - 1
                        || i % Math.max(1, values.length / 5) == 0) {

                    drawXLabel(canvas, labels[i], x, height);
                }
            }
        }

        private void drawXLabel(
                Canvas canvas,
                String label,
                float x,
                float height) {

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(MUTED);
            paint.setTextSize(dp(8));
            paint.setTextAlign(Paint.Align.CENTER);

            canvas.drawText(
                    label,
                    x,
                    height - dp(8),
                    paint
            );
        }
    }

    // =================================================
    // HEART RATE
    // =================================================

    private void addHeartRateWidget() {
        LinearLayout card = createCard();

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(0, dp(5), 0, dp(10));

        TextView title = createText(
                "Heart Rate Monitor", 16, WHITE, true
        );

        top.addView(title, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        heartRateStatus = createText(
                "WAITING", 10, MUTED, true
        );

        top.addView(heartRateStatus);

        card.addView(top);

        LinearLayout heartRateRow = new LinearLayout(this);
        heartRateRow.setOrientation(LinearLayout.HORIZONTAL);
        heartRateRow.setGravity(Gravity.CENTER);
        heartRateRow.setPadding(0, dp(8), 0, dp(8));

        ImageView heartImage = new ImageView(this);

        heartImage.setImageResource(R.drawable.img_heart_rate);

        LinearLayout.LayoutParams heartParams =
                new LinearLayout.LayoutParams(
                        dp(HEART_WIDTH),
                        dp(HEART_HEIGHT)
                );

        heartParams.setMargins(0, 0, dp(10), 0);

        heartImage.setLayoutParams(heartParams);
        heartImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        heartImage.setContentDescription("Heart rate icon");

        heartRateRow.addView(heartImage);

        heartRateText = createText(
                currentHeartRate > 0
                        ? currentHeartRate + " BPM"
                        : "-- BPM",
                29, Color.rgb(240, 107, 120), true
        );

        heartRateText.setGravity(Gravity.CENTER_VERTICAL);

        heartRateRow.addView(heartRateText);
        card.addView(heartRateRow);

        heartRateDescription = createText(
                "Waiting for heart rate measurements.",
                12,
                MUTED,
                false
        );

        heartRateDescription.setGravity(Gravity.CENTER);
        heartRateDescription.setPadding(0, dp(8), 0, 0);

        card.addView(heartRateDescription);

        card.setOnClickListener(
                v -> openActivity(HeartRateActivity.class)
        );

        dashboardContainer.addView(card);
        updateHeartRateDisplay();
    }


    // =================================================
    // DESIGN HELPERS
    // =================================================

    private void addTitleIcon(
            LinearLayout parent,
            int imageResource,
            String description,
            int width,
            int height,
            int left,
            int top,
            int right,
            int bottom) {

        ImageView image = new ImageView(this);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        dp(width),
                        dp(height)
                );

        params.setMargins(
                dp(left),
                dp(top),
                dp(right),
                dp(bottom)
        );

        image.setLayoutParams(params);
        image.setImageResource(imageResource);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setContentDescription(description);

        parent.addView(image);
    }

    private LinearLayout createCard() {
        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);

        card.setPadding(
                dp(16), dp(15), dp(16), dp(16)
        );

        card.setBackground(makeBackground(CARD_COLOR, 18));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 0, 0, dp(14));

        card.setLayoutParams(params);
        card.setClickable(true);
        card.setFocusable(true);

        return card;
    }

    private TextView createText(
            String value,
            int size,
            int color,
            boolean bold) {

        TextView text = new TextView(this);

        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);

        if (bold) {
            text.setTypeface(null, Typeface.BOLD);
        }

        return text;
    }

    private Button createArrowButton(String symbol) {
        Button button = createSmallButton(symbol);

        button.setTextSize(32);
        button.setPadding(dp(5), 0, dp(5), 0);

        return button;
    }

    private Button createSmallButton(String symbol) {
        Button button = new Button(this);

        button.setText(symbol);
        button.setTextColor(WHITE);
        button.setAllCaps(false);

        button.setMinWidth(0);
        button.setMinimumWidth(0);

        button.setBackgroundColor(Color.TRANSPARENT);

        return button;
    }

    private Button createSquareActionButton(String symbol) {
        Button button = new Button(this);

        button.setText(symbol);
        button.setTextSize(17);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);

        button.setPadding(0, 0, 0, 0);

        button.setMinWidth(0);
        button.setMinimumWidth(0);

        button.setMinHeight(0);
        button.setMinimumHeight(0);

        button.setBackground(
                makeBackground(Color.rgb(55, 62, 75), 9)
        );

        return button;
    }

    private GradientDrawable makeBackground(
            int color, int radius) {

        GradientDrawable background = new GradientDrawable();

        background.setColor(color);
        background.setCornerRadius(dp(radius));

        return background;
    }

    private int dp(int value) {
        return (int) (
                value * getResources().getDisplayMetrics().density + 0.5f
        );
    }

    private int dp(float value) {
        return (int) (
                value * getResources().getDisplayMetrics().density + 0.5f
        );
    }

    // =================================================
    // DATE HELPERS
    // =================================================

    private String formatDate(long milliseconds) {
        return new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
        ).format(new Date(milliseconds));
    }

    private String formatReadableDate(String date) {
        try {
            SimpleDateFormat input =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            SimpleDateFormat output =
                    new SimpleDateFormat(
                            "EEEE, MMMM d",
                            Locale.getDefault()
                    );

            Date parsed = input.parse(date);

            if (parsed != null) {
                return output.format(parsed);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return date;
    }

    // =================================================
    // ESP32 BLUETOOTH CALLBACKS
    // =================================================

    @Override
    public void onSample(BLEManager.SensorSample sample) {

        // Send incoming sensor data to the processing class.
        sensorProcess.processSample(sample);

        // Calculate orientation every 100 samples.
        if (sample.sequence % 100 == 0) {

            double qw = sample.qw;
            double qx = sample.qx;
            double qy = sample.qy;
            double qz = sample.qz;

            double roll = Math.toDegrees(Math.atan2(
                    2 * (qw * qx + qy * qz),
                    1 - 2 * (qx * qx + qy * qy)
            ));

            double pitch = Math.toDegrees(Math.asin(
                    Math.max(
                            -1,
                            Math.min(1, 2 * (qw * qy - qz * qx))
                    )
            ));

            // Roll and pitch are calculated here.
            // These values can later be used for workout tracking.

            // android.util.Log.d(
            //         "BarbellBLE",
            //         "Roll: " + roll + " | Pitch: " + pitch
            // );
        }
    }
    @Override
    public void onHeartRate(int bpm) {
        currentHeartRate = bpm;

        android.util.Log.d("BarbellBLE", "Heart rate: " + bpm + " BPM");

        heartRateHandler.removeCallbacks(heartRateTimeout);

        if (bpm > 0) {
            heartRateHandler.postDelayed(heartRateTimeout, 5000);
        }

        updateHeartRateDisplay();
    }

    private void updateHeartRateDisplay() {
        HeartRateData.setHeartRate(currentHeartRate);
        if (heartRateText == null
                || heartRateStatus == null
                || heartRateDescription == null) {
            return;
        }

        if (currentHeartRate > 0) {
            heartRateText.setText(currentHeartRate + " BPM");
            heartRateStatus.setText("LIVE");
            heartRateDescription.setText(
                    "Live heart rate from your tracker"
            );
        } else {
            heartRateText.setText("-- BPM");
            heartRateStatus.setText("WAITING");
            heartRateDescription.setText(
                    "Waiting for heart rate measurements."
            );
        }
    }
@Override
public void onStatus(String message) {
    android.util.Log.d("BarbellBLE", message);

    if ("Disconnected".equals(message)) {
        heartRateHandler.removeCallbacks(heartRateTimeout);
        currentHeartRate = 0;
        updateHeartRateDisplay();
    }
}

    @Override
    public void onError(String message) {
        android.util.Log.e("BarbellBLE", message);
    }

    // =================================================
    // ESP32 BLUETOOTH PERMISSIONS AND CONNECTION
    // =================================================

    private void connectToTracker() {

        if (bleManager == null) return;

        if (bleManager.hasPermissions()) {
            bleManager.scanAndConnect();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            // Android 12 and newer.
            permissionLauncher.launch(new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            });

        } else {

            // Android 11 and older.
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            });
        }
    }

    // =================================================
    // REFRESH DASHBOARD WHEN RETURNING
    // =================================================

    @Override
    protected void onResume() {
        super.onResume();

        if (dashboardContainer != null) {
            buildDashboard();
        }
    }
    @Override
    protected void onDestroy() {
        heartRateHandler.removeCallbacks(heartRateTimeout);
        // Stop simulated IMU readings
        simulationHandler.removeCallbacks(simulationRunnable);

        // Disconnect Bluetooth
        if (bleManager != null) {
            bleManager.disconnect();
        }

        super.onDestroy();
    }
    private long simulationSampleCount = 0;
    private BLEManager.SensorSample createSimulatedSample() {
        BLEManager.SensorSample sample = new BLEManager.SensorSample();

        sample.timestampUs = android.os.SystemClock.elapsedRealtimeNanos() / 1000L;
        sample.sequence = 0;

        // Stationary sensor: gravity along Z
        simulationSampleCount++;

        long phase = simulationSampleCount % 200;

// Simulate one movement every 4 seconds
        if (phase >= 50 && phase < 125) {
            // Movement lasting approximately 1.5 seconds
            sample.ax = (float) (0.20 * Math.sin(
                    2.0 * Math.PI * (phase - 50) / 75.0
            ));
        } else {
            // Resting
            sample.ax = 0.0f;
        }

        sample.ay = 0.0f;
        sample.az = 1.0f;

        // No rotation
        sample.gx = 0.0f;
        sample.gy = 0.0f;
        sample.gz = 0.0f;

        // Neutral orientation
        sample.qw = 1.0f;
        sample.qx = 0.0f;
        sample.qy = 0.0f;
        sample.qz = 0.0f;

        return sample;
    }
    private final Runnable simulationRunnable = new Runnable() {
        @Override
        public void run() {
            if (!SIMULATE_IMU) {
                return;
            }

            BLEManager.SensorSample sample = createSimulatedSample();

            // Send simulated data through existing processing code
            onSample(sample);

            // Generate another sample after 20 milliseconds
            simulationHandler.postDelayed(this, 20);
        }
    };
}
