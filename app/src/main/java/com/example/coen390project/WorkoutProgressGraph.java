
package com.example.coen390project;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class WorkoutProgressGraph extends View {

    private Paint paint;
    private float[] workoutData = {};

    public WorkoutProgressGraph(Context context) {
        super(context);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(40);
    }

    public WorkoutProgressGraph(
            Context context,
            android.util.AttributeSet attrs) {

        super(context, attrs);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(40);
    }


    // Update the graph with workout data
    public void setWorkoutData(float[] data) {
        workoutData = data;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        canvas.drawColor(Color.WHITE);

        // Display a message if no workout data exists
        if (workoutData == null || workoutData.length == 0) {
            paint.setColor(Color.GRAY);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(
                    "No workout data available",
                    width / 2,
                    height / 2,
                    paint
            );
            return;
        }

        // Draw the graph axes
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);

        float left = 70;
        float bottom = height - 60;
        float top = 40;
        float right = width - 30;

        canvas.drawLine(left, top, left, bottom, paint);
        canvas.drawLine(left, bottom, right, bottom, paint);

        // Find the largest value
        float max = 1;

        for (float value : workoutData) {
            if (value > max) {
                max = value;
            }
        }

        // Draw the line graph
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(5);

        for (int i = 1; i < workoutData.length; i++) {

            float x1 = left + (i - 1) *
                    (right - left) / Math.max(1, workoutData.length - 1);

            float y1 = bottom - workoutData[i - 1] *
                    (bottom - top) / max;

            float x2 = left + i *
                    (right - left) / Math.max(1, workoutData.length - 1);

            float y2 = bottom - workoutData[i] *
                    (bottom - top) / max;

            canvas.drawLine(x1, y1, x2, y2, paint);
        }
    }
}
