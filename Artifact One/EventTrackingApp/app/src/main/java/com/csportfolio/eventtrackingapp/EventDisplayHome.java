package com.csportfolio.eventtrackingapp;

import android.util.Log;
import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.database.Cursor;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.ArrayList;

/**
 * EventDisplayHome is the main screen of the app. It displays a list of events
 * using a RecyclerView. It also handles notification permission requests,
 * sets up the notification channel, and launches the NewEvent activity.
 */
public class EventDisplayHome extends AppCompatActivity {

    // Event Logging
    private static final String TAG = EventDisplayHome.class.getSimpleName();

    // Constants
    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;
    private static final String CHANNEL_ID = "notificationText";

    // UI Components
    private RecyclerView recyclerView;
    private TextView emptyMessage;

    // Database
    private EventDatabase db;

    // Events
    private List<EventStructure> allEvents = new ArrayList<>();

    // Activity Result Launcher - Adding Events
    private ActivityResultLauncher<Intent> addEventLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_display_home);

        Log.d(TAG, "onCreate: Initializing UI components and loading events.");  // logs event

        // Requests Notification Permission
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATION_PERMISSION
            );
        }

        // Initializes Views
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        emptyMessage = findViewById(R.id.empty_message);
        FloatingActionButton addBtn = findViewById(R.id.add_event);
        EditText searchInput = findViewById(R.id.search_input);

        // Initializes Database
        db = new EventDatabase(this);

        // Initializes Launcher - NewEvent Activity
        addEventLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        recreate();  // refreshes list after adding event
                    }
                }
        );

        // Creates Notification Channel
        createNotificationChannel();

        // Launches NewEvent Activity
        addBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewEvent.class);
            addEventLauncher.launch(intent);
        });

        // Loads Events
        allEvents = loadEventsFromDatabase();
        toggleEmptyState(allEvents.isEmpty());

        // Adapter Setup
        EventAdapter adapter = new EventAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.submitList(allEvents);

        // Search Logic
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString(), adapter);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Loads all events from the database into a list
     */
    private List<EventStructure> loadEventsFromDatabase() {
        List<EventStructure> eventList = new ArrayList<>();

        Cursor cursor = db.readAllEvents();

        try (cursor) {  // null check
            if (cursor == null) {
                Log.e(TAG, "loadEventsFromDatabase: Cursor is null");  // logs event
                return eventList;
            }
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(0);
                    String title = cursor.getString(1);
                    String date = cursor.getString(2);
                    String startTime = cursor.getString(3);
                    String endTime = cursor.getString(4);
                    String description = cursor.getString(5);

                    EventStructure event = new EventStructure(id, title, date, startTime, endTime, description);
                    eventList.add(event);
                } while (cursor.moveToNext());
            }

            Log.d(TAG, "loadEventsFromDatabase: Loaded " + eventList.size() + " events.");  // logs event
        } finally {
            db.close();
        }

        return eventList;
    }

    /**
     * Filters the event list based on search query
     */
    private void filterEvents(String query, EventAdapter adapter) {
        List<EventStructure> filteredList = new ArrayList<>();

        for (EventStructure event : allEvents) {
            if (event.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(event);
            }
        }

        toggleEmptyState(filteredList.isEmpty());
        adapter.submitList(filteredList);
    }

    /**
     * Toggles visibility of the empty message and event list
     */
    private void toggleEmptyState(boolean isEmpty) {
        emptyMessage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    /**
     * Handles result of permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            boolean granted = grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED;

            Log.d(TAG, "Notification permission granted: " + granted);  // logs event

            if (granted) {
                Toast.makeText(this, getString(R.string.notificationsEnabled),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.notificationsDisabled),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Creates the notification channel used by this app
     */
    private void createNotificationChannel() {
        CharSequence name = "Reminder";
        String description = "Event Notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Notification channel created with ID: " + CHANNEL_ID);  // logs event
        } else {
            Log.e(TAG, "NotificationManager not available. Channel not created.");  // logs event
        }
    }
}