package com.example.coen390project;


import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HeartRateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate);

        androidx.appcompat.widget.Toolbar toolbar =
                findViewById(R.id.heartToolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Heart Rate Monitor");
        }

        toolbar.setNavigationOnClickListener(v -> finish());
        TextView heartRateValue = findViewById(R.id.heartRateValue);
        TextView heartRateMessage = findViewById(R.id.heartRateMessage);

        HeartRateData.getHeartRate().observe(this, bpm -> {

            if (bpm != null && bpm > 0) {
                heartRateValue.setText(bpm + " BPM");
                heartRateMessage.setText("Live heart rate from your tracker");
            } else {
                heartRateValue.setText("-- BPM");
                heartRateMessage.setText("Waiting for heart rate measurements.");
            }
        });
    }
}
