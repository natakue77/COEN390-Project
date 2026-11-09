
package com.example.coen390project;

import android.os.Bundle;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class Option4Activity extends AppCompatActivity {

    private Spinner goalSpinner;
    private TextView tipsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option4);

        Toolbar toolbar = findViewById(R.id.optionToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        goalSpinner = findViewById(R.id.goalSpinner);
        tipsTextView = findViewById(R.id.tipsTextView);

        // Fitness goals
        String[] goals = {
                "Build Muscle",
                "Lose Weight",
                "Improve Endurance",
                "General Fitness"
        };

        // Add goals to the dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                goals
        );

        goalSpinner.setAdapter(adapter);

        // Save and remember the selected goal
        SharedPreferences preferences = getSharedPreferences(
                "FitnessPreferences", MODE_PRIVATE
        );

        int savedGoal = preferences.getInt("selectedGoal", 0);
        goalSpinner.setSelection(savedGoal);

        goalSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        // Save the selected goal
                        preferences.edit()
                                .putInt("selectedGoal", position)
                                .apply();

                        // Display tips based on the goal
                        if (position == 0) {
                            tipsTextView.setText(
                                    "BUILD MUSCLE\n\n" +
                                            "1. Focus on proper exercise form.\n\n" +
                                            "2. Gradually increase weights or repetitions.\n\n" +
                                            "3. Allow your muscles time to recover.\n\n" +
                                            "4. Eat enough protein to support muscle growth."
                            );

                        } else if (position == 1) {
                            tipsTextView.setText(
                                    "LOSE WEIGHT\n\n" +
                                            "1. Combine strength training with cardio.\n\n" +
                                            "2. Stay consistent with your workouts.\n\n" +
                                            "3. Focus on balanced eating habits.\n\n" +
                                            "4. Get enough sleep and recovery."
                            );

                        } else if (position == 2) {
                            tipsTextView.setText(
                                    "IMPROVE ENDURANCE\n\n" +
                                            "1. Include regular cardio exercises.\n\n" +
                                            "2. Gradually increase workout duration.\n\n" +
                                            "3. Take recovery days when needed.\n\n" +
                                            "4. Track your progress over time."
                            );

                        } else if (position == 3) {
                            tipsTextView.setText(
                                    "GENERAL FITNESS\n\n" +
                                            "1. Stay physically active regularly.\n\n" +
                                            "2. Include both strength and cardio exercises.\n\n" +
                                            "3. Warm up before exercising.\n\n" +
                                            "4. Maintain a consistent workout routine."
                            );
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
