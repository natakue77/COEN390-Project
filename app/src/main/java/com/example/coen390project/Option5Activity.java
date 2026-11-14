
package com.example.coen390project;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

public class Option5Activity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText ageEditText;
    private EditText weightEditText;

    private RadioButton lightModeRadio;
    private RadioButton darkModeRadio;

    private Button saveSettingsButton;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option5);
        Button learnButton = findViewById(R.id.LearnButton);
        SensorProcess processor = SensorProcess.getInstance();

        learnButton.setText(processor.isLearning() ? "Stop Learning" : "Start Learning");

        learnButton.setOnClickListener(v -> {
            if (processor.isLearning()) {
                processor.stopLearning();
            } else {
                processor.startLearning();
            }

            learnButton.setText(
                    processor.isLearning() ? "Stop Learning" : "Start Learning"
            );
        });

        // Keep the original Toolbar
        Toolbar toolbar = findViewById(R.id.optionToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Connect Java to XML
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        ageEditText = findViewById(R.id.ageEditText);
        weightEditText = findViewById(R.id.weightEditText);

        lightModeRadio = findViewById(R.id.lightModeRadio);
        darkModeRadio = findViewById(R.id.darkModeRadio);

        saveSettingsButton = findViewById(R.id.saveSettingsButton);

        // Access saved settings
        preferences = getSharedPreferences(
                "UserSettings", MODE_PRIVATE
        );

        // Load previously saved information
        nameEditText.setText(preferences.getString("name", ""));
        emailEditText.setText(preferences.getString("email", ""));
        ageEditText.setText(preferences.getString("age", ""));
        weightEditText.setText(preferences.getString("weight", ""));

        // Load saved appearance
        boolean darkMode = preferences.getBoolean("darkMode", false);

        if (darkMode) {
            darkModeRadio.setChecked(true);
        } else {
            lightModeRadio.setChecked(true);
        }

        // Save button
        saveSettingsButton.setOnClickListener(v -> {

            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String age = ageEditText.getText().toString().trim();
            String weight = weightEditText.getText().toString().trim();

            boolean useDarkMode = darkModeRadio.isChecked();

            // Save information locally
            preferences.edit()
                    .putString("name", name)
                    .putString("email", email)
                    .putString("age", age)
                    .putString("weight", weight)
                    .putBoolean("darkMode", useDarkMode)
                    .apply();

            Toast.makeText(
                    this,
                    "Settings saved successfully",
                    Toast.LENGTH_SHORT
            ).show();

            // Apply selected theme
            int mode = useDarkMode
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO;

            if (AppCompatDelegate.getDefaultNightMode() != mode) {
                AppCompatDelegate.setDefaultNightMode(mode);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
