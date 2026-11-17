
package com.example.coen390project;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ProgressGraphView extends View {

    private static final int BACKGROUND =
            Color.rgb(32, 35, 41);

    private static final int GRID =
            Color.rgb(65, 70, 80);

    private static final int TEXT =
            Color.rgb(143, 149, 161);

    private static final int WHITE =
            Color.WHITE;

    private static final int RED =
            Color.rgb(239, 83, 80);

    private static final int YELLOW =
            Color.rgb(255, 193, 7);

    private String[] labels = new String[0];
    private float[] values = new float[0];

    private String[] seriesNames = new String[0];
    private float[][] seriesValues = new float[0][];
    private int[] seriesColors = new int[0];

    private float target;
    private boolean barChart;
    private boolean showTarget;

    private String unit = "";
    private String message = "";

    private boolean multiLineMode = false;

    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final int[] defaultColors = {
            Color.rgb(239, 83, 80),
            Color.rgb(66, 165, 245),
            Color.rgb(102, 187, 106),
            Color.rgb(255, 202, 40),
            Color.rgb(171, 71, 188),
            Color.rgb(38, 198, 218),
            Color.rgb(255, 167, 38),
            Color.rgb(236, 64, 122)
    };

    private float density;

    public ProgressGraphView(Context context) {
        super(context);
        initialize();
    }

    public ProgressGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ProgressGraphView(
            Context context,
            AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        density = getResources()
                .getDisplayMetrics().density;

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    private float dp(float value) {
        return value * density;
    }

    // =================================================
    // SINGLE-SERIES GRAPH
    // =================================================

    public void setData(
            String[] labels,
            float[] values,
            float target,
            boolean barChart,
            String unit,
            boolean showTarget) {

        this.labels = labels == null
                ? new String[0] : labels.clone();

        this.values = values == null
                ? new float[0] : values.clone();

        this.target = target;
        this.barChart = barChart;
        this.unit = unit == null ? "" : unit;
        this.showTarget = showTarget;

        multiLineMode = false;
        message = "";

        seriesNames = new String[0];
        seriesValues = new float[0][];
        seriesColors = new int[0];

        invalidate();
    }

    // =================================================
    // MULTI-LINE GRAPH
    // =================================================

    public void setMultiLineData(
            String[] labels,
            String[] seriesNames,
            float[][] seriesValues,
            int[] colors) {

        this.labels = labels == null
                ? new String[0] : labels.clone();

        this.seriesNames = seriesNames == null
                ? new String[0] : seriesNames.clone();

        this.seriesValues = copySeries(seriesValues);

        this.seriesColors = colors == null
                ? new int[0] : colors.clone();

        multiLineMode = true;
        barChart = false;
        showTarget = false;
        target = 0;
        message = "";
        unit = "";

        invalidate();
    }

    private float[][] copySeries(float[][] source) {

        if (source == null) {
            return new float[0][];
        }

        float[][] result = new float[source.length][];

        for (int i = 0; i < source.length; i++) {
            result[i] = source[i] == null
                    ? new float[0] : source[i].clone();
        }

        return result;
    }

    // =================================================
    // MESSAGE DISPLAY
    // =================================================

    public void setMessage(String message) {

        this.message = message == null ? "" : message;

        labels = new String[0];
        values = new float[0];

        seriesNames = new String[0];
        seriesValues = new float[0][];
        seriesColors = new int[0];

        showTarget = false;
        multiLineMode = false;

        invalidate();
    }

    // =================================================
    // DRAW
    // =================================================

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        if (width <= 0 || height <= 0) return;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(BACKGROUND);

        canvas.drawRoundRect(
                new RectF(0, 0, width, height),
                dp(12),
                dp(12),
                paint
        );

        if (!message.isEmpty()) {
            drawMessage(canvas, width, height);
            return;
        }

        if (multiLineMode) {
            drawMultiLineGraph(canvas, width, height);
        } else {
            drawSingleGraph(canvas, width, height);
        }
    }

    private void drawMessage(
            Canvas canvas,
            float width,
            float height) {

        paint.setColor(TEXT);
        paint.setTextSize(dp(13));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawText(
                message,
                width / 2f,
                height / 2f,
                paint
        );
    }

    // =================================================
    // SINGLE GRAPH
    // =================================================

    private void drawSingleGraph(
            Canvas canvas,
            float width,
            float height) {

        if (values.length == 0 || labels.length == 0) {
            drawMessage(canvas, width, height);
            return;
        }

        int count = Math.min(values.length, labels.length);

        float left = dp(43);
        float right = width - dp(14);
        float top = dp(24);
        float bottom = height - dp(43);

        if (right <= left || bottom <= top) return;

        float maxValue = 0;

        for (int i = 0; i < count; i++) {
            if (Float.isFinite(values[i])) {
                maxValue = Math.max(maxValue, values[i]);
            }
        }

        if (showTarget && target > maxValue) {
            maxValue = target;
        }

        maxValue = niceMaximum(maxValue);

        drawGrid(
                canvas,
                left,
                right,
                top,
                bottom,
                maxValue
        );

        if (showTarget) {

            float targetY = mapY(
                    target,
                    maxValue,
                    top,
                    bottom
            );

            paint.setColor(YELLOW);
            paint.setStrokeWidth(dp(2));
            paint.setStyle(Paint.Style.STROKE);

            paint.setPathEffect(
                    new DashPathEffect(
                            new float[]{dp(7), dp(5)},
                            0
                    )
            );

            canvas.drawLine(
                    left,
                    targetY,
                    right,
                    targetY,
                    paint
            );

            paint.setPathEffect(null);
            paint.setStyle(Paint.Style.FILL);
        }

        if (barChart) {

            drawBars(
                    canvas,
                    left,
                    right,
                    top,
                    bottom,
                    maxValue,
                    count
            );

            drawCenteredBarLabels(
                    canvas,
                    left,
                    right,
                    bottom,
                    count
            );

        } else {

            drawSingleLine(
                    canvas,
                    left,
                    right,
                    top,
                    bottom,
                    maxValue,
                    count
            );

            drawXLabels(
                    canvas,
                    left,
                    right,
                    bottom,
                    count
            );
        }
    }

    // =================================================
    // BAR LABELS
    // =================================================

    private void drawCenteredBarLabels(
            Canvas canvas,
            float left,
            float right,
            float bottom,
            int count) {

        if (count == 0) return;

        float spacing = (right - left) / count;

        paint.setColor(TEXT);
        paint.setTextSize(dp(10));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < count; i++) {

            float x = left + spacing * (i + 0.5f);

            canvas.drawText(
                    labels[i],
                    x,
                    bottom + dp(20),
                    paint
            );
        }
    }

    // =================================================
    // BARS
    // =================================================

    private void drawBars(
            Canvas canvas,
            float left,
            float right,
            float top,
            float bottom,
            float maxValue,
            int count) {

        float spacing = (right - left) / count;

        float barWidth = Math.min(
                dp(28),
                spacing * 0.55f
        );

        for (int i = 0; i < count; i++) {

            float value = values[i];

            if (!Float.isFinite(value)) continue;

            float x = left + spacing * (i + 0.5f);

            float y = mapY(
                    value,
                    maxValue,
                    top,
                    bottom
            );

            paint.setColor(RED);
            paint.setStyle(Paint.Style.FILL);

            canvas.drawRoundRect(
                    x - barWidth / 2f,
                    y,
                    x + barWidth / 2f,
                    bottom,
                    dp(5),
                    dp(5),
                    paint
            );
        }
    }

    // =================================================
    // SINGLE LINE
    // =================================================

    private void drawSingleLine(
            Canvas canvas,
            float left,
            float right,
            float top,
            float bottom,
            float maxValue,
            int count) {

        float spacing = count <= 1
                ? 0 : (right - left) / (count - 1);

        Path path = new Path();
        boolean started = false;

        for (int i = 0; i < count; i++) {

            float value = values[i];

            if (!Float.isFinite(value)) {
                started = false;
                continue;
            }

            float x = count == 1
                    ? (left + right) / 2f
                    : left + spacing * i;

            float y = mapY(
                    value,
                    maxValue,
                    top,
                    bottom
            );

            if (!started) {
                path.moveTo(x, y);
                started = true;
            } else {
                path.lineTo(x, y);
            }
        }

        paint.setColor(RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2.5f));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        canvas.drawPath(path, paint);

        paint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < count; i++) {

            if (!Float.isFinite(values[i])) continue;

            float x = count == 1
                    ? (left + right) / 2f
                    : left + spacing * i;

            float y = mapY(
                    values[i],
                    maxValue,
                    top,
                    bottom
            );

            paint.setColor(RED);

            canvas.drawCircle(
                    x,
                    y,
                    dp(4),
                    paint
            );
        }
    }

    // =================================================
    // MULTI-LINE GRAPH
    // =================================================

    private void drawMultiLineGraph(
            Canvas canvas,
            float width,
            float height) {

        int count = labels.length;

        if (count == 0 || seriesNames.length == 0) {
            drawMessage(canvas, width, height);
            return;
        }

        float left = dp(43);
        float right = width - dp(14);

        float legendHeight = calculateLegendHeight(width);

        float top = dp(20);
        float bottom = height - dp(45) - legendHeight;

        if (bottom <= top) {
            bottom = height - dp(65);
        }

        if (right <= left || bottom <= top) return;

        float maxValue = 0;

        for (int s = 0; s < seriesValues.length; s++) {

            if (seriesValues[s] == null) continue;

            for (float value : seriesValues[s]) {

                if (Float.isFinite(value)) {
                    maxValue = Math.max(maxValue, value);
                }
            }
        }

        maxValue = niceMaximum(maxValue);

        drawGrid(
                canvas,
                left,
                right,
                top,
                bottom,
                maxValue
        );

        float spacing = count <= 1
                ? 0 : (right - left) / (count - 1);

        for (int s = 0; s < seriesNames.length; s++) {

            float[] values = s < seriesValues.length
                    ? seriesValues[s] : new float[0];

            int color = getSeriesColor(s);

            drawOneSeries(
                    canvas,
                    left,
                    right,
                    top,
                    bottom,
                    maxValue,
                    spacing,
                    count,
                    values,
                    color
            );
        }

        drawXLabels(
                canvas,
                left,
                right,
                bottom,
                count
        );

        drawLegend(
                canvas,
                width,
                height,
                legendHeight
        );
    }

    private void drawOneSeries(
            Canvas canvas,
            float left,
            float right,
            float top,
            float bottom,
            float maxValue,
            float spacing,
            int count,
            float[] values,
            int color) {

        Path path = new Path();
        boolean started = false;

        int available = Math.min(count, values.length);

        for (int i = 0; i < available; i++) {

            float value = values[i];

            if (!Float.isFinite(value)) {
                started = false;
                continue;
            }

            float x = count == 1
                    ? (left + right) / 2f
                    : left + spacing * i;

            float y = mapY(
                    value,
                    maxValue,
                    top,
                    bottom
            );

            if (!started) {
                path.moveTo(x, y);
                started = true;
            } else {
                path.lineTo(x, y);
            }
        }

        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2.5f));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        canvas.drawPath(path, paint);

        paint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < available; i++) {

            float value = values[i];

            if (!Float.isFinite(value)) continue;

            float x = count == 1
                    ? (left + right) / 2f
                    : left + spacing * i;

            float y = mapY(
                    value,
                    maxValue,
                    top,
                    bottom
            );

            paint.setColor(color);

            canvas.drawCircle(
                    x,
                    y,
                    dp(4),
                    paint
            );
        }
    }

    private int getSeriesColor(int index) {

        if (index < seriesColors.length) {
            return seriesColors[index];
        }

        return defaultColors[index % defaultColors.length];
    }

    // =================================================
    // LEGEND
    // =================================================

    private float calculateLegendHeight(float width) {

        if (seriesNames.length == 0) return 0;

        float availableWidth = width - dp(28);
        float currentWidth = 0;
        int rows = 1;

        paint.setTextSize(dp(11));

        for (String name : seriesNames) {

            float itemWidth =
                    dp(20) + paint.measureText(name) + dp(18);

            if (currentWidth + itemWidth > availableWidth
                    && currentWidth > 0) {

                rows++;
                currentWidth = 0;
            }

            currentWidth += itemWidth;
        }

        return rows * dp(23) + dp(5);
    }

    private void drawLegend(
            Canvas canvas,
            float width,
            float height,
            float legendHeight) {

        if (seriesNames.length == 0) return;

        float currentX = dp(14);
        float startY = height - legendHeight + dp(8);
        float currentY = startY;

        paint.setTextSize(dp(11));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < seriesNames.length; i++) {

            String name = seriesNames[i];

            float textWidth = paint.measureText(name);
            float itemWidth = dp(20) + textWidth + dp(18);

            if (currentX + itemWidth > width - dp(14)
                    && currentX > dp(14)) {

                currentX = dp(14);
                currentY += dp(23);
            }

            paint.setColor(getSeriesColor(i));

            canvas.drawCircle(
                    currentX + dp(5),
                    currentY + dp(5),
                    dp(4),
                    paint
            );

            paint.setColor(WHITE);

            canvas.drawText(
                    name,
                    currentX + dp(14),
                    currentY + dp(9),
                    paint
            );

            currentX += itemWidth;
        }
    }

    // =================================================
    // GRID AND Y-AXIS
    // =================================================

    private void drawGrid(
            Canvas canvas,
            float left,
            float right,
            float top,
            float bottom,
            float maxValue) {

        // Keep all five gridlines, including fractional intervals.
        int divisions = 4;

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(GRID);

        for (int i = 0; i <= divisions; i++) {

            float y = top
                    + (bottom - top) * i / divisions;

            canvas.drawLine(
                    left,
                    y,
                    right,
                    y,
                    paint
            );

            float value = maxValue
                    * (divisions - i) / divisions;

            // Only display whole-number labels.
            // Fractional gridlines remain visible without text.
            if (Math.abs(value - Math.round(value)) < 0.001f) {

                paint.setStyle(Paint.Style.FILL);
                paint.setColor(TEXT);
                paint.setTextSize(dp(10));
                paint.setTextAlign(Paint.Align.RIGHT);

                canvas.drawText(
                        String.valueOf(Math.round(value)),
                        left - dp(7),
                        y + dp(4),
                        paint
                );
            }

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(GRID);
        }

        paint.setStyle(Paint.Style.FILL);
    }

    // =================================================
    // X-AXIS LABELS
    // =================================================

    private void drawXLabels(
            Canvas canvas,
            float left,
            float right,
            float bottom,
            int count) {

        if (count == 0) return;

        float spacing = count <= 1
                ? 0 : (right - left) / (count - 1);

        paint.setColor(TEXT);
        paint.setTextSize(dp(10));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);

        int step = Math.max(
                1,
                (int) Math.ceil(count / 7f)
        );

        for (int i = 0; i < count; i++) {

            if (i % step != 0 && i != count - 1) {
                continue;
            }

            float x = count == 1
                    ? (left + right) / 2f
                    : left + spacing * i;

            canvas.drawText(
                    labels[i],
                    x,
                    bottom + dp(20),
                    paint
            );
        }
    }

    // =================================================
    // Y-AXIS SCALING
    // =================================================

    private float mapY(
            float value,
            float maxValue,
            float top,
            float bottom) {

        if (maxValue <= 0) return bottom;

        float fraction = value / maxValue;

        return bottom - fraction * (bottom - top);
    }

    private float niceMaximum(float value) {

        // Weekly workout bar charts use whole-number intervals.
        if (barChart) {
            return Math.max(
                    4f,
                    (float) (Math.ceil(value / 4f) * 4f)
            );
        }

        // Keep the original scaling for line graphs.
        if (value <= 0) return 4;

        double exponent = Math.floor(
                Math.log10(value)
        );

        double magnitude = Math.pow(10, exponent);
        double normalized = value / magnitude;

        double nice;

        if (normalized <= 1) {
            nice = 1;
        } else if (normalized <= 2) {
            nice = 2;
        } else if (normalized <= 5) {
            nice = 5;
        } else {
            nice = 10;
        }

        return (float) (nice * magnitude);
    }
}
