package com.zybooks.michaelbasile_eventtrackingapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class UserDatabase extends SQLiteOpenHelper {

    public static final String DBNAME = "UserDB";

    public UserDatabase(@Nullable Context context) {
        super(context, "UserDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creates table for storing usernames and password
        db.execSQL("create Table users(username TEXT primary key, password TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop Table if exists users");
    }

    // adding new users
    public Boolean insertData(String user, String pass){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues Values = new ContentValues();
        Values.put("username", user);
        Values.put("password", pass);
        long result = db.insert("users", null, Values);
        return result != -1;
    }

    // checking for existing username
    public Boolean checkUsername(String user){
        SQLiteDatabase db = this.getWritableDatabase();
        //checking database fo the username
        Cursor cursor = db.rawQuery("Select * from users where username = ?", new String[]{user});
        return cursor.getCount() > 0;
    }

    // verifying username and password
    public Boolean checkUsernamePassword(String user, String pass){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from users where username = ? and password = ?", new String[] {user, pass});
        return cursor.getCount() > 0;
    }
}
