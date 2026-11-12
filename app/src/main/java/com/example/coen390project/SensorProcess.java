package com.example.coen390project;

public class SensorProcess {
    private double vx,vy,vz, x,y,z;
    private double ax,ay,az;

    private double qw, qx, qy, qz;
    private double  gx, gy, gz;
    private long previousTime;

    private double worldAX, worldAY, worldAZ;

    private double stationaryTime = 0;

    private RepCounter repCounter = new RepCounter();

    private double maxX = Double.NEGATIVE_INFINITY;
    private double minX = Double.POSITIVE_INFINITY;
    private long lastPrint = 0;




    public void processSample(BLEManager.SensorSample sample){

        //android.util.Log.d("BarbellBLE", "SensorProcess reached: " + sample.sequence);
        ax =  (sample.ax*9.80665);
        ay =  (sample.ay*9.80665);
        az =  (sample.az*9.80665);
        gx = sample.gx;
        gy = sample.gy;
        gz = sample.gz;// Gyroscope X, in degrees/s
        qw = sample.qw;
        qx = sample.qx;
        qy = sample.qy;
        qz = sample.qz;// Quaternion W


        //accel. due to gravity
        double grav_x,grav_y,grav_z;
        grav_x = 2*(qx*qz-qw*qy)*9.80665;
        grav_y = 2*(qy*qz+qw*qx)*9.80665;
        grav_z = (1-2*((qx*qx)+(qy*qy)))*9.80665;

        double alx, aly,alz;

        alx = ax - grav_x;
        aly = ay - grav_y;
        alz = az - grav_z;


        worldAX = (1 - 2 * (qy*qy + qz*qz)) * alx + 2 * (qx*qy - qw*qz) * aly + 2 * (qx*qz + qw*qy) * alz;
        worldAY = 2 * (qx*qy + qw*qz) * alx + (1 - 2 * (qx*qx + qz*qz)) * aly + 2 * (qy*qz - qw*qx) * alz;
        worldAZ = 2 * (qx*qz - qw*qy) * alx + 2 * (qy*qz + qw*qx) * aly + (1 - 2 * (qx*qx + qy*qy)) * alz;

        maxX = Math.max(maxX, worldAX);
        minX = Math.min(minX, worldAX);

        long now = android.os.SystemClock.elapsedRealtime();

        if (now - lastPrint >= 1000) {
            android.util.Log.d("BarbellBLE",
                    "X Maximum: " + maxX + " | X Minimum: " + minX);
            android.util.Log.d("BarbellBLE",
                    "Position X: " + x + " m | Velocity X: " + vx + " m/s");

            maxX = Double.NEGATIVE_INFINITY;
            minX = Double.POSITIVE_INFINITY;
            lastPrint = now;
        }


        /*if (sample.sequence % 100 == 0) {
            android.util.Log.d("BarbellMotion",
                    "AX: " + worldAX +
                            " AY: " + worldAY +
                            " AZ: " + worldAZ);
        }

        android.util.Log.d("BarbellBLE",
                "World: " + worldAX + ", " + worldAY + ", " + worldAZ +
                        " | Raw: " + sample.ax + ", " + sample.ay + ", " + sample.az +
                        " | Q: " + qw + ", " + qx + ", " + qy + ", " + qz
        );

        if (Math.abs(worldAX) > 1.0 ||
                Math.abs(worldAY) > 1.0 ||
                Math.abs(worldAZ) > 1.0) {

            android.util.Log.d("BarbellBLE",
                    "SPIKE | Raw AX: " + sample.ax +
                            " AY: " + sample.ay +
                            " AZ: " + sample.az +
                            " | Q: " + qw + ", " + qx + ", " + qy + ", " + qz
            );
        }*/

        // in microseconds
        long currentTime = sample.timestampUs;
        if (previousTime == 0){
            previousTime = sample.timestampUs;

            return;}

        double dt = ((currentTime-previousTime) & 0xFFFFFFFFL)/1_000_000.0;
        previousTime = currentTime;

        if(dt <= 0  || dt > 0.1){
                return;

            }

        vx += worldAX*dt;
        vy += worldAY * dt;
        vz += worldAZ * dt;

        if (isStationary(worldAX,worldAY,worldAZ)){
            stationaryTime += dt;
        } else {stationaryTime = 0;}

        if (stationaryTime >= 0.2) {
            vx = 0;
            vy = 0;
            vz = 0;
        }

        x += vx*dt;
        y += vy * dt;
        z += vz * dt;

        boolean repCompleted = repCounter.update(
                (float) z,
                (float) vz,
                sample.timestampUs / 1000L
        );
        int reps = repCounter.getreps();

        if (repCompleted) {
            android.util.Log.d("BarbellBLE", "REP! total = " + reps);
        }




    }

    public void reset(){
        vx =0;
        vy =0;
        vz =0;
        x =0;
        y =0;
        z =0;
        previousTime =0;
    }
    private boolean isStationary(double ax, double ay, double az) {
        return Math.abs(ax) < 0.15 &&
                Math.abs(ay) < 0.15 &&
                Math.abs(az) < 0.15;
    }

}
