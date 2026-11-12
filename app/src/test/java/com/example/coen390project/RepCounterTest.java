package com.example.coen390project;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Checks RepCounter with simulated movements, so the rep logic
 * can be tested without the bracelet.
 *
 * Each sample is (height in m, vertical velocity in m/s, time in ms),
 * the same values SensorProcess passes to RepCounter.update().
 */
public class RepCounterTest {

    private static final int HZ = 100;          // samples per second
    private long timeMs = 0;

    /** Bar held still at the top for the given number of seconds. */
    private void rest(RepCounter counter, double seconds) {
        for (int i = 0; i < seconds * HZ; i++) {
            timeMs += 1000 / HZ;
            counter.update(0f, 0f, timeMs);
        }
    }

    /** One smooth rep: down by depthM and back up, over durationS seconds. */
    private void rep(RepCounter counter, double depthM, double durationS) {
        int n = (int) (durationS * HZ);
        for (int i = 1; i <= n; i++) {
            timeMs += 1000 / HZ;
            double p = (double) i / n;
            double height = -depthM / 2 * (1 - Math.cos(2 * Math.PI * p));
            double velocity = -depthM / 2 * 2 * Math.PI
                    * Math.sin(2 * Math.PI * p) / durationS;
            counter.update((float) height, (float) velocity, timeMs);
        }
    }

    @Test
    public void threeSquatsWithPausesCountsThree() {
        RepCounter counter = new RepCounter();
        counter.reset(0f);

        for (int i = 0; i < 3; i++) {
            rest(counter, 1.0);
            rep(counter, 0.40, 2.0);   // 40 cm squat
        }
        rest(counter, 1.0);

        assertEquals(3, counter.getreps());
    }

    @Test
    public void tinyMovementIsNotARep() {
        RepCounter counter = new RepCounter();
        counter.reset(0f);

        rest(counter, 1.0);
        rep(counter, 0.04, 1.0);       // 4 cm wobble, below the 8 cm minimum
        rest(counter, 1.0);

        assertEquals(0, counter.getreps());
    }

    @Test
    public void standingStillCountsNothing() {
        RepCounter counter = new RepCounter();
        counter.reset(0f);

        rest(counter, 5.0);

        assertEquals(0, counter.getreps());
    }
}
