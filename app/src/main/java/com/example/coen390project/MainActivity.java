
package com.example.coen390project;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements BLEManager.Listener {

    private DrawerLayout drawerLayout;

    // Preserve the existing ESP32 BLE connection and IMU processing.
    private BLEManager bleManager;
    private SensorProcess sensorProcess = SensorProcess.getInstance();

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
    private LinearLayout dashboardContainer;

    private TextView workoutName;
    private TextView workoutDetails;
    private TextView workoutWeight;

    private TextView weeklyNumber;
    private TextView weeklyDescription;

    private TextView upcomingDescription;

    private final ArrayList<String> todayExercises = new ArrayList<>();

    private int currentExercise = 0;

    private SharedPreferences weightPreferences;

    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(143, 149, 161);
    private final int CARD_COLOR = Color.rgb(41, 45, 53);


    // =================================================
    // IMAGE SIZE SETTINGS
    // Change width and height independently.
    // All measurements are in dp.
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


    // =================================================
    // ICON PLACEMENT SETTINGS
    //
    // Gravity.START  = Left
    // Gravity.CENTER = Center
    // Gravity.END    = Right
    //
    // Change each widget independently.
    // =================================================

    private static final int DUMBBELL_PLACEMENT =
            Gravity.START | Gravity.CENTER_VERTICAL;

    private static final int WEEKLY_PLACEMENT =
            Gravity.CENTER;

    private static final int PROGRESS_PLACEMENT =
            Gravity.CENTER;

    private static final int UPCOMING_PLACEMENT =
            Gravity.START | Gravity.CENTER_VERTICAL;


    // =================================================
    // ICON MARGINS
    //
    // Order: LEFT, TOP, RIGHT, BOTTOM
    // =================================================

    private static final int DUMBBELL_LEFT = -10;
    private static final int DUMBBELL_TOP = 2;
    private static final int DUMBBELL_RIGHT = 0;
    private static final int DUMBBELL_BOTTOM = 0;

    private static final int WEEKLY_LEFT = -27;
    private static final int WEEKLY_TOP = 0;
    private static final int WEEKLY_RIGHT = 0;
    private static final int WEEKLY_BOTTOM = 0;

    private static final int PROGRESS_LEFT = -70;
    private static final int PROGRESS_TOP = 0;
    private static final int PROGRESS_RIGHT = 0;
    private static final int PROGRESS_BOTTOM = 0;

    private static final int UPCOMING_LEFT = -3;
    private static final int UPCOMING_TOP = 0;
    private static final int UPCOMING_RIGHT = 9;
    private static final int UPCOMING_BOTTOM = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bleManager = new BLEManager(this, this);

        weightPreferences = getSharedPreferences(
                "DashboardExerciseWeights",
                MODE_PRIVATE
        );

        dashboardContainer = findViewById(R.id.dashboardContainer);

        setupToolbar();

        buildDashboard();

        // Connect after the dashboard and BLE manager have been initialized.
        connectToTracker();
    }


    // ------------------------------------------------
    // TOOLBAR AND NAVIGATION
    // ------------------------------------------------

    private void setupToolbar() {

        androidx.appcompat.widget.Toolbar toolbar =
                findViewById(R.id.mainToolbar);

        setSupportActionBar(toolbar);

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
                new androidx.activity.OnBackPressedCallback(true) {
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


    // ------------------------------------------------
    // DASHBOARD
    // ------------------------------------------------

    private void buildDashboard() {

        dashboardContainer.removeAllViews();

        loadTodayExercises();

        addWelcomeHeader();

        addWorkoutWidget();

        addSmallWidgetsRow();

        addUpcomingRoutineWidget();

        addHeartRateWidget();
    }

    private void addWelcomeHeader() {

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(0, dp(8), 0, dp(20));

        TextView title = createText(
                "Welcome Back!",
                25,
                WHITE,
                true
        );

        TextView subtitle = createText(
                "READY FOR YOUR NEXT SESSION?",
                11,
                MUTED,
                true
        );

        header.addView(title);
        header.addView(subtitle);

        dashboardContainer.addView(header);
    }


    // ------------------------------------------------
    // TODAY'S WORKOUT
    // ------------------------------------------------

    private void loadTodayExercises() {

        todayExercises.clear();

        String today = formatDate(System.currentTimeMillis());

        JSONArray sessions =
                RoutineStorage.getSessionsForDate(this, today);

        for (int i = 0; i < sessions.length(); i++) {

            JSONObject session = sessions.optJSONObject(i);

            if (session == null) {
                continue;
            }

            JSONArray exercises =
                    session.optJSONArray("exercises");

            if (exercises == null) {
                continue;
            }

            for (int j = 0; j < exercises.length(); j++) {

                String name = exercises.optString(j, "").trim();

                if (!name.isEmpty()) {
                    todayExercises.add(name);
                }
            }
        }

        currentExercise = 0;
    }

    private void addWorkoutWidget() {

        LinearLayout card = createCard();

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(DUMBBELL_PLACEMENT);
        topRow.setPadding(0, dp(0), 0, dp(0));

        topRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        addTitleIcon(
                topRow,
                R.drawable.img_workouts,
                "Dumbbell icon",
                DUMBBELL_WIDTH,
                DUMBBELL_HEIGHT,
                DUMBBELL_LEFT,
                DUMBBELL_TOP,
                DUMBBELL_RIGHT,
                DUMBBELL_BOTTOM
        );

        TextView heading = createText(
                "TODAY'S WORKOUT",
                13,
                WHITE,
                true
        );

        topRow.addView(
                heading,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        Button menuButton = createSmallButton("⋮");
        menuButton.setTextSize(23);

        menuButton.setOnClickListener(v ->
                showWorkoutMenu(menuButton)
        );

        topRow.addView(menuButton);

        card.addView(topRow);

        LinearLayout playerRow = new LinearLayout(this);
        playerRow.setOrientation(LinearLayout.HORIZONTAL);
        playerRow.setGravity(Gravity.CENTER_VERTICAL);
        playerRow.setPadding(0, dp(10), 0, dp(10));

        Button previous = createArrowButton("‹");

        previous.setOnClickListener(v ->
                changeExercise(-1)
        );

        playerRow.addView(previous);

        LinearLayout center = new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setGravity(Gravity.CENTER);
        center.setPadding(dp(4), 0, dp(4), 0);

        workoutName = createText("", 20, WHITE, true);
        workoutName.setGravity(Gravity.CENTER);

        workoutDetails = createText("", 13, MUTED, false);
        workoutDetails.setGravity(Gravity.CENTER);

        center.addView(workoutName);
        center.addView(workoutDetails);

        playerRow.addView(
                center,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        LinearLayout weightLayout = new LinearLayout(this);
        weightLayout.setOrientation(LinearLayout.VERTICAL);
        weightLayout.setGravity(Gravity.CENTER);
        weightLayout.setPadding(dp(5), 0, dp(5), 0);

        workoutWeight = createText("", 23, WHITE, true);
        workoutWeight.setGravity(Gravity.CENTER);

        TextView kg = createText("KG", 10, MUTED, true);

        weightLayout.addView(workoutWeight);
        weightLayout.addView(kg);

        playerRow.addView(weightLayout);

        Button next = createArrowButton("›");

        next.setOnClickListener(v ->
                changeExercise(1)
        );

        playerRow.addView(next);

        card.addView(playerRow);

        TextView footer = createText(
                "‹  PREVIOUS EXERCISE                 NEXT EXERCISE  ›",
                10,
                MUTED,
                true
        );

        footer.setGravity(Gravity.CENTER);
        card.addView(footer);

        dashboardContainer.addView(card);

        updateWorkoutDisplay();
    }

    private void changeExercise(int direction) {

        if (todayExercises.isEmpty()) {
            return;
        }

        currentExercise += direction;

        if (currentExercise < 0) {
            currentExercise = todayExercises.size() - 1;
        }

        if (currentExercise >= todayExercises.size()) {
            currentExercise = 0;
        }

        updateWorkoutDisplay();
    }

    private void updateWorkoutDisplay() {

        if (todayExercises.isEmpty()) {

            workoutName.setText("No workout scheduled");

            workoutDetails.setText(
                    "Add a workout in My Routine to see it here."
            );

            workoutWeight.setText("--");

            return;
        }

        String exercise = todayExercises.get(currentExercise);

        workoutName.setText(exercise);

        workoutDetails.setText(
                "Exercise " + (currentExercise + 1)
                        + " of " + todayExercises.size()
                        + "\nReps and sets not configured"
        );

        String savedWeight = weightPreferences.getString(
                weightKey(exercise),
                "--"
        );

        workoutWeight.setText(savedWeight);
    }


    // ------------------------------------------------
    // WORKOUT MENU
    // ------------------------------------------------

    private void showWorkoutMenu(View anchor) {

        if (todayExercises.isEmpty()) {

            Toast.makeText(
                    this,
                    "There is no exercise to edit yet.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        PopupMenu popup = new PopupMenu(this, anchor);

        popup.getMenu().add("Edit Weight");
        popup.getMenu().add("Reset Weight");

        popup.setOnMenuItemClickListener(item -> {

            String selected = item.getTitle().toString();

            if (selected.equals("Edit Weight")) {

                showEditWeightDialog();

            } else if (selected.equals("Reset Weight")) {

                String exercise = todayExercises.get(currentExercise);

                weightPreferences.edit()
                        .remove(weightKey(exercise))
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

        String exercise = todayExercises.get(currentExercise);

        EditText input = new EditText(this);

        input.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        input.setHint("Enter weight in kg");

        String currentWeight = weightPreferences.getString(
                weightKey(exercise),
                ""
        );

        input.setText(currentWeight.equals("--") ? "" : currentWeight);
        input.setSelectAllOnFocus(true);
        input.setPadding(dp(16), dp(12), dp(16), dp(12));

        new AlertDialog.Builder(this)
                .setTitle("Edit Weight")
                .setMessage("Weight for " + exercise)
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {

                    String value = input.getText()
                            .toString()
                            .trim();

                    try {

                        double weight = Double.parseDouble(value);

                        if (weight < 0) {
                            throw new NumberFormatException();
                        }

                        weightPreferences.edit()
                                .putString(weightKey(exercise), value)
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
                .show();
    }

    private String weightKey(String exercise) {

        return "weight_" + exercise.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "_");
    }


    // ------------------------------------------------
    // WEEKLY ACTIVITY AND PROGRESS
    // ------------------------------------------------

    private void addSmallWidgetsRow() {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);

        LinearLayout weeklyCard = createCard();
        LinearLayout progressCard = createCard();

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        params.setMargins(0, 0, dp(7), dp(14));
        weeklyCard.setLayoutParams(params);

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        progressParams.setMargins(dp(7), 0, 0, dp(14));
        progressCard.setLayoutParams(progressParams);

        addWeeklyActivityContent(weeklyCard);
        addProgressContent(progressCard);

        row.addView(weeklyCard);
        row.addView(progressCard);

        dashboardContainer.addView(row);
    }

    private void addWeeklyActivityContent(LinearLayout card) {

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);

        // Change WEEKLY_PLACEMENT at the top to reposition this group.
        titleRow.setGravity(WEEKLY_PLACEMENT);

        titleRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

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

        TextView title = createText(
                "Weekly Activity",
                15,
                WHITE,
                true
        );

        titleRow.addView(title);

        card.addView(titleRow);

        weeklyNumber = createText(
                "0",
                29,
                WHITE,
                true
        );

        weeklyDescription = createText(
                "Scheduled workouts",
                12,
                MUTED,
                false
        );

        card.addView(weeklyNumber);
        card.addView(weeklyDescription);

        updateWeeklyActivity();

        card.setOnClickListener(v ->
                openActivity(Option3Activity.class)
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

    private void addProgressContent(LinearLayout card) {

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);

        // Change PROGRESS_PLACEMENT at the top to reposition this group.
        titleRow.setGravity(PROGRESS_PLACEMENT);

        titleRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        titleRow.setPadding(0, 0, 0, dp(48));

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

        TextView title = createText(
                "Progress",
                15,
                WHITE,
                true
        );

        titleRow.addView(title);

        card.addView(titleRow);

        TextView description = createText(
                "Your workout progress will appear here.",
                12,
                MUTED,
                false
        );

        card.addView(description);

        card.setOnClickListener(v ->
                openActivity(Option2Activity.class)
        );
    }


    // ------------------------------------------------
    // UPCOMING ROUTINE
    // ------------------------------------------------

    private void addUpcomingRoutineWidget() {

        LinearLayout card = createCard();

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);

        // Change UPCOMING_PLACEMENT at the top to reposition this group.
        titleRow.setGravity(UPCOMING_PLACEMENT);

        titleRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

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

        TextView title = createText(
                "Upcoming Routine",
                16,
                WHITE,
                true
        );

        titleRow.addView(title);

        card.addView(titleRow);

        upcomingDescription = createText(
                "",
                13,
                MUTED,
                false
        );

        card.addView(upcomingDescription);

        updateUpcomingRoutine();

        card.setOnClickListener(v ->
                openActivity(Option3Activity.class)
        );

        dashboardContainer.addView(card);
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

                if (session == null) {
                    continue;
                }

                String time = session.optString("time", "");

                Calendar sessionDateTime =
                        createSessionDateTime(date, time);

                if (sessionDateTime == null) {
                    continue;
                }

                if (sessionDateTime.after(now)) {

                    if (nextSession == null) {

                        nextSession = session;
                        nextDate = date;

                    } else {

                        Calendar currentNext =
                                createSessionDateTime(
                                        nextDate,
                                        nextSession.optString("time", "")
                                );

                        if (currentNext == null
                                || sessionDateTime.before(currentNext)) {

                            nextSession = session;
                            nextDate = date;
                        }
                    }
                }
            }

            if (nextSession != null) {
                break;
            }

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

        int exerciseCount =
                exercises == null ? 0 : exercises.length();

        String readableDate = formatReadableDate(nextDate);

        String time = nextSession.optString("time", "");

        upcomingDescription.setText(
                readableDate
                        + "  •  " + time
                        + "\n" + exerciseCount
                        + (exerciseCount == 1
                        ? " exercise scheduled"
                        : " exercises scheduled")
        );
    }

    private Calendar createSessionDateTime(String date, String time) {

        try {

            SimpleDateFormat dateFormat =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            dateFormat.setLenient(false);

            Date parsedDate = dateFormat.parse(date);

            if (parsedDate == null) {
                return null;
            }

            SimpleDateFormat timeFormat =
                    new SimpleDateFormat("h:mm a", Locale.getDefault());

            timeFormat.setLenient(false);

            Date parsedTime = timeFormat.parse(time);

            if (parsedTime == null) {

                timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
                parsedTime = timeFormat.parse(time);
            }

            if (parsedTime == null) {
                return null;
            }

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


    // ------------------------------------------------
    // HEART RATE
    // ------------------------------------------------

    private void addHeartRateWidget() {

        LinearLayout card = createCard();

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(0, dp(5), 0, dp(10));

        TextView title = createText(
                "Heart Rate Monitor",
                16,
                WHITE,
                true
        );

        top.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        TextView status = createText(
                "COMING SOON",
                10,
                MUTED,
                true
        );

        top.addView(status);
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

        TextView heart = createText(
                "-- BPM",
                29,
                Color.rgb(240, 107, 120),
                true
        );

        heart.setGravity(Gravity.CENTER_VERTICAL);

        heartRateRow.addView(heart);

        card.addView(heartRateRow);

        TextView description = createText(
                "Heart rate sensor integration coming soon.",
                12,
                MUTED,
                false
        );

        description.setGravity(Gravity.CENTER);
        description.setPadding(0, dp(8), 0, 0);

        card.addView(description);

        card.setOnClickListener(v ->
                openActivity(HeartRateActivity.class)
        );

        dashboardContainer.addView(card);
    }


    // ------------------------------------------------
    // CUSTOM ICON HELPER
    // ------------------------------------------------

    private void addTitleIcon(
            LinearLayout parent,
            int imageResource,
            String description,
            int width,
            int height,
            int left,
            int top,
            int right,
            int bottom
    ) {

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


    // ------------------------------------------------
    // DESIGN HELPERS
    // ------------------------------------------------

    private LinearLayout createCard() {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);

        card.setPadding(
                dp(16),
                dp(15),
                dp(16),
                dp(16)
        );

        card.setBackground(makeBackground(
                CARD_COLOR,
                18
        ));

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
            boolean bold
    ) {

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

    private GradientDrawable makeBackground(
            int color,
            int radius
    ) {

        GradientDrawable background = new GradientDrawable();

        background.setColor(color);
        background.setCornerRadius(dp(radius));

        return background;
    }

    private int dp(int value) {

        return (int) (
                value *
                        getResources().getDisplayMetrics().density
                        + 0.5f
        );
    }


    // ------------------------------------------------
    // DATE HELPERS
    // ------------------------------------------------

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


    // ------------------------------------------------
    // REFRESH WHEN RETURNING TO DASHBOARD
    // ------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();

        if (dashboardContainer != null) {
            buildDashboard();
        }
    }


    // ------------------------------------------------
    // BLE SENSOR STREAM (PRESERVED FROM YOUR CURRENT APP)
    // ------------------------------------------------

    @Override
    public void onSample(BLEManager.SensorSample sample) {
        sensorProcess.processSample(sample);
    }

    @Override
    public void onStatus(String message) {
        android.util.Log.d("BarbellBLE", message);
    }

    @Override
    public void onError(String message) {
        android.util.Log.e("BarbellBLE", message);
    }

    private void connectToTracker() {
        if (bleManager.hasPermissions()) {
            bleManager.scanAndConnect();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(new String[] {
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            });
        } else {
            permissionLauncher.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION
            });
        }
    }
}
