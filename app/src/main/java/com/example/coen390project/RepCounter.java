package com.example.coen390project;

import java.util.ArrayList;
import java.util.List;

/** One-rep template learner and DTW-based rep counter. */
public class RepCounter {
    private static final int CHANNELS = 6;
    private static final int POINTS = 48;
    private static final long SAMPLE_INTERVAL_US = 20_000L; // ~50 Hz
    private static final long STOP_PAUSE_US = 500_000L;       // brief rest to end a rep
    private static final float ACCEL_TRIGGER_G = 0.045f;
    private static final float GYRO_TRIGGER = 0.04f;          // 0.04 * 150 = 6 deg/s
    private static final float MAX_DTW_SCORE = 1.10f;

    private double peakAccel = 0;
    private long lastDebugTime = 0;// tune with real data
    private long lastDebugMs = 0;

    private static class Frame {
        final long time;
        final float[] values;
        Frame(long time, float[] values) {
            this.time = time;
            this.values = values;
        }
    }

    private static class Profile {
        final float[][] points;
        final float strength;
        Profile(float[][] points, float strength) {
            this.points = points;
            this.strength = strength;
        }
    }

    private final ArrayList<Frame> segment = new ArrayList<>();
    private final ArrayList<Frame> preRoll = new ArrayList<>();
    private final float[] baseline = new float[3];
    private boolean baselineSet = false;
    private boolean moving = false;
    private long quietSince = -1;
    private long previousTime = -1;
    private float[][] reference;
    private float referenceStrength;
    private long referenceDurationUs;
    private float lastScore = Float.NaN;
    private int reps = 0;

    public void reset() {
        reference = null;
        referenceStrength = 0;
        referenceDurationUs = 0;
        reps = 0;
        lastScore = Float.NaN;
        clearMotion();
        previousTime = -1;
        baselineSet = false;
    }

    /** Save the reference rep. Returns false when the recording is too short or weak. */
    public boolean learn(List<BLEManager.SensorSample> recordedSamples) {
        reset();
        if (recordedSamples == null || recordedSamples.size() < 20) return false;
        ArrayList<Frame> frames = new ArrayList<>();
        long last = -1;
        for (BLEManager.SensorSample sample : recordedSamples) {
            long now = sample.timestampUs & 0xFFFFFFFFL;
            if (last != -1 && elapsed(now, last) < SAMPLE_INTERVAL_US) continue;
            frames.add(new Frame(now, features(sample)));
            last = now;
        }
        if (frames.size() < 20) return false;
        int[] bounds = activeBounds(frames);
        if (bounds == null) return false;
        int start = Math.max(0, bounds[0] - 10);
        int end = Math.min(frames.size(), bounds[1] + 11);
        referenceDurationUs = elapsed(frames.get(end - 1).time, frames.get(start).time);
        if (referenceDurationUs < 500_000L || referenceDurationUs > 12_000_000L) return false;
        Profile p = profile(frames, start, end);
        if (p.strength < 0.015f) return false;
        reference = p.points;
        referenceStrength = p.strength;
        reps = 1; // The learning repetition counts as the first rep.
        return true;
    }

    /** Feed incoming samples when not learning. Returns true once a valid rep ends. */
    public boolean update(BLEManager.SensorSample sample) {
        if (!isReady() || sample == null) return false;
        long now = sample.timestampUs & 0xFFFFFFFFL;
        if (previousTime != -1 && elapsed(now, previousTime) < SAMPLE_INTERVAL_US) return false;
        previousTime = now;
        Frame frame = new Frame(now, features(sample));
        float[] v = frame.values;
        if (!baselineSet) {
            System.arraycopy(v, 0, baseline, 0, 3);
            baselineSet = true;
        }
        double accelChange = Math.sqrt(
                sq(v[0]) +
                        sq(v[1]) +
                        sq(v[2])
        );
        double gyroChange = Math.sqrt(sq(v[3]) + sq(v[4]) + sq(v[5]));
        boolean active = accelChange > ACCEL_TRIGGER_G || gyroChange > GYRO_TRIGGER;

        peakAccel = Math.max(peakAccel, accelChange);

        long time = android.os.SystemClock.elapsedRealtime();

        long nowMs = android.os.SystemClock.elapsedRealtime();

        if (nowMs - lastDebugMs >= 1000) {
            lastDebugMs = nowMs;

            android.util.Log.d("BarbellBLE",
                    "Accel change: " + accelChange +
                            " | Gyro: " + (gyroChange * 150.0) +
                            " deg/s | Active: " + active +
                            " | Moving: " + moving);
        }

        if (!moving) {
            if (!active) {
                // Update the rest reference slowly to accommodate small orientation drift.
                for (int c = 0; c < 3; c++) baseline[c] += 0.02f * (v[c] - baseline[c]);
                preRoll.add(frame);
                if (preRoll.size() > 10) preRoll.remove(0);
                return false;
            }
            moving = true;
            segment.clear();
            segment.addAll(preRoll);
            preRoll.clear();
        }
        segment.add(frame);
        if (active) quietSince = -1;
        else if (quietSince == -1) quietSince = now;

        if (segment.size() > 650) { // cap a runaway recording (~13 seconds)
            clearMotion();
            return false;
        }
        if (quietSince == -1 || elapsed(now, quietSince) < STOP_PAUSE_US) return false;

        int[] bounds = activeBounds(segment);
        boolean counted = false;
        if (bounds != null) {
            int start = Math.max(0, bounds[0] - 10);
            int end = Math.min(segment.size(), bounds[1] + 11);
            long duration = elapsed(segment.get(end - 1).time, segment.get(start).time);
            if (duration >= referenceDurationUs * 0.50 &&
                    duration <= referenceDurationUs * 2.0) {
                Profile candidate = profile(segment, start, end);
                if (candidate.strength >= referenceStrength * 0.25f) {
                    lastScore = dtw(reference, candidate.points);
                    if (lastScore <= MAX_DTW_SCORE) {
                        reps++;
                        counted = true;
                    }
                }
            }
        }

        android.util.Log.d("BarbellBLE",
                "Rep finished | Samples: " + segment.size() +
                        " | DTW score: " + lastScore +
                        " | Counted: " + counted);
        clearMotion();
        return counted;
    }

