package com.example.campusbites.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public final class NotificationChannels {

    public static final String CHANNEL_ORDER_UPDATES = "order_updates";
    public static final String CHANNEL_PROMOTIONS = "promotions";

    private NotificationChannels() {}

    public static void ensureCreated(Context context) {
        if (context == null) return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        NotificationChannel orders = new NotificationChannel(
                CHANNEL_ORDER_UPDATES,
                "Order updates",
                NotificationManager.IMPORTANCE_HIGH
        );
        orders.setDescription("Notifications for order status like Ready / Delivered");

        NotificationChannel promos = new NotificationChannel(
                CHANNEL_PROMOTIONS,
                "Promotions",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        promos.setDescription("Nearby/lunch-time deals and reminders");

        nm.createNotificationChannel(orders);
        nm.createNotificationChannel(promos);
    }
}
