
package com.example.coen390project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "WorkoutDatabase.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_WORKOUTS = "custom_workouts";

    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_CATEGORY = "category";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable = "CREATE TABLE " + TABLE_WORKOUTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_CATEGORY + " TEXT NOT NULL)";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUTS);
        onCreate(db);
    }

    // Add a custom workout
    public boolean addWorkout(String name, String description, String category) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_NAME, name);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_CATEGORY, category);

        long result = db.insert(TABLE_WORKOUTS, null, values);

        return result != -1;
    }

    // Get all custom workouts for a category
    public Cursor getCustomWorkouts(String category) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                TABLE_WORKOUTS,
                null,
                COL_CATEGORY + " = ?",
                new String[]{category},
                null,
                null,
                COL_NAME + " ASC"
        );
    }

    // Delete a custom workout
    public boolean deleteWorkout(int id) {

        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                TABLE_WORKOUTS,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        return result > 0;
    }
}
