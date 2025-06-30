package com.csportfolio.eventtrackingapp;

import android.util.Log;
import android.Manifest;
import android.content.Intent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * EventNotification is a BroadcastReceiver that triggers when an alarm goes off.
 * It reads event data from the Intent and displays a local notification.
 */
public class EventNotification extends BroadcastReceiver {

    // Event Logging
    private static final String TAG = EventNotification.class.getSimpleName();

    /**
     * Called when the alarm is triggered
     * Builds and displays notification with event title
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        // Null Check
        if (intent == null || intent.getExtras() == null) {
            Log.e(TAG, "Received null intent or extras.");  // logs event
            return;
        }

        // Checks Broadcast Received
        if (!"com.csportfolio.eventtrackingapp.ACTION_NOTIFY_EVENT".equals(intent.getAction())) {
            Log.w(TAG, "Unexpected intent action: " + intent.getAction());  // logs event
            return;
        }

        // Retrieves Event Details
        String eventTitle = intent.getStringExtra("eventTitle");
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");

        if (eventTitle == null || eventTitle.trim().isEmpty()) {  // checks if null or empty
            Log.w(TAG, "Missing or empty event title.");  // logs event
            eventTitle = context.getString(R.string.defaultEventTitle);
        }

        // Builds Message (optional date/time)
        String message = (date != null && time != null)
                ? context.getString(R.string.upcomingEventMessageWithTime, eventTitle, date, time)
                : context.getString(R.string.upcomingEventMessage, eventTitle);

        Log.d(TAG, "onReceive: Notification triggered for event: " + eventTitle);  // logs event

        // Builds Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                "notificationText")

                .setSmallIcon(R.drawable.ic_notifications_icon)
                .setContentTitle(context.getString(R.string.upcomingEventTitle))
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Checks Notification Permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {  // permission granted
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            int notificationId = (int) System.currentTimeMillis();

            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Notification posted successfully.");  // logs event
        } else {
            Log.w(TAG, "Permission to POST_NOTIFICATIONS not granted. " +
                    "Notification will not be set.");  // logs event
        }
    }
}