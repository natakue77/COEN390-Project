package com.example.coen390project;

public class RepCounter {
    public enum Phase{
        LOWERING,
        READY,
        RISING

    }
    private Phase phase = Phase.READY;
    private int reps = 0;
    private float Theight;
    private float Bheight;
    private long topHoldStart = -1;

//To be tuned
    private static final float MIN_REP_DEPTH = 0.08f; // 8 cm
    private static final float MOVE_SPEED = 0.04f;    // m/s
    private static final long TOP_HOLD_MS = 150;

    public void reset(float startH){
        reps = 0;
        phase = Phase.READY;
        Theight = startH;
        Bheight = startH;
        topHoldStart = -1;

    }

    public boolean update(float height, float velocity, long time){
        boolean repComplete;
        switch (phase){
            case READY:

                if (velocity < -MOVE_SPEED){
                    phase = Phase.LOWERING;
                    Bheight = height;
                }else if (Math.abs(velocity)<MOVE_SPEED){
                    Theight = height;
                }break;

            case LOWERING:
                Bheight = Math.min(Bheight,height);
                float depth = Theight - Bheight;
                if (depth >= MIN_REP_DEPTH && velocity >MOVE_SPEED){
                    phase = Phase.RISING;
                }
                if (depth >= MIN_REP_DEPTH && height>= Theight && Math.abs(velocity) < MOVE_SPEED){phase= Phase.READY;}
                break; // without this, LOWERING fell through into RISING and counted a rep on almost every sample

            case RISING:
                float repDepth = Theight - Bheight;
                float tol = Math.max (0.03f, repDepth*0.20f); // 3 cm (was 0.3f = 30 cm, larger than a whole rep)
                boolean neartop = height >= Theight -tol;
                if (neartop){
                    if (Math.abs(velocity) < MOVE_SPEED) {
                        if (topHoldStart == -1) {
                            topHoldStart = time;
                        }
                    if (time - topHoldStart >= TOP_HOLD_MS){
                        reps++;
                        phase = Phase.READY;
                        Theight = height;
                        topHoldStart = -1;
                        return true;
                    }

                    } else if (velocity < -MOVE_SPEED) {


                        reps++;
                        phase = Phase.LOWERING;
                        Theight = height;
                        Bheight = height;
                        topHoldStart = -1;
                        return true;

                    } else {
                        topHoldStart = -1;
                    }

                } else {
                    topHoldStart = -1;
                }
                break;
        }

        return false;
    }
    public int getreps(){
        return reps;
    }
    public Phase getPhase(){
        return phase;
    }


                }









