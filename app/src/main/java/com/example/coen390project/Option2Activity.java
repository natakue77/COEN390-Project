
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


import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import androidx.activity.OnBackPressedCallback;
import android.widget.FrameLayout;


public class Option2Activity extends AppCompatActivity {

    private Spinner workoutSpinner;
    private FrameLayout workoutGraphContainer;
    private FrameLayout goalGraphContainer;
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
        workoutGraphContainer = findViewById(R.id.workoutGraphContainer);
        goalGraphContainer = findViewById(R.id.goalGraphContainer);
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
        setupBackButton();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (workoutSpinner != null) {
            setupSpinners();
        }
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


    private ArrayList<String> getCustomWorkouts() {

        ArrayList<String> customWorkouts = new ArrayList<>();

        SharedPreferences workoutPreferences = getSharedPreferences(
                "CustomWorkoutPreferences",
                MODE_PRIVATE
        );

        String saved = workoutPreferences.getString(
                "customWorkoutLists",
                "[]"
        );

        try {
            JSONArray workouts = new JSONArray(saved);

            for (int i = 0; i < workouts.length(); i++) {

                JSONObject workout = workouts.optJSONObject(i);

                if (workout != null) {
                    String name = workout.optString("name", "");

                    if (!name.isEmpty()) {
                        customWorkouts.add(name);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return customWorkouts;
    }

    private void setupSpinners() {


        String[] workouts = {
                "Bicep Curls",
                "Tricep Extensions",
                "Hammer Curls",
                "Shoulder Press",
                "Lateral Raises",
                "Dumbbell Lateral Raises",
                "Front Raises",
                "Reverse Flyes",
                "Arnold Press",
                "Upright Rows",
                "Face Pulls",
                "Wrist Curls",
                "Reverse Wrist Curls",
                "Reverse Curls",
                "Farmer's Carries",
                "Dead Hangs",
                "Wrist Roller",
                "Standard Bicep Curls",
                "Concentration Curls",
                "Preacher Curls",
                "Incline Dumbbell Curls",
                "Cable Curls",
                "EZ Bar Curls",
                "Tricep Pushdowns",
                "Overhead Tricep Extensions",
                "Tricep Dips",
                "Skull Crushers",
                "Close-Grip Bench Press",
                "Diamond Push-Ups",
                "Dumbbell Kickbacks",
                "Squats",
                "Lunges",
                "Leg Press",
                "Calf Raises",
                "Glute Bridges",
                "Romanian Deadlifts",
                "Leg Extensions",
                "Leg Curls",
                "Push-Ups",
                "Bench Press",
                "Incline Dumbbell Press",
                "Chest Flyes",
                "Cable Crossovers",
                "Decline Bench Press",
                "Dumbbell Pullovers",
                "Pull-Ups",
                "Lat Pulldowns",
                "Bent-Over Rows",
                "Seated Cable Rows",
                "Deadlifts",
                "Single-Arm Dumbbell Rows",
                "Straight-Arm Pulldowns",
                "T-Bar Rows"
        };


        ArrayList<String> allWorkouts = new ArrayList<>();

        for (String workout : workouts) {
            allWorkouts.add(workout);
        }

        for (String customWorkout : getCustomWorkouts()) {
            if (!allWorkouts.contains(customWorkout)) {
                allWorkouts.add(customWorkout);
            }
        }


        String[] timelines = {
                "Day",
                "Week",
                "Month"
        };

        ArrayAdapter<String> workoutAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                allWorkouts
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
                        displayWorkoutHistory();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );

        timelineSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        displayWorkoutHistory();
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
            displayWorkoutHistory();

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


    private void setupBackButton() {

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (drawerLayout != null &&
                                drawerLayout.isDrawerOpen(GravityCompat.START)) {

                            drawerLayout.closeDrawer(GravityCompat.START);

                        } else {
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                });
    }

    private void displayWorkoutHistory() {

        if (workoutGraphContainer == null ||
                workoutSpinner.getSelectedItem() == null) {
            return;
        }

        String selectedExercise =
                workoutSpinner.getSelectedItem().toString();

        JSONArray history = WorkoutHistoryStorage.getHistory(this);

        int totalReps = 0;
        int sessionCount = 0;

        java.util.ArrayList<Float> repData = new java.util.ArrayList<>();


        String selectedTimeline = timelineSpinner.getSelectedItem() == null
                ? "Day"
                : timelineSpinner.getSelectedItem().toString();

        java.util.Calendar start = java.util.Calendar.getInstance();

        if (selectedTimeline.equals("Day")) {
            start.set(java.util.Calendar.HOUR_OF_DAY, 0);
            start.set(java.util.Calendar.MINUTE, 0);
            start.set(java.util.Calendar.SECOND, 0);
            start.set(java.util.Calendar.MILLISECOND, 0);

        } else if (selectedTimeline.equals("Week")) {
            start.set(java.util.Calendar.DAY_OF_WEEK,
                    start.getFirstDayOfWeek());
            start.set(java.util.Calendar.HOUR_OF_DAY, 0);
            start.set(java.util.Calendar.MINUTE, 0);
            start.set(java.util.Calendar.SECOND, 0);
            start.set(java.util.Calendar.MILLISECOND, 0);

        } else if (selectedTimeline.equals("Month")) {
            start.set(java.util.Calendar.DAY_OF_MONTH, 1);
            start.set(java.util.Calendar.HOUR_OF_DAY, 0);
            start.set(java.util.Calendar.MINUTE, 0);
            start.set(java.util.Calendar.SECOND, 0);
            start.set(java.util.Calendar.MILLISECOND, 0);
        }

        long startTime = start.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        for (int i = 0; i < history.length(); i++) {
            JSONObject workout = history.optJSONObject(i);


            if (workout != null &&
                    selectedExercise.equals(workout.optString("exercise")) &&
                    workout.optLong("date", 0) >= startTime &&
                    workout.optLong("date", 0) <= endTime) {


                totalReps += workout.optInt("reps", 0);
                sessionCount++;

                repData.add((float) workout.optInt("reps", 0));

            }
        }

        workoutGraphContainer.removeAllViews();

        WorkoutProgressGraph graph = new WorkoutProgressGraph(this);


        float[] graphData = new float[repData.size()];

        for (int i = 0; i < repData.size(); i++) {
            graphData[i] = repData.get(i);
        }

        graph.setWorkoutData(graphData);

        int targetGoal = preferences.getInt(selectedExercise, 0);

        displayGoalAnalytics(totalReps, targetGoal);



        workoutGraphContainer.addView(graph,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private void displayGoalAnalytics(float progress, float goal) {

        if (goalGraphContainer == null) {
            return;
        }

        goalGraphContainer.removeAllViews();

        GoalAnalyticsGraph graph = new GoalAnalyticsGraph(this);
        graph.setGoalData(progress, goal);

        goalGraphContainer.addView(graph,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
    }


}
