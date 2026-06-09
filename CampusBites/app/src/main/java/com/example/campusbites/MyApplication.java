package com.example.campusbites;

import android.app.Application;

import com.example.campusbites.notification.NotificationChannels;
import com.google.firebase.FirebaseApp;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        NotificationChannels.ensureCreated(this);
    }
}