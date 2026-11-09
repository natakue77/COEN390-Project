
package com.example.coen390project;

import android.os.Bundle;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class Option2Activity extends AppCompatActivity {

    private Spinner workoutSpinner;
    private Spinner timelineSpinner;
    private EditText goalEditText;
    private Button saveGoalButton;
    private TextView goalDescriptionTextView;
    private GoalAnalyticsGraph goalAnalyticsGraph;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option2);

        Toolbar toolbar = findViewById(R.id.optionToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        workoutSpinner = findViewById(R.id.workoutSpinner);
        timelineSpinner = findViewById(R.id.timelineSpinner);
        goalEditText = findViewById(R.id.goalEditText);
        saveGoalButton = findViewById(R.id.saveGoalButton);
        goalDescriptionTextView =
                findViewById(R.id.goalDescriptionTextView);
        goalAnalyticsGraph =
                findViewById(R.id.goalAnalyticsGraph);

        preferences = getSharedPreferences(
                "WorkoutGoals", MODE_PRIVATE
        );

        String[] workouts = {
                "Squats",
                "Push-ups",
                "Bicep Curls"
        };

        String[] timelines = {
                "Day",
                "Week",
                "Month"
        };

        ArrayAdapter<String> workoutAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                workouts
        );

        workoutSpinner.setAdapter(workoutAdapter);

        ArrayAdapter<String> timelineAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                timelines
        );

        timelineSpinner.setAdapter(timelineAdapter);

        // Load the saved goal when the exercise changes
        workoutSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        loadGoal();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );

        // Save the user's target
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

            loadGoal();

            Toast.makeText(
                    this,
                    "Goal saved successfully",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    // Display the saved goal for the selected exercise
    private void loadGoal() {

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

        // Actual completed repetitions are not connected yet
        goalAnalyticsGraph.setGoalData(0, target);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
