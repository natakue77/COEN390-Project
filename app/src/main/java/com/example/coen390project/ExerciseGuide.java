
package com.example.coen390project;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ExerciseGuide {

    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.rgb(170, 177, 190);
    private static final int CARD = Color.rgb(41, 45, 53);
    private static final int ACCENT = Color.rgb(130, 170, 255);

    public static void show(Context context, String exerciseName) {

        String instructions = getInstructions(exerciseName);
        String muscles = getMuscles(exerciseName);
        String reminder = getReminder(exerciseName);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(context, 18), dp(context, 12),
                dp(context, 18), dp(context, 12));
        root.setBackgroundColor(CARD);

        ScrollView scroll = new ScrollView(context);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);

        TextView heading = new TextView(context);
        heading.setText(exerciseName);
        heading.setTextColor(WHITE);
        heading.setTextSize(22);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setPadding(0, 0, 0, dp(context, 12));

        content.addView(heading);

        TextView illustrationTitle = new TextView(context);
        illustrationTitle.setText("MOVEMENT DEMONSTRATION");
        illustrationTitle.setTextColor(ACCENT);
        illustrationTitle.setTextSize(12);
        illustrationTitle.setTypeface(null, Typeface.BOLD);

        content.addView(illustrationTitle);

        MovementIllustration illustration =
                new MovementIllustration(context, exerciseName);

        LinearLayout.LayoutParams illustrationParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(context, 210)
                );

        illustrationParams.setMargins(
                0, dp(context, 8), 0, dp(context, 12)
        );

        illustration.setLayoutParams(illustrationParams);
        content.addView(illustration);

        addSection(context, content, "HOW TO PERFORM", instructions);
        addSection(context, content, "MAIN MUSCLES", muscles);
        addSection(context, content, "FORM REMINDER", reminder);

        TextView note = new TextView(context);
        note.setText(
                "Illustration is a simplified movement reference. " +
                        "Use controlled motion and choose a comfortable resistance."
        );
        note.setTextColor(MUTED);
        note.setTextSize(11);
        note.setPadding(0, dp(context, 10), 0, dp(context, 6));

        content.addView(note);

        scroll.addView(content);
        root.addView(scroll);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(root)
                .setPositiveButton("Close", null)
                .create();

        dialog.setOnShowListener(d -> {

            if (dialog.getWindow() != null) {
                GradientDrawable background = new GradientDrawable();
                background.setColor(CARD);
                background.setCornerRadius(dp(context, 18));
                dialog.getWindow().setBackgroundDrawable(background);
            }

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(WHITE);
        });

        dialog.show();
    }

    private static void addSection(
            Context context,
            LinearLayout parent,
            String title,
            String body) {

        TextView heading = new TextView(context);
        heading.setText(title);
        heading.setTextColor(ACCENT);
        heading.setTextSize(12);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setPadding(0, dp(context, 12), 0, dp(context, 4));

        TextView description = new TextView(context);
        description.setText(body);
        description.setTextColor(WHITE);
        description.setTextSize(14);
        description.setLineSpacing(dp(context, 3), 1f);

        parent.addView(heading);
        parent.addView(description);
    }

    private static String getInstructions(String name) {

        String n = name.toLowerCase();

        if (n.contains("curl")) {
            return "Start with your arms extended and elbows close to your sides. " +
                    "Bend your elbows to lift the weight toward your shoulders. " +
                    "Lower slowly until your arms return to the starting position.";
        }

        if (n.contains("press") || n.contains("push-up")
                || n.contains("pushdown")) {
            return "Begin in a stable position with your hands aligned for the exercise. " +
                    "Push the weight or your body through the movement. " +
                    "Return slowly without locking your joints aggressively.";
        }

        if (n.contains("squat") || n.contains("leg press")
                || n.contains("lunge")) {
            return "Stand with your feet in a comfortable position. " +
                    "Bend your hips and knees while keeping your chest controlled. " +
                    "Push through your feet to return to the starting position.";
        }

        if (n.contains("raise") || n.contains("fly")) {
            return "Start with your arms in front of or beside your body. " +
                    "Move your arms through a controlled arc. " +
                    "Pause briefly, then return slowly to the starting position.";
        }

        if (n.contains("deadlift") || n.contains("row")) {
            return "Start with a stable stance and a neutral back. " +
                    "Move through your hips and shoulders while keeping the load close. " +
                    "Return under control without rounding your back.";
        }

        if (n.contains("plank")) {
            return "Support yourself on your forearms and toes. " +
                    "Keep your body in a straight line from shoulders to heels. " +
                    "Brace your abdomen and hold without letting your hips sag.";
        }

        if (n.contains("crunch") || n.contains("twist")
                || n.contains("leg raise") || n.contains("flutter")
                || n.contains("mountain")) {
            return "Begin with your core engaged. " +
                    "Move your torso or legs slowly while keeping your lower back controlled. " +
                    "Avoid using momentum to complete repetitions.";
        }

        if (n.contains("pull-up") || n.contains("pulldown")
                || n.contains("hang")) {
            return "Start with a secure grip and controlled shoulders. " +
                    "Pull using your back and arms, keeping your torso steady. " +
                    "Return slowly to the starting position.";
        }

        return "Begin in a stable position with the equipment secured. " +
                "Move slowly through the exercise's comfortable range of motion. " +
                "Return to the starting position with control.";
    }

    private static String getMuscles(String name) {

        String n = name.toLowerCase();

        if (n.contains("curl")) return "Biceps and forearms.";
        if (n.contains("tricep")) return "Triceps.";
        if (n.contains("shoulder") || n.contains("lateral")
                || n.contains("front raise") || n.contains("arnold")) {
            return "Shoulders and supporting upper-body muscles.";
        }
        if (n.contains("squat") || n.contains("lunge")
                || n.contains("leg") || n.contains("calf")) {
            return "Quadriceps, hamstrings, glutes and supporting leg muscles.";
        }
        if (n.contains("chest") || n.contains("bench")
                || n.contains("push-up") || n.contains("pullover")) {
            return "Chest, shoulders and triceps.";
        }
        if (n.contains("row") || n.contains("pull")
                || n.contains("deadlift")) {
            return "Back, lats and supporting arm muscles.";
        }
        if (n.contains("plank") || n.contains("crunch")
                || n.contains("twist") || n.contains("abs")
                || n.contains("mountain") || n.contains("flutter")) {
            return "Abdominal muscles and core stabilizers.";
        }

        return "Primary muscles depend on the exercise variation.";
    }

    private static String getReminder(String name) {

        String n = name.toLowerCase();

        if (n.contains("deadlift") || n.contains("row")
                || n.contains("squat")) {
            return "Keep your back controlled, brace your core, and avoid using a load " +
                    "that makes you lose your position.";
        }

        if (n.contains("press") || n.contains("push")) {
            return "Keep your wrists aligned and avoid bouncing or forcing the movement.";
        }

        if (n.contains("curl") || n.contains("raise")) {
            return "Avoid swinging your body. Use a weight you can control throughout the movement.";
        }

        return "Keep your movement controlled, breathe steadily, and stop if you feel pain.";
    }

    private static int dp(Context context, int value) {
        return (int) (
                value * context.getResources().getDisplayMetrics().density + 0.5f
        );
    }

    // Simplified two-position movement illustration.
    private static class MovementIllustration extends View {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final String exercise;

        MovementIllustration(Context context, String exercise) {
            super(context);
            this.exercise = exercise.toLowerCase();
            setBackgroundColor(Color.rgb(29, 32, 39));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float width = getWidth();
            float height = getHeight();

            paint.setColor(Color.rgb(75, 82, 96));
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.STROKE);

            canvas.drawRoundRect(
                    2, 2, width - 2, height - 2,
                    18, 18, paint
            );

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(WHITE);
            paint.setTextSize(12);
            paint.setTypeface(Typeface.DEFAULT_BOLD);


            float centerY = height * 0.60f;



            paint.setColor(ACCENT);
            paint.setTextSize(30);


            paint.setColor(MUTED);
            paint.setTextSize(10);
            paint.setTypeface(Typeface.DEFAULT);

        }


    }
}
