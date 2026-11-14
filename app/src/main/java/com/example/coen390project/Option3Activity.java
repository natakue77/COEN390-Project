
package com.example.coen390project;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Option3Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_WORKOUT = 200;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private CalendarView routineCalendar;
    private TextView tvSelectedDate;
    private TextView routineCount;
    private Button btnAddWorkout;
    private LinearLayout routineList;

    private String selectedDate;

    private static final int CARD_COLOR =
            Color.rgb(41, 45, 53);

    private static final int MUTED_COLOR =
            Color.rgb(191, 196, 206);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_option3);

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

        setupToolbar();
        setupNavigation();

        routineCalendar = findViewById(R.id.routineCalendar);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        routineCount = findViewById(R.id.routineCount);
        btnAddWorkout = findViewById(R.id.btnAddWorkout);
        routineList = findViewById(R.id.routineList);

        selectedDate = formatDate(System.currentTimeMillis());

        updateSelectedDateText();
        displayRoutine();

        routineCalendar.setOnDateChangeListener(
                (view, year, month, dayOfMonth) -> {

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month, dayOfMonth);

                    selectedDate = formatDate(
                            calendar.getTimeInMillis()
                    );

                    updateSelectedDateText();
                    displayRoutine();
                }
        );

        btnAddWorkout.setOnClickListener(v -> openWorkouts());
    }

    private void setupToolbar() {

        Toolbar toolbar = findViewById(R.id.optionToolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Routine");
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
        navigationView.setCheckedItem(R.id.nav_routine);
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
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;

        } else if (id == R.id.nav_tips) {
            startActivity(new Intent(this, Option4Activity.class));

        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, Option5Activity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openWorkouts() {

        Intent intent = new Intent(
                Option3Activity.this,
                Option1Activity.class
        );

        intent.putExtra("ROUTINE_SELECTION_MODE", true);

        startActivityForResult(intent, REQUEST_WORKOUT);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_WORKOUT
                && resultCode == RESULT_OK
                && data != null) {

            ArrayList<String> exercises =
                    data.getStringArrayListExtra("SELECTED_EXERCISES");

            if (exercises != null && !exercises.isEmpty()) {
                showRepeatChoice(exercises);
            }
        }
    }

    // REPEAT WORKOUT POPUP
    private void showRepeatChoice(ArrayList<String> exercises) {

        String[] choices = {
                "No repeat",
                "Repeat every week"
        };

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Repeat workout?")
                .setItems(choices, (dialogInterface, which) -> {

                    boolean repeatWeekly = (which == 1);

                    showTimePicker(exercises, repeatWeekly);
                })
                .setNegativeButton("Cancel", null)
                .create();

        styleDarkDialog(dialog);
    }

    // DARK POPUP STYLING
    private void styleDarkDialog(AlertDialog dialog) {

        dialog.setOnShowListener(d -> {

            int titleId = getResources().getIdentifier(
                    "alertTitle",
                    "id",
                    "android"
            );

            if (titleId != 0) {

                TextView title = dialog.findViewById(titleId);

                if (title != null) {
                    title.setTextColor(Color.WHITE);
                }
            }

            TextView message = dialog.findViewById(
                    android.R.id.message
            );

            if (message != null) {
                message.setTextColor(MUTED_COLOR);
            }

            if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(Color.WHITE);
            }

            if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(Color.WHITE);
            }

            if (dialog.getButton(AlertDialog.BUTTON_NEUTRAL) != null) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                        .setTextColor(Color.WHITE);
            }

            if (dialog.getListView() != null) {

                dialog.getListView().setBackgroundColor(CARD_COLOR);

                dialog.getListView().setDivider(
                        new android.graphics.drawable.ColorDrawable(
                                Color.rgb(65, 70, 80)
                        )
                );

                dialog.getListView().setDividerHeight(1);

                for (int i = 0;
                     i < dialog.getListView().getChildCount();
                     i++) {

                    View child = dialog.getListView().getChildAt(i);

                    if (child instanceof TextView) {
                        ((TextView) child).setTextColor(Color.WHITE);
                    }
                }
            }
        });

        dialog.show();

        GradientDrawable background = new GradientDrawable();
        background.setColor(CARD_COLOR);
        background.setCornerRadius(
                16 * getResources().getDisplayMetrics().density
        );

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(background);
        }
    }

    private void showTimePicker(
            ArrayList<String> exercises,
            boolean repeatWeekly) {

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog picker = new TimePickerDialog(
                new android.view.ContextThemeWrapper(
                        Option3Activity.this,
                        R.style.RoutineTimePickerTheme
                ),
                (view, selectedHour, selectedMinute) -> {

                    Calendar selectedTime = Calendar.getInstance();

                    selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                    selectedTime.set(Calendar.MINUTE, selectedMinute);

                    String time = new SimpleDateFormat(
                            "h:mm a",
                            Locale.getDefault()
                    ).format(selectedTime.getTime());

                    saveWorkout(exercises, repeatWeekly, time);
                },
                hour,
                minute,
                false
        );

        picker.setTitle("Choose Workout Time");
        picker.show();
    }

    private void saveWorkout(
            ArrayList<String> exercises,
            boolean repeatWeekly,
            String time) {

        JSONArray exerciseArray = new JSONArray();

        for (String exercise : exercises) {
            exerciseArray.put(exercise);
        }

        RoutineStorage.addSession(
                this,
                selectedDate,
                time,
                exerciseArray,
                repeatWeekly
        );

        Toast.makeText(
                this,
                "Workout saved successfully!",
                Toast.LENGTH_SHORT
        ).show();

        displayRoutine();
    }

    private void updateSelectedDateText() {

        try {

            SimpleDateFormat inputFormat =
                    new SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                    );

            SimpleDateFormat outputFormat =
                    new SimpleDateFormat(
                            "MMMM dd, yyyy",
                            Locale.getDefault()
                    );

            Date date = inputFormat.parse(selectedDate);

            if (date != null) {
                tvSelectedDate.setText(
                        "Selected date: " + outputFormat.format(date)
                );
            }

        } catch (Exception e) {
            tvSelectedDate.setText("Selected date: " + selectedDate);
        }
    }

    private void displayRoutine() {

        routineList.removeAllViews();

        JSONArray sessions = RoutineStorage.getSessionsForDate(
                this,
                selectedDate
        );

        int count = sessions.length();

        routineCount.setText(
                count + (count == 1
                        ? " workout scheduled"
                        : " workouts scheduled")
        );

        if (count == 0) {

            TextView emptyMessage = new TextView(this);

            emptyMessage.setText(
                    "No workouts scheduled for this day.\nTap Add Workout to get started."
            );

            emptyMessage.setTextColor(Color.rgb(143, 149, 161));
            emptyMessage.setTextSize(15);
            emptyMessage.setGravity(Gravity.CENTER);
            emptyMessage.setPadding(20, 45, 20, 45);

            routineList.addView(emptyMessage);
            return;
        }

        for (int i = 0; i < count; i++) {

            try {

                JSONObject session = sessions.getJSONObject(i);

                String sessionId = session.getString("id");
                String time = session.getString("time");

                boolean repeatWeekly =
                        session.optBoolean("repeatWeekly", false);

                JSONArray exercises =
                        session.getJSONArray("exercises");

                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(20, 18, 20, 18);
                card.setBackgroundResource(R.drawable.equipment_card);

                LinearLayout.LayoutParams cardParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                cardParams.setMargins(0, 0, 0, 18);
                card.setLayoutParams(cardParams);

                TextView timeText = new TextView(this);
                timeText.setText(time);
                timeText.setTextSize(20);
                timeText.setTextColor(Color.WHITE);
                timeText.setTypeface(null, Typeface.BOLD);

                card.addView(timeText);

                if (repeatWeekly) {

                    TextView repeatText = new TextView(this);
                    repeatText.setText("Repeats every week");
                    repeatText.setTextSize(13);
                    repeatText.setTextColor(Color.rgb(239, 83, 80));

                    LinearLayout.LayoutParams repeatParams =
                            new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );

                    repeatParams.setMargins(0, 4, 0, 0);
                    repeatText.setLayoutParams(repeatParams);

                    card.addView(repeatText);
                }

                TextView exerciseText = new TextView(this);

                StringBuilder exerciseNames = new StringBuilder();

                for (int j = 0; j < exercises.length(); j++) {
                    exerciseNames.append("• ")
                            .append(exercises.getString(j))
                            .append("\n");
                }

                exerciseText.setText(exerciseNames.toString());
                exerciseText.setTextSize(15);
                exerciseText.setTextColor(Color.WHITE);
                exerciseText.setPadding(0, 12, 0, 12);

                card.addView(exerciseText);

                Button removeButton = new Button(this);
                removeButton.setText("Remove Workout");
                removeButton.setAllCaps(false);
                removeButton.setTextColor(Color.WHITE);

                removeButton.setBackgroundTintList(
                        ColorStateList.valueOf(
                                Color.rgb(211, 47, 47)
                        )
                );

                // REMOVE WORKOUT POPUP
                removeButton.setOnClickListener(v -> {

                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("Remove Workout")
                            .setMessage(
                                    "Are you sure you want to remove this workout?"
                            )
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton(
                                    "Remove",
                                    (dialogInterface, which) -> {

                                        RoutineStorage.removeSession(
                                                this,
                                                sessionId
                                        );

                                        displayRoutine();

                                        Toast.makeText(
                                                this,
                                                "Workout removed.",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                            )
                            .create();

                    styleDarkDialog(dialog);
                });

                card.addView(removeButton);
                routineList.addView(card);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String formatDate(long milliseconds) {

        return new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date(milliseconds));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (routineList != null) {
            displayRoutine();
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
