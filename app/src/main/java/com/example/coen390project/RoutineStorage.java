
package com.example.coen390project;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class RoutineStorage {

    private static final String PREF_NAME = "WorkoutRoutinePreferences";
    private static final String KEY_SESSIONS = "scheduledSessions";

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static JSONArray getAllSessions(Context context) {
        String saved = getPreferences(context).getString(KEY_SESSIONS, "[]");

        try {
            return new JSONArray(saved);
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    public static void addSession(
            Context context,
            String date,
            String time,
            JSONArray exercises,
            boolean repeatWeekly) {

        JSONArray sessions = getAllSessions(context);

        try {
            JSONObject session = new JSONObject();

            session.put("id", UUID.randomUUID().toString());
            session.put("date", date);
            session.put("time", time);
            session.put("exercises", exercises);
            session.put("repeatWeekly", repeatWeekly);

            sessions.put(session);

            getPreferences(context).edit()
                    .putString(KEY_SESSIONS, sessions.toString())
                    .apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONArray getSessionsForDate(Context context, String selectedDate) {

        JSONArray allSessions = getAllSessions(context);
        JSONArray matchingSessions = new JSONArray();

        for (int i = 0; i < allSessions.length(); i++) {

            JSONObject session = allSessions.optJSONObject(i);

            if (session == null) {
                continue;
            }

            String savedDate = session.optString("date", "");
            boolean repeatWeekly = session.optBoolean("repeatWeekly", false);

            if (savedDate.equals(selectedDate)) {
                matchingSessions.put(session);
            } else if (repeatWeekly && isSameWeeklyDay(savedDate, selectedDate)) {
                matchingSessions.put(session);
            }
        }

        return matchingSessions;
    }

    private static boolean isSameWeeklyDay(String savedDate, String selectedDate) {

        try {
            SimpleDateFormat format =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            format.setLenient(false);

            Date startDate = format.parse(savedDate);
            Date currentDate = format.parse(selectedDate);

            if (startDate == null || currentDate == null ||
                    currentDate.before(startDate)) {
                return false;
            }

            Calendar start = Calendar.getInstance();
            Calendar current = Calendar.getInstance();

            start.setTime(startDate);
            current.setTime(currentDate);

            return start.get(Calendar.DAY_OF_WEEK) ==
                    current.get(Calendar.DAY_OF_WEEK);

        } catch (Exception e) {
            return false;
        }
    }

    public static void removeSession(Context context, String sessionId) {

        JSONArray allSessions = getAllSessions(context);
        JSONArray updatedSessions = new JSONArray();

        for (int i = 0; i < allSessions.length(); i++) {

            JSONObject session = allSessions.optJSONObject(i);

            if (session != null &&
                    !session.optString("id").equals(sessionId)) {
                updatedSessions.put(session);
            }
        }

        getPreferences(context).edit()
                .putString(KEY_SESSIONS, updatedSessions.toString())
                .apply();
    }
}
