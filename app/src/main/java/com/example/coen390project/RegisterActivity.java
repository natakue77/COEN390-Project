
package com.example.coen390project;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private Button registerButton;

    private final int BACKGROUND = Color.rgb(17, 18, 20);
    private final int CARD = Color.rgb(41, 45, 53);
    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(143, 149, 161);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        buildScreen();
    }

    private void buildScreen() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(28), dp(25), dp(28), dp(25));
        root.setBackgroundColor(BACKGROUND);

        TextView logo = new TextView(this);
        logo.setText("REP TRACKER");
        logo.setTextColor(WHITE);
        logo.setTextSize(29);
        logo.setTypeface(null, Typeface.BOLD);
        logo.setGravity(Gravity.CENTER);

        root.addView(logo);

        TextView subtitle = new TextView(this);
        subtitle.setText("CREATE YOUR FITNESS ACCOUNT");
        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(11);
        subtitle.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        subtitleParams.topMargin = dp(8);
        subtitleParams.bottomMargin = dp(30);

        root.addView(subtitle, subtitleParams);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(24), dp(20), dp(24));
        card.setBackground(roundBackground(CARD, 18));

        TextView heading = new TextView(this);
        heading.setText("Create Account");
        heading.setTextColor(WHITE);
        heading.setTextSize(23);
        heading.setTypeface(null, Typeface.BOLD);

        card.addView(heading);

        TextView description = new TextView(this);
        description.setText("Enter your details to get started.");
        description.setTextColor(MUTED);
        description.setTextSize(13);

        LinearLayout.LayoutParams descriptionParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        descriptionParams.topMargin = dp(6);
        descriptionParams.bottomMargin = dp(22);

        card.addView(description, descriptionParams);

        emailInput = createInput(
                "Email address",
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );

        card.addView(emailInput);

        passwordInput = createInput(
                "Password (at least 6 characters)",
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        LinearLayout.LayoutParams fieldParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(54)
                );

        fieldParams.topMargin = dp(13);

        card.addView(passwordInput, fieldParams);

        confirmPasswordInput = createInput(
                "Confirm password",
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        LinearLayout.LayoutParams confirmParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(54)
                );

        confirmParams.topMargin = dp(13);

        card.addView(confirmPasswordInput, confirmParams);

        registerButton = createButton("Create Account");

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(52)
                );

        buttonParams.topMargin = dp(24);

        card.addView(registerButton, buttonParams);

        registerButton.setOnClickListener(v -> createAccount());

        root.addView(card);

        TextView backToLogin = new TextView(this);
        backToLogin.setText("Already have an account? Sign In");
        backToLogin.setTextColor(WHITE);
        backToLogin.setTextSize(13);
        backToLogin.setGravity(Gravity.CENTER);
        backToLogin.setPadding(0, dp(20), 0, dp(10));

        backToLogin.setOnClickListener(v -> finish());

        root.addView(backToLogin);

        setContentView(root);
    }

    private EditText createInput(String hint, int inputType) {

        EditText input = new EditText(this);

        input.setHint(hint);
        input.setHintTextColor(MUTED);
        input.setTextColor(WHITE);
        input.setTextSize(14);
        input.setSingleLine(true);
        input.setInputType(inputType);
        input.setPadding(dp(15), 0, dp(15), 0);
        input.setBackground(roundBackground(CARD, 10));

        return input;
    }

    private Button createButton(String text) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextColor(WHITE);
        button.setTextSize(15);
        button.setTypeface(null, Typeface.BOLD);
        button.setAllCaps(false);
        button.setBackground(roundBackground(
                Color.rgb(75, 86, 105), 12
        ));

        return button;
    }

    private GradientDrawable roundBackground(int color, int radius) {

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));

        return drawable;
    }

    private void createAccount() {

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirm = confirmPasswordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please complete all fields.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(
                    this,
                    "Password must contain at least 6 characters.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(
                    this,
                    "Passwords do not match.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        registerButton.setEnabled(false);
        registerButton.setText("Creating account...");

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                this,
                                "Account created successfully!",
                                Toast.LENGTH_SHORT
                        ).show();

                        Intent intent = new Intent(
                                this,
                                MainActivity.class
                        );

                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        );

                        startActivity(intent);
                        finish();

                    } else {

                        registerButton.setEnabled(true);
                        registerButton.setText("Create Account");

                        String message = task.getException() == null
                                ? "Could not create account."
                                : task.getException().getMessage();

                        Toast.makeText(
                                this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private int dp(int value) {
        return (int) (
                value * getResources().getDisplayMetrics().density + 0.5f
        );
    }
}
