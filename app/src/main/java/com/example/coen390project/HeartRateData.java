package com.example.coen390project;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class HeartRateData {

    private static final MutableLiveData<Integer> heartRate =
            new MutableLiveData<>(0);

    public static LiveData<Integer> getHeartRate() {
        return heartRate;
    }

    public static void setHeartRate(int bpm) {
        heartRate.setValue(bpm);
    }
}