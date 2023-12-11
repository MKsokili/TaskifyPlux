package com.example.taskmanager.Service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.taskmanager.LoginActivity;
import com.example.taskmanager.MainActivity;
import com.example.taskmanager.R;

import java.util.Locale;

// This receiver is to show notification when alarm triggered
// See MyForegroundService
public class MyNotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "task4175";

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        // Acquire a wake lock to turn on the screen
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "MyApp::MyWakelockTag");

            wakeLock.acquire(1 * 60 * 1000L /* 1 minutes */);

            // Release the wake lock after a short duration
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }, 3000); // Adjust the time as needed, e.g., 3000 milliseconds = 3 seconds
        }

        Intent i = new Intent(context, LoginActivity.class); //NotificationDetails.class
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_IMMUTABLE);

        // Get the Bundle from the Intent
        Bundle bundle = intent.getExtras();
        String msgTitle, msgDescription, msgCategory, msgTime;
        int notificationId = 1;
        if (bundle != null) {
            // Retrieve values from the Bundle using keys
            msgTitle = bundle.getString("msg_title");
            msgDescription = bundle.getString("msg_description","");
            msgCategory = bundle.getString("msg_category", "Appointment");
            msgTime = bundle.getString("msg_time", "Time not specified");
            notificationId = bundle.getInt("notification_id");
        } else {
            msgTitle = "Activity";
            msgDescription = "No description";
            msgCategory = "Appointment";
            msgTime = "Time not specified";
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.calendar_small);
        if (msgCategory.equals("Appointment")) {
            largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.calendar_small);
        } else if (msgCategory.equals("Medicine")) {
            largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.medicine_small);
        } else if (msgCategory.equals("Workout")) {
            largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.workout_small);
        } else {
            largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.clock_small);
        }

        // Create and show the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_access_alarm_24)
                .setContentTitle("Reminder: " + msgTitle)
                .setContentText("at " + msgTime + ". ")
                .setLargeIcon(largeIcon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL) // Add default notification sound, vibration, etc.
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msgDescription));
                //.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(largeIcon));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }
}

