package com.example.campusbites.notification;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Ensures the "don't forget to order" notification is shown only once per successful login.
 */
public final class LoginReminderGate {

    private static final String PREFS = "CampusBitesPrefs";
    private static final String KEY_SHOWN = "LOGIN_REMINDER_SHOWN";

    private LoginReminderGate() {}

    public static boolean shouldShow(Context context) {
        if (context == null) return false;
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return !prefs.getBoolean(KEY_SHOWN, false);
    }

    public static void markShown(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_SHOWN, true).apply();
    }

    public static void reset(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_SHOWN).apply();
    }
}
