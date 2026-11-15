package com.example.coen390project;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.TextView;
import android.view.View;
import android.widget.Button;
import android.os.Handler;
import android.os.Looper;

public class WorkoutSessionActivity extends AppCompatActivity {
    private Handler repHandler;
    private Runnable repUpdater;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workout_session);

        Button backButton = findViewById(R.id.backButton);


        backButton.setOnClickListener(v -> {
            SensorProcess.getInstance().stopWorkout();
            SensorProcess.getInstance().cancelLearning();
            finish();
        });



        TextView exerciseNameText = findViewById(R.id.exerciseNameText);

        String exerciseName = getIntent().getStringExtra("EXERCISE_NAME");

        if (exerciseName == null || exerciseName.isEmpty()) {
            exerciseName = "Exercise";
        }

        exerciseNameText.setText(exerciseName);

        SensorProcess sensorProcess = SensorProcess.getInstance();

        boolean alreadyLearned =
                sensorProcess.loadExercise(this, exerciseName);

        Button learnButton = findViewById(R.id.learnMovementButton);
        Button finishLearningButton = findViewById(R.id.finishLearningButton);
        Button startButton = findViewById(R.id.startWorkoutButton);
        Button finishButton = findViewById(R.id.finishWorkoutButton);

        TextView statusText = findViewById(R.id.workoutStatusText);

        if (alreadyLearned) {
            statusText.setText("Movement already learned");
            learnButton.setVisibility(View.GONE);
            finishLearningButton.setVisibility(View.GONE);
            startButton.setVisibility(View.VISIBLE);
        } else {
            statusText.setText("Learn this movement first");
            learnButton.setVisibility(View.VISIBLE);
            finishLearningButton.setVisibility(View.GONE);
            startButton.setVisibility(View.GONE);
        }

        finishButton.setVisibility(View.GONE);

        learnButton.setOnClickListener(v -> {
            sensorProcess.startLearning();

            statusText.setText("Learning... Perform one repetition");

            learnButton.setVisibility(View.GONE);
            finishLearningButton.setVisibility(View.VISIBLE);
        });

        finishLearningButton.setOnClickListener(v -> {

            sensorProcess.stopLearning();

            boolean saved = sensorProcess.saveExercise(
                    this, exerciseNameText.getText().toString());

            if (saved) {
                statusText.setText("Movement learned successfully!");

                finishLearningButton.setVisibility(View.GONE);
                startButton.setVisibility(View.VISIBLE);

            } else {
                statusText.setText("Learning failed. Please try again.");

                finishLearningButton.setVisibility(View.GONE);
                learnButton.setVisibility(View.VISIBLE);
            }
        });

        TextView repCountText = findViewById(R.id.repCountText);
        startButton.setOnClickListener(v -> {
            sensorProcess.startWorkout();
            repCountText.setText("0 Reps");


            statusText.setText("Workout in progress...");

            startButton.setVisibility(View.GONE);
            learnButton.setVisibility(View.GONE);
            finishLearningButton.setVisibility(View.GONE);
            finishButton.setVisibility(View.VISIBLE);
        });


        finishButton.setOnClickListener(v -> {
            sensorProcess.stopWorkout();

            int totalReps = sensorProcess.getRepCount();

            if (totalReps == 0) {
                statusText.setText("No repetitions recorded. Workout not saved.");
                finishButton.setVisibility(View.GONE);
                startButton.setVisibility(View.VISIBLE);
                return;
            }


            boolean saved = WorkoutHistoryStorage.saveWorkout(
                    this,
                    exerciseNameText.getText().toString(),
                    totalReps
            );

            if (saved) {
                statusText.setText("Workout completed and saved!");
            } else {
                statusText.setText("Workout completed, but saving failed.");
            }

            repCountText.setText(totalReps + " Reps");

            finishButton.setVisibility(View.GONE);
            startButton.setVisibility(View.VISIBLE);
        });


        repHandler = new Handler(Looper.getMainLooper());

        repUpdater = new Runnable() {
            @Override
            public void run() {
                repCountText.setText(sensorProcess.getRepCount() + " Reps");
                repHandler.postDelayed(this, 200);
            }
        };

        repHandler.post(repUpdater);



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.exerciseNameText), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onDestroy() {
        if (repHandler != null && repUpdater != null) {
            repHandler.removeCallbacks(repUpdater);
        }

        SensorProcess.getInstance().stopWorkout();

        SensorProcess.getInstance().cancelLearning();


        super.onDestroy();
    }

}