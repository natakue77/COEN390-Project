
package com.example.coen390project;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExerciseProfileStorage {

    private static final String PREF_NAME = "ExerciseProfiles";

    public static void saveProfile(Context context,
                                   String exerciseName,
                                   RepCounter counter) {

        if (exerciseName == null || exerciseName.trim().isEmpty()
                || counter == null || !counter.isReady()) {
            return;
        }

        float[][] reference = counter.getReference();

        try {
            JSONArray points = new JSONArray();

            for (float[] row : reference) {
                JSONArray values = new JSONArray();

                for (float value : row) {
                    values.put(value);
                }

                points.put(values);
            }

            JSONObject profile = new JSONObject();
            profile.put("points", points);
            profile.put("strength", counter.getReferenceStrength());
            profile.put("duration", counter.getReferenceDurationUs());

            SharedPreferences preferences =
                    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

            preferences.edit()
                    .putString(exerciseName, profile.toString())
                    .apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static boolean loadProfile(Context context,
                                      String exerciseName,
                                      RepCounter counter) {

        if (exerciseName == null || counter == null) {
            return false;
        }

        SharedPreferences preferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String saved = preferences.getString(exerciseName, null);

        if (saved == null) {
            return false;
        }

        try {
            JSONObject profile = new JSONObject(saved);

            JSONArray points = profile.getJSONArray("points");
            float[][] reference = new float[points.length()][];

            for (int i = 0; i < points.length(); i++) {
                JSONArray values = points.getJSONArray(i);
                reference[i] = new float[values.length()];

                for (int j = 0; j < values.length(); j++) {
                    reference[i][j] = (float) values.getDouble(j);
                }
            }

            float strength = (float) profile.getDouble("strength");
            long duration = profile.getLong("duration");

            counter.loadReference(reference, strength, duration);

            return counter.isReady();

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
}
