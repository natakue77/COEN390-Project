
package com.example.coen390project;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WorkoutHistoryStorage {

    private static final String PREF_NAME = "WorkoutHistory";
    private static final String KEY_HISTORY = "sessions";

    public static boolean saveWorkout(Context context,
                                      String exerciseName,
                                      int reps) {

        if (exerciseName == null ||
                exerciseName.trim().isEmpty() || reps < 0) {
            return false;
        }

        SharedPreferences preferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        try {
            JSONArray history = new JSONArray(
                    preferences.getString(KEY_HISTORY, "[]"));

            JSONObject workout = new JSONObject();

            workout.put("exercise", exerciseName);
            workout.put("reps", reps);
            workout.put("date", System.currentTimeMillis());

            history.put(workout);

            return preferences.edit()
                    .putString(KEY_HISTORY, history.toString())
                    .commit();

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static JSONArray getHistory(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        try {
            return new JSONArray(
                    preferences.getString(KEY_HISTORY, "[]"));
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }
}
