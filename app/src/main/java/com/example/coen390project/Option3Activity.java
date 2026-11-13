
package com.example.coen390project;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Option3Activity extends AppCompatActivity {

    private static final int REQUEST_WORKOUT = 200;

    private CalendarView routineCalendar;
    private TextView tvSelectedDate;
    private TextView routineCount;
    private Button btnAddWorkout;
    private LinearLayout routineList;

    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option3);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.optionToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Routine");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        // Connect layout views
        routineCalendar = findViewById(R.id.routineCalendar);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        routineCount = findViewById(R.id.routineCount);
        btnAddWorkout = findViewById(R.id.btnAddWorkout);
        routineList = findViewById(R.id.routineList);

        // Select today's date initially
        selectedDate = formatDate(System.currentTimeMillis());

        updateSelectedDateText();
        displayRoutine();

        // Calendar date selection
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

        // Add workout button
        btnAddWorkout.setOnClickListener(v -> openWorkouts());
    }

    // Open Workouts in routine-selection mode
    private void openWorkouts() {

        Intent intent = new Intent(
                Option3Activity.this,
                Option1Activity.class
        );

        intent.putExtra("ROUTINE_SELECTION_MODE", true);

        startActivityForResult(intent, REQUEST_WORKOUT);
    }

    // Receive selected exercises from Workouts
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

    // Ask whether the workout should repeat weekly
    private void showRepeatChoice(ArrayList<String> exercises) {

        String[] choices = {
                "No repeat",
                "Repeat every week"
        };

        new AlertDialog.Builder(this)
                .setTitle("Repeat workout?")
                .setItems(choices, (dialog, which) -> {

                    boolean repeatWeekly = (which == 1);

                    showTimePicker(exercises, repeatWeekly);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Open the clock directly
    private void showTimePicker(
            ArrayList<String> exercises,
            boolean repeatWeekly) {

        showClockPicker(exercises, repeatWeekly);
    }

    // Display Android's clock time picker
    private void showClockPicker(
            ArrayList<String> exercises,
            boolean repeatWeekly) {

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog picker = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {

                    Calendar selectedTime = Calendar.getInstance();

                    selectedTime.set(
                            Calendar.HOUR_OF_DAY,
                            selectedHour
                    );

                    selectedTime.set(
                            Calendar.MINUTE,
                            selectedMinute
                    );

                    String time = new SimpleDateFormat(
                            "h:mm a",
                            Locale.getDefault()
                    ).format(selectedTime.getTime());

                    saveWorkout(
                            exercises,
                            repeatWeekly,
                            time
                    );
                },
                hour,
                minute,
                false
        );

        picker.setTitle("Choose Workout Time");
        picker.show();
    }

    // Save the workout session
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

    // Update selected date label
    private void updateSelectedDateText() {

        try {
            SimpleDateFormat inputFormat =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            SimpleDateFormat outputFormat =
                    new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());

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

    // Display all workouts scheduled for the selected date
    private void displayRoutine() {

        routineList.removeAllViews();

        JSONArray sessions = RoutineStorage.getSessionsForDate(
                this,
                selectedDate
        );

        int count = sessions.length();

        routineCount.setText(
                count + (count == 1 ? " workout scheduled" : " workouts scheduled")
        );

        if (count == 0) {

            TextView emptyMessage = new TextView(this);

            emptyMessage.setText(
                    "No workouts scheduled for this day.\nTap Add Workout to get started."
            );

            emptyMessage.setTextColor(Color.GRAY);
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

                // Workout card
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

                // Workout time
                TextView timeText = new TextView(this);

                timeText.setText(time);
                timeText.setTextSize(20);
                timeText.setTextColor(Color.WHITE);
                timeText.setTypeface(null, android.graphics.Typeface.BOLD);

                card.addView(timeText);

                // Weekly repeat label
                if (repeatWeekly) {

                    TextView repeatText = new TextView(this);

                    repeatText.setText("Repeats every week");
                    repeatText.setTextSize(13);
                    repeatText.setTextColor(Color.LTGRAY);

                    card.addView(repeatText);
                }

                // Exercise names
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

                // Remove workout button
                Button removeButton = new Button(this);

                removeButton.setText("Remove Workout");
                removeButton.setAllCaps(false);

                removeButton.setOnClickListener(v -> {

                    new AlertDialog.Builder(this)
                            .setTitle("Remove Workout")
                            .setMessage(
                                    "Are you sure you want to remove this workout?"
                            )
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Remove", (dialog, which) -> {

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
                            })
                            .show();
                });

                card.addView(removeButton);

                routineList.addView(card);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // Format date as yyyy-MM-dd for storage
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
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
