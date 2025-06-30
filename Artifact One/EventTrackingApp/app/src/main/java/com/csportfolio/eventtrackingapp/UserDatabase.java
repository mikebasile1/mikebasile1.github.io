package com.csportfolio.eventtrackingapp;

import android.util.Log;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context;

import androidx.annotation.Nullable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * UserDatabase manages user registration and authentication
 * using a local SQLite database.
 */
public class UserDatabase extends SQLiteOpenHelper {

    // Constants
    private static final String TAG = "UserDatabase";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "user.db";
    private static final String TABLE_USER = "users";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Constructor
    public UserDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is first created
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_USER + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT);";
        db.execSQL(query);

        Log.d(TAG, "User table created");  // logs event
    }

    /**
     * Called when the database needs to be upgraded
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);

        Log.w(TAG, "User table dropped and recreated (upgrade from " +
                oldVersion + " to " + newVersion + ")");  // logs event
    }

    /**
     * Hashes a password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "SHA-256 hashing failed", e);  // logs event
            return null;
        }
    }

    /**
     * Adds a new user to the database
     */
    public Boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        String hashedPass = hashPassword(password);
        if (hashedPass == null) {
            Log.e(TAG, "Password hashing failed during registration");  // logs event
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, hashedPass);

        long result = db.insert(TABLE_USER, null, values);
        if (result == -1) {
            Log.e(TAG, "Failed to register user: " + username);  // logs event
            return false;
        } else {
            Log.d(TAG, "User registered: " + username);  // logs event
            return true;
        }
    }

    /**
     * Checks if a username already exists in the database
     */
    public Boolean checkUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USER + " WHERE "+ COLUMN_USERNAME + " = ?",
                new String[]{username})) {

            boolean exists = cursor.getCount() > 0;
            Log.d(TAG, "Username check for '" + username + "': " + exists);  // logs event
            return exists;
        }
    }

    /**
     * Verifies login credentials by checking username and hashed password
     */
    public Boolean verifyCredentials(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPass = hashPassword(password);

        if (hashedPass == null) {
            Log.e(TAG, "Password hashing failed during login attempt for user: " +
                    username);  // logs event
            return false;
        }

        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_USERNAME + " = ? AND "
                        + COLUMN_PASSWORD + " = ?",
                new String[]{username, hashedPass})) {

            boolean valid = cursor.getCount() > 0;
            Log.d(TAG, "Login attempt for '" + username + "': " +
                    (valid ? "Success" : "Failure"));  // logs event
            return valid;
        }
    }
}