    public int getreps() { return reps; }
    public boolean isReady() { return reference != null; }
    public float getLastMatchScore() { return lastScore; }

    private void clearMotion() {
        moving = false;
        quietSince = -1;
        segment.clear();
        preRoll.clear();
    }

    // Gyroscope channels are scaled to approximately match accel magnitudes.
    private static float[] features(BLEManager.SensorSample s) {
        float qw = s.qw;
        float qx = s.qx;
        float qy = s.qy;
        float qz = s.qz;

        // Gravity components in g
        float gravX = 2f * (qx*qz - qw*qy);
        float gravY = 2f * (qy*qz + qw*qx);
        float gravZ = 1f - 2f * (qx*qx + qy*qy);
        if (s.sequence % 300 == 0) {
            android.util.Log.d("BarbellBLE",
                    "Raw: " + s.ax + ", " + s.ay + ", " + s.az +
                            " | Gravity: " + gravX + ", " + gravY + ", " + gravZ);
        }

        return new float[]{
                s.ax - gravX,
                s.ay - gravY,
                s.az - gravZ,
                s.gx / 150f,
                s.gy / 150f,
                s.gz / 150f
        };
    }

    /** Finds activity using the accelerometer baseline and gyroscope. */
    private static int[] activeBounds(List<Frame> frames) {
        int baseN = Math.min(8, frames.size());
        float[] base = new float[3];
        for (int i = 0; i < baseN; i++) {
            for (int c = 0; c < 3; c++) base[c] += frames.get(i).values[c] / baseN;
        }
        int first = -1, last = -1;
        for (int i = 0; i < frames.size(); i++) {
            float[] v = frames.get(i).values;
            double da = Math.sqrt(sq(v[0] - base[0]) + sq(v[1] - base[1]) + sq(v[2] - base[2]));
            double dg = Math.sqrt(sq(v[3]) + sq(v[4]) + sq(v[5]));
            if (da > ACCEL_TRIGGER_G || dg > GYRO_TRIGGER) {
                if (first < 0) first = i;
                last = i;
            }
        }
        return first == -1 ? null : new int[]{first, last};
    }

    /** Interpolate to fixed length, mean-center and normalize amplitude. */
    private static Profile profile(List<Frame> frames, int from, int end) {
        int length = end - from;
        float[][] points = new float[POINTS][CHANNELS];
        for (int i = 0; i < POINTS; i++) {
            float p = (float) i * (length - 1) / (POINTS - 1);
            int left = (int) p;
            int right = Math.min(left + 1, length - 1);
            float t = p - left;
            for (int c = 0; c < CHANNELS; c++) {
                float a = frames.get(from + left).values[c];
                float b = frames.get(from + right).values[c];
                points[i][c] = a + t * (b - a);
            }
        }
        float[] means = new float[CHANNELS];
        for (float[] row : points) for (int c = 0; c < CHANNELS; c++) means[c] += row[c] / POINTS;
        double energy = 0;
        for (float[] row : points) {
            for (int c = 0; c < CHANNELS; c++) {
                row[c] -= means[c];
                energy += row[c] * row[c];
            }
        }
        float strength = (float) Math.sqrt(energy / POINTS);
        float scale = Math.max(0.0001f, strength);
        for (float[] row : points) for (int c = 0; c < CHANNELS; c++) row[c] /= scale;
        return new Profile(points, strength);
    }

    /** DTW allows a rep's speed to vary within its recorded pattern. */
    private static float dtw(float[][] a, float[][] b) {
        int n = a.length;
        float[] prev = new float[n + 1];
        float[] curr = new float[n + 1];
        java.util.Arrays.fill(prev, Float.POSITIVE_INFINITY);
        prev[0] = 0;
        for (int i = 1; i <= n; i++) {
            java.util.Arrays.fill(curr, Float.POSITIVE_INFINITY);
            for (int j = Math.max(1, i - 12); j <= Math.min(n, i + 12); j++) {
                float distance = 0;
                for (int c = 0; c < CHANNELS; c++) {
                    float difference = a[i - 1][c] - b[j - 1][c];
                    distance += difference * difference;
                }
                curr[j] = distance + Math.min(prev[j], Math.min(prev[j - 1], curr[j - 1]));
            }
            float[] swap = prev;
            prev = curr;
            curr = swap;
        }
        return (float) Math.sqrt(prev[n] / n);
    }

    private static long elapsed(long current, long previous) {
        return (current - previous) & 0xFFFFFFFFL;
    }
    private static double sq(double x) { return x * x; }
}
