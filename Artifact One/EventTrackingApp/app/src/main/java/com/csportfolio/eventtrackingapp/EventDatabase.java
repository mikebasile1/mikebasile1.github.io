package com.csportfolio.eventtrackingapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * EventDatabase is a helper class for managing the SQLite database
 * used to store, retrieve, update, and delete event records.
 */
public class EventDatabase extends SQLiteOpenHelper {

    // Event Logging
    private static final String TAG = EventDatabase.class.getSimpleName();

    // Database Constants
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "event.db";
    private static final String TABLE_EVENT = "events";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_START_TIME = "start_time";
    private static final String COLUMN_END_TIME = "end_time";
    private static final String COLUMN_DESCRIPTION = "description";

    // Constructor
    EventDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is first created
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + TABLE_EVENT + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_TITLE + " TEXT, " +
                        COLUMN_DATE + " TEXT, " +
                        COLUMN_START_TIME + " TEXT, " +
                        COLUMN_END_TIME + " TEXT, " +
                        COLUMN_DESCRIPTION + " TEXT);";
        db.execSQL(query);
        Log.d(TAG, "Database table created: " + TABLE_EVENT);  // logs event
    }

    /**
     * Called when the database needs to be upgraded
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT);
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);  // logs event

        onCreate(db);
        Log.d(TAG, "Table dropped and recreated: " + TABLE_EVENT);  // logs event
    }

    /**
     * Retrieves all events from the database
     */
    public Cursor readAllEvents() {
        SQLiteDatabase db = this.getReadableDatabase();

        if (db == null) {  // null check
            Log.e(TAG, "readAllEvents: Failed to get readable database");  // logs event
            return null;
        }

        Log.d(TAG, "readAllEvents: Querying all events from database");  // logs event
        return db.rawQuery("SELECT * FROM " + TABLE_EVENT, null);
    }

    /**
     * Retrieves a single event by its ID
     */
    public EventStructure readEventById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EVENT + " WHERE " +
                        COLUMN_ID + "=?", new String[]{id});
        EventStructure event = null;

        if (cursor.moveToFirst()) {
            String eventId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
            String startTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_TIME));
            String endTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_TIME));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));

            event = new EventStructure(eventId, title, date, startTime, endTime, description);
            Log.d(TAG, "Event found for ID: " + id + " Title: " + title);  // logs event
        }else {
            Log.w(TAG, "No event found for ID: " + id);  // logs event
        }

        cursor.close();
        db.close();
        return event;
    }

    /**
     * Returns a list of all events from the database as EventStructure objects.
     */
    public List<EventStructure> readAllEventsList() {
        List<EventStructure> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        if (db == null) {
            Log.e(TAG, "readAllEventsList: Failed to get readable database");
            return eventList;
        }

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EVENT, null);

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String startTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_TIME));
                String endTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_TIME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));

                EventStructure event = new EventStructure(id, title, date, startTime, endTime, description);
                eventList.add(event);
            } while (cursor.moveToNext());

            Log.d(TAG, "readAllEventsList: Loaded " + eventList.size() + " events");
        } else {
            Log.w(TAG, "readAllEventsList: No events found in database");
        }

        cursor.close();
        db.close();
        return eventList;
    }

    /**
     * Adds a new event to the database
     */
    public long addEvent(String title, String date, String startTime, String endTime, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_START_TIME, startTime);
        values.put(COLUMN_END_TIME, endTime);
        values.put(COLUMN_DESCRIPTION, description);

        long result = db.insert(TABLE_EVENT, null, values);

        if (result == -1) {
            Log.e(TAG, "Failed to insert event: " + title);  // logs event
        } else {
            Log.d(TAG, "Event inserted with ID: " + result);  // logs event
        }

        db.close();
        return result;
    }

    /**
     * Updates an existing event
     */
    public boolean updateEvent(String row_id, String title, String date, String startTime,
                               String endTime, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_START_TIME, startTime);
        values.put(COLUMN_END_TIME, endTime);
        values.put(COLUMN_DESCRIPTION, description);

        int result = db.update(TABLE_EVENT, values, COLUMN_ID + "=?", new String[]{row_id});
        db.close();

        Log.d(TAG, "updateEvent result for ID " + row_id + ": " + (result > 0));  // logs event
        return result > 0;
    }

    /**
     * Deletes an event from the database
     */
    public boolean deleteEvent(String row_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_EVENT, COLUMN_ID + "=?", new String[]{row_id});
        db.close();

        Log.d(TAG, "deleteEvent result for ID " + row_id + ": " + (result > 0));  // logs event
        return result > 0;
    }
}