package com.zybooks.michaelbasile_eventtrackingapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;
import androidx.annotation.Nullable;

public class EventDatabase extends SQLiteOpenHelper {

    private static final String tableName = "my_library";
    private static final String idC = "_id";
    private static final String titleC = "title";
    private static final String descC = "description";
    private static final String dateC = "date";
    private static final String timeC = "time";
    private final Context context;
    private static final String Dname = "Event.db";
    private static final int DATABASE = 1;

    EventDatabase(@Nullable Context context) {
        super(context, Dname, null, DATABASE);
        this.context = context;
    }

    // build  database
    @Override
    public void onCreate(SQLiteDatabase db) {

        String query =
                "CREATE TABLE " + tableName +
                        " (" + idC + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        titleC + " TEXT, " +
                        descC + " TEXT, " +
                        dateC + " TEXT, " +
                        timeC + " TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(db);
    }

    // returns all events to recycler view
    Cursor readAllData(){
        String Q = "SELECT * FROM " + tableName;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = null;
        if(db != null){
            c = db.rawQuery(Q, null);
        }
        return c;
    }

    // add event to database
    long addReminder(String date, String time, String title, String description){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(dateC, date);
        cv.put(timeC, time);
        cv.put(titleC, title);
        cv.put(descC, description);
        long res = db.insert(tableName, null, cv);
        if (res == -1){
            Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show();
            return res;
        }else {
            Toast.makeText(context, "Event added", Toast.LENGTH_SHORT).show();
            return res;
        }
    }

    // update the event
    void updateData(String row_id, String title, String description, String date, String time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(titleC, title);
        cv.put(descC, description);
        cv.put(dateC, date);
        cv.put(timeC, time);
        long res = db.update(tableName, cv, "_id=?", new String[]{row_id});
        if (res == -1){
            Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Update success", Toast.LENGTH_SHORT).show();
        }
    }

    //delete a row
    void deleteOneRow(String row){
        SQLiteDatabase db = this.getWritableDatabase();
        long res = db.delete(tableName, "_id=?", new String[]{row});
        if(res == -1){
            Toast.makeText(context, "Failed deletion", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Successfully Deleted", Toast.LENGTH_SHORT).show();
        }
    }
}