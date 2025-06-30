package com.csportfolio.eventtrackingapp;

import android.util.Log;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.Toast;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Objects;
import java.util.Locale;
import java.util.Date;
import java.util.List;

/**
 * NewEvent allows the user to create a new calendar event.
 * It provides UI for entering event details, performs validation,
 * checks for time conflicts, and schedules a reminder 30 minutes
 * before the event starts.
 */
public class NewEvent extends AppCompatActivity {

    // Event Logging
    private static final String TAG = "NewEvent";

    // Start & End Time Inputs
    private String startTime24, endTime24;

    // UI Components
    private EditText titleInput, descriptionInput;
    private TextInputEditText dateInput, startTimeInput, endTimeInput;
    private Button saveBtn;

    // Date & Time Formatter
    private SimpleDateFormat getCurrentDateTimeFormat() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        // Toolbar Setup
        MaterialToolbar toolbar = findViewById(R.id.topAppBarNewEvent);
        setSupportActionBar(toolbar);  // sets as action bar
        toolbar.setNavigationOnClickListener(v -> finish());  // closes activity

        // Initializes Views
        titleInput = findViewById(R.id.title_input);
        dateInput = findViewById(R.id.date_input);
        startTimeInput = findViewById(R.id.start_time_input);
        endTimeInput = findViewById(R.id.end_time_input);
        descriptionInput = findViewById(R.id.description_input);
        saveBtn = findViewById(R.id.save_button);

        // Change Listeners (input Validation)
        titleInput.addTextChangedListener(textWatcher);
        descriptionInput.addTextChangedListener(textWatcher);

        // Disables Manual Input
        dateInput.setFocusable(false);
        startTimeInput.setFocusable(false);
        endTimeInput.setFocusable(false);

        // Opens Date & Time Pickers
        dateInput.setOnClickListener(v -> selectDate());
        startTimeInput.setOnClickListener(v -> selectTime(true));
        endTimeInput.setOnClickListener(v -> selectTime(false));

        // Save Button Handler
        saveBtn.setOnClickListener(v -> saveEvent());

        // Disables Save Initially
        saveBtn.setEnabled(false);
    }

    /**
     * Opens a date picker and populates the date field
     */
    private void selectDate() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, y, m, d) -> {
                    dateInput.setText(String.format(Locale.getDefault(),
                            "%02d-%02d-%04d", d, m + 1, y));
                    validateInputs();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    /**
     * Opens a time picker and populates start or end time
     */
    private void selectTime(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(this,
                (view, h, m) -> {
                    String formatted24 = String.format(Locale.getDefault(), "%02d:%02d", h, m);
                    String formatted12 = formatTime(h, m);

                    if (isStart) {
                        startTime24 = formatted24;
                        startTimeInput.setText(formatted12);
                    } else {
                        endTime24 = formatted24;
                        endTimeInput.setText(formatted12);
                    }
                    validateInputs();
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE), false
        ).show();
    }

    /**
     * Saves the new event if valid and non-conflicting
     */
    private void saveEvent() {
        String title = titleInput.getText().toString().trim();
        String date = Objects.requireNonNull(dateInput.getText()).toString().trim();
        String description = descriptionInput.getText().toString().trim();

        // time input checks
        if (startTime24 == null || endTime24 == null || startTime24.equals(endTime24)) {
            Toast.makeText(this, "Please select valid start and end times.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {  // time order validation
            Date start = getCurrentDateTimeFormat().parse(date + " " + startTime24);
            Date end = getCurrentDateTimeFormat().parse(date + " " + endTime24);

            if (start != null && end != null && !end.after(start)) {
                Toast.makeText(this, "End time must be after start time.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            Log.e(TAG, "Time parsing failed", e);  // logs event
            return;
        }

        // creates new event object
        EventStructure newEvent = new EventStructure("0", title, date, startTime24, endTime24, description);

        try (EventDatabase db = new EventDatabase(this)) {
            List<EventStructure> allEvents = db.readAllEventsList();

            // conflict detection
            for (EventStructure existing : allEvents) {
                if (newEvent.conflictsWith(existing)) {
                    Toast.makeText(this, "This event conflicts with another event.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }

            // add event to database
            long result = db.addEvent(title, date, startTime24, endTime24, description);
            if (result == -1) {
                Toast.makeText(this, R.string.failedToAddEvent,
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Schedules Reminder Notification
        setNotification(title, date, startTime24, description);

        // Shows Confirmation Dialogue
        new AlertDialog.Builder(this)
                .setTitle(R.string.savedEventDialogTitle)
                .setMessage(R.string.savedEventMessage)
                .setPositiveButton(R.string.okay, (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    /**
     * Schedules a notification 30 minutes before the event's start time
     */
    private void setNotification(String title, String date, String time, String description) {
        try {
            Date eventDateTime = getCurrentDateTimeFormat().parse(date + " " + time);
            if (eventDateTime == null) return;

            Calendar cal = Calendar.getInstance();
            cal.setTime(eventDateTime);
            cal.add(Calendar.MINUTE, -30);  // notifies 30 minutes before

            if (cal.getTimeInMillis() < System.currentTimeMillis()) return;

            Intent intent = new Intent(this, EventNotification.class);
            intent.setAction("com.csportfolio.eventtrackingapp.ACTION_NOTIFY_EVENT");
            intent.putExtra("eventTitle", title);
            intent.putExtra("date", date);
            intent.putExtra("time", time);
            intent.putExtra("description", description);

            int notificationId = (title + date + time + System.currentTimeMillis()).hashCode();

            PendingIntent pending = PendingIntent.getBroadcast(
                    this, notificationId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (manager != null && manager.canScheduleExactAlarms()) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pending);
                Log.d(TAG, "Alarm set for: " + cal.getTime());  // logs event
            }
        } catch (ParseException e) {
            Log.e(TAG, "Failed to set notification", e);  // logs event
        }
    }

    /**
     * Converts 24-hour time to 12-hour format string for display
     */
    private String formatTime(int hr, int min) {
        String period = hr < 12 ? "AM" : "PM";
        int displayHr = (hr == 0 || hr == 12) ? 12 : hr % 12;
        return String.format(Locale.getDefault(), "%02d:%02d %s", displayHr, min, period);
    }

    /**
     * Validates form input to ensure save button should be enabled
     */
    private void validateInputs() {
        String title = titleInput.getText().toString().trim();
        String date = Objects.requireNonNull(dateInput.getText()).toString().trim();
        String description = descriptionInput.getText().toString().trim();

        boolean isValid = !title.isEmpty() && !date.isEmpty()
                && startTime24 != null && endTime24 != null
                && !description.isEmpty();

        saveBtn.setEnabled(isValid);
    }

    /**
     * Watches text input fields to trigger validation on change
     */
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            validateInputs();
        }
        @Override public void afterTextChanged(Editable s) {}
    };
}