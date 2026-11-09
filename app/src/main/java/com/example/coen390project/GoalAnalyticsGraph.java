
package com.example.coen390project;

import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class GoalAnalyticsGraph extends View {

    private Paint paint;
    private float currentProgress = 0;
    private float targetGoal = 0;

    public GoalAnalyticsGraph(Context context) {
        super(context);
        initialize();
    }

    // Required when the graph is created from XML
    public GoalAnalyticsGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(35);
    }

    // Update the graph with the user's progress and goal
    public void setGoalData(float progress, float goal) {
        currentProgress = progress;
        targetGoal = goal;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        canvas.drawColor(Color.WHITE);

        paint.setTextAlign(Paint.Align.CENTER);

        // No goal has been set yet
        if (targetGoal <= 0) {
            paint.setColor(Color.GRAY);
            canvas.drawText(
                    "No goal data available",
                    width / 2,
                    height / 2,
                    paint
            );
            return;
        }

        // Calculate goal completion percentage
        float percentage = Math.max(
                0,
                Math.min(100, (currentProgress / targetGoal) * 100)
        );

        // Background progress bar
        paint.setColor(Color.LTGRAY);

        canvas.drawRoundRect(
                40,
                height / 2 - 25,
                width - 40,
                height / 2 + 25,
                20,
                20,
                paint
        );

        // Completed progress bar
        paint.setColor(Color.rgb(76, 175, 80));

        float barWidth = (width - 80) * percentage / 100;

        canvas.drawRoundRect(
                40,
                height / 2 - 25,
                40 + barWidth,
                height / 2 + 25,
                20,
                20,
                paint
        );

        // Display percentage
        paint.setColor(Color.BLACK);

        canvas.drawText(
                String.format("%.0f%%", percentage),
                width / 2,
                height / 2 - 55,
                paint
        );
    }
}
