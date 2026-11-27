
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

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;

    private final int BACKGROUND = Color.rgb(17, 18, 20);
    private final int CARD = Color.rgb(41, 45, 53);
    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(143, 149, 161);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        // Already signed in? Go straight to Home.
        if (auth.getCurrentUser() != null) {
            openHome();
            return;
        }

        buildScreen();
    }

    private void buildScreen() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(28), dp(30), dp(28), dp(30));
        root.setBackgroundColor(BACKGROUND);

        TextView logo = new TextView(this);
        logo.setText("REP TRACKER");
        logo.setTextColor(WHITE);
        logo.setTextSize(29);
        logo.setTypeface(null, Typeface.BOLD);
        logo.setGravity(Gravity.CENTER);

        root.addView(logo);

        TextView subtitle = new TextView(this);
        subtitle.setText("YOUR FITNESS JOURNEY STARTS HERE");
        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(11);
        subtitle.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        subtitleParams.topMargin = dp(8);
        subtitleParams.bottomMargin = dp(35);

        root.addView(subtitle, subtitleParams);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(24), dp(20), dp(24));
        card.setBackground(roundBackground(CARD, 18));

        TextView heading = new TextView(this);
        heading.setText("Welcome Back!");
        heading.setTextColor(WHITE);
        heading.setTextSize(23);
        heading.setTypeface(null, Typeface.BOLD);

        card.addView(heading);

        TextView description = new TextView(this);
        description.setText("Sign in to continue your workouts.");
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
                "Password",
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        LinearLayout.LayoutParams passwordParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(54)
                );

        passwordParams.topMargin = dp(13);

        card.addView(passwordInput, passwordParams);

        TextView forgotPassword = new TextView(this);
        forgotPassword.setText("Forgot password?");
        forgotPassword.setTextColor(MUTED);
        forgotPassword.setTextSize(12);
        forgotPassword.setGravity(Gravity.END);
        forgotPassword.setPadding(0, dp(12), 0, dp(12));
        forgotPassword.setOnClickListener(v -> resetPassword());

        card.addView(forgotPassword);

        loginButton = createButton("Sign In");

        LinearLayout.LayoutParams loginParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(52)
                );

        loginParams.topMargin = dp(8);

        card.addView(loginButton, loginParams);

        loginButton.setOnClickListener(v -> signIn());

        root.addView(card, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout registerRow = new LinearLayout(this);
        registerRow.setGravity(Gravity.CENTER);
        registerRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView noAccount = new TextView(this);
        noAccount.setText("Don't have an account? ");
        noAccount.setTextColor(MUTED);
        noAccount.setTextSize(13);

        TextView register = new TextView(this);
        register.setText("Create Account");
        register.setTextColor(WHITE);
        register.setTextSize(13);
        register.setTypeface(null, Typeface.BOLD);
        register.setPadding(dp(3), dp(15), dp(3), dp(15));
        register.setOnClickListener(v ->
                startActivity(new Intent(
                        LoginActivity.this,
                        RegisterActivity.class
                ))
        );

        registerRow.addView(noAccount);
        registerRow.addView(register);

        LinearLayout.LayoutParams registerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        registerParams.topMargin = dp(14);

        root.addView(registerRow, registerParams);

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

    private void signIn() {

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please enter your email and password.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(
                                this,
                                "Welcome back!",
                                Toast.LENGTH_SHORT
                        ).show();

                        openHome();

                    } else {
                        loginButton.setEnabled(true);
                        loginButton.setText("Sign In");

                        String message = task.getException() == null
                                ? "Sign in failed."
                                : task.getException().getMessage();

                        Toast.makeText(
                                this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void resetPassword() {

        String email = emailInput.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(
                    this,
                    "Enter your email address first.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused ->
                        Toast.makeText(
                                this,
                                "Password reset email sent.",
                                Toast.LENGTH_LONG
                        ).show()
                )
                .addOnFailureListener(error ->
                        Toast.makeText(
                                this,
                                error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    private void openHome() {

        Intent intent = new Intent(this, MainActivity.class);

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    private int dp(int value) {
        return (int) (
                value * getResources().getDisplayMetrics().density + 0.5f
        );
    }
}
