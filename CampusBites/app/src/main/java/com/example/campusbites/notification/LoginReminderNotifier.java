package com.example.campusbites.notification;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.campusbites.CustomerMainActivity;
import com.example.campusbites.R;

/**
 * Posts a friendly notification right after login.
 * This is intentionally simple and doesn't change any existing flow.
 */
public final class LoginReminderNotifier {

    private static final int NOTIF_ID_LOGIN_REMINDER = 2001;

    private LoginReminderNotifier() {}

    public static void show(Context context) {
        if (context == null) return;

        // Android 13+ requires POST_NOTIFICATIONS runtime permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return; // Don't crash or block login flow.
            }
        }

        Intent intent = new Intent(context, CustomerMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pi = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, NotificationChannels.CHANNEL_PROMOTIONS)
                .setSmallIcon(R.drawable.ic_campus_bites_logo)
                .setContentTitle("Campus Bites")
                .setContentText("Don't forget to order your food. Enjoy your meal:)")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Don't forget to order your food.\n\nTap to browse stores and menu."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pi);

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_LOGIN_REMINDER, b.build());
        } catch (SecurityException ignored) {
            // Permission revoked mid-run; don't break flow.
        }
    }
}
