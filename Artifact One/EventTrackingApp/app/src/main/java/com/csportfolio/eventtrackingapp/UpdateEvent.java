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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.List;
import java.util.Locale;
import java.util.Date;

/**
 * UpdateEvent allows the user to modify an existing calendar event.
 * It handles input validation, conflict detection, database update,
 * and notification scheduling.
 */
public class UpdateEvent extends AppCompatActivity {

    // Event Logging
    private static final String TAG = "UpdateEvent";

    // Event Data
    private String eventId;
    private String startTime24, endTime24;

    // UI Components
    private EditText titleInput, descriptionInput;
    private TextInputEditText dateInput, startTimeInput, endTimeInput;
    private Button updateBtn;

    // Date & Time Formatter
    private final SimpleDateFormat datetimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_event);

        // Toolbar Setup
        MaterialToolbar toolbar = findViewById(R.id.topAppBarUpdateEvent);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize Views
        titleInput = findViewById(R.id.title_update);
        descriptionInput = findViewById(R.id.description_update);
        dateInput = findViewById(R.id.date_update);
        startTimeInput = findViewById(R.id.start_time_update);
        endTimeInput = findViewById(R.id.end_time_update);
        updateBtn = findViewById(R.id.update_button);
        Button deleteBtn = findViewById(R.id.delete_button);

        /// Change Listeners (input Validation)
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

        // Loads Existing Data
        loadEventFromDatabase();

        // Update Button Handler
        updateBtn.setOnClickListener(v -> updateEvent());

        // Delete Button Handler
        deleteBtn.setOnClickListener(v -> confirmDelete());

        // Disables Update Initially
        updateBtn.setEnabled(false);
    }

    /**
     * Loads existing event details from database using event ID
     */
    private void loadEventFromDatabase() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            eventId = intent.getStringExtra("id");

            try (EventDatabase db = new EventDatabase(this)) {
                EventStructure event = db.readEventById(eventId);
                if (event != null) {
                    titleInput.setText(event.getTitle());
                    dateInput.setText(event.getDate());
                    startTime24 = event.getStartTime();
                    endTime24 = event.getEndTime();
                    startTimeInput.setText(formatTo12Hr(startTime24));
                    endTimeInput.setText(formatTo12Hr(endTime24));
                    descriptionInput.setText(event.getDescription());

                    Log.d(TAG, "Loaded event from DB: ID=" + eventId);  // logs event
                } else {
                    Log.w(TAG, "No event found for ID=" + eventId);  // logs event
                    Toast.makeText(this, R.string.noEventDataReceived,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        } else {
            Log.w(TAG, "Intent missing event ID");  // logs event
            Toast.makeText(this, R.string.noEventDataReceived,
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Launches a date picker
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
     * Launches a time picker for start or end time
     */
    private void selectTime(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(this,
                (view, h, m) -> {
                    String time24 = String.format(Locale.getDefault(),
                            "%02d:%02d", h, m);
                    String time12 = formatTo12Hr(time24);

                    if (isStart) {
                        startTime24 = time24;
                        startTimeInput.setText(time12);
                    } else {
                        endTime24 = time24;
                        endTimeInput.setText(time12);
                    }

                    validateInputs();
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE), false
        ).show();
    }

    /**
     * Attempts to update the event and set a notification
     */
    private void updateEvent() {
        String title = titleInput.getText().toString().trim();
        String date = Objects.requireNonNull(dateInput.getText()).toString().trim();
        String description = descriptionInput.getText().toString().trim();

        // ensures both times are valid and end > start
        if (startTime24 == null || endTime24 == null || startTime24.equals(endTime24)) {
            Toast.makeText(this, "Please select valid start and end times.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Date start = datetimeFormat.parse(date + " " + startTime24);
            Date end = datetimeFormat.parse(date + " " + endTime24);

            if (start != null && end != null && !end.after(start)) {
                Toast.makeText(this, "End time must be after start time.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            Log.e(TAG, "Time parsing failed", e);
            return;
        }

        EventStructure updated = new EventStructure(eventId, title, date, startTime24, endTime24, description);

        try (EventDatabase db = new EventDatabase(this)) {
            List<EventStructure> allEvents = db.readAllEventsList();
            for (EventStructure existing : allEvents) {
                if (!existing.getId().equals(eventId) && updated.conflictsWith(existing)) {
                    Toast.makeText(this, "This event conflicts with another event.", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            boolean result = db.updateEvent(eventId, title, date, startTime24, endTime24, description);
            if (!result) {
                Toast.makeText(this, R.string.failedToUpdateEvent, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        setNotification(title, date, startTime24, description);

        new AlertDialog.Builder(this)
                .setTitle(R.string.updatedEventDialogueTitle)
                .setMessage(R.string.updatedEventMessage)
                .setPositiveButton(R.string.okay, (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    /**
     * Prompts user to confirm and delete the event
     */
    private void confirmDelete() {
        String title = titleInput.getText().toString().trim();
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirmDeleteTitle, title))
                .setMessage(getString(R.string.confirmDeleteMessage, title))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    try (EventDatabase db = new EventDatabase(this)) {
                        if (db.deleteEvent(eventId)) {
                            Log.d(TAG, "Deleted event ID=" + eventId);
                        } else {
                            Log.e(TAG, "Failed to delete event ID=" + eventId);
                        }
                    }
                    finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    /**
     * Sets a notification to trigger 30 minutes before the event
     */
    private void setNotification(String title, String date, String time, String description) {
        try {
            Date eventDateTime = datetimeFormat.parse(date + " " + time);
            if (eventDateTime == null) return;

            Calendar cal = Calendar.getInstance();
            cal.setTime(eventDateTime);
            cal.add(Calendar.MINUTE, -30);

            if (cal.getTimeInMillis() < System.currentTimeMillis()) return;

            Intent intent = new Intent(this, EventNotification.class);
            intent.setAction("com.csportfolio.eventtrackingapp.ACTION_NOTIFY_EVENT");
            intent.putExtra("eventTitle", title);
            intent.putExtra("date", date);
            intent.putExtra("time", time);
            intent.putExtra("description", description);

            int notificationId = (title + date + time + System.currentTimeMillis()).hashCode();
            PendingIntent pending = PendingIntent.getBroadcast(
                    this,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (manager != null && manager.canScheduleExactAlarms()) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pending);
                Log.d(TAG, "Alarm set for: " + cal.getTime());
            }
        } catch (ParseException e) {
            Log.e(TAG, "Failed to set notification", e);
        }
    }

    /**
     * Converts 24-hour time string to 12-hour display format
     */
    private String formatTo12Hr(String time24) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("HH:mm", Locale.US);
            SimpleDateFormat output = new SimpleDateFormat("hh:mm a", Locale.US);
            return output.format(Objects.requireNonNull(input.parse(time24)));
        } catch (ParseException e) {
            Log.w(TAG, "Invalid time format: " + time24);
            return "12:00 PM";
        }
    }

    /**
     * Validates all inputs before enabling update
     */
    private void validateInputs() {
        String title = titleInput.getText().toString().trim();
        String date = Objects.requireNonNull(dateInput.getText()).toString().trim();
        String description = descriptionInput.getText().toString().trim();

        boolean isValid = !title.isEmpty() && !date.isEmpty() && startTime24 != null && endTime24 != null && !description.isEmpty();
        updateBtn.setEnabled(isValid);
    }

    /**
     * Listens to text changes to trigger validation
     */
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            validateInputs();
        }
        @Override public void afterTextChanged(Editable s) {}
    };
}