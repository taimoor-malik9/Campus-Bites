package com.example.campusbites;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.campusbites.notification.LoginReminderGate;
import com.example.campusbites.notification.LoginReminderNotifier;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private CartManager cartManager;

    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);

        // Check if user is logged in
        SharedPreferences prefs = getSharedPreferences("CampusBitesPrefs", MODE_PRIVATE);
        String userId = prefs.getString("USER_ID", null);
        String userType = prefs.getString("USER_TYPE", null);

        if (userId == null || userType == null || !userType.equals("customer")) {
            // Redirect to role selection to maintain proper flow
            Intent intent = new Intent(this, RoleSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    // If granted, post the reminder (only once)
                    tryShowLoginReminder();
                }
        );

        cartManager = CartManager.getInstance(this);
        bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CustomerHomeFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            try {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new CustomerHomeFragment();
                } else if (itemId == R.id.nav_cart) {
                    selectedFragment = new CustomerCartFragment();
                } else if (itemId == R.id.nav_orders) {
                    selectedFragment = new CustomerOrdersFragment();
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new CustomerProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });

        updateCartBadge();

        // Trigger reminder from a stable foreground activity
        tryShowLoginReminder();
    }

    private void tryShowLoginReminder() {
        try {
            if (!LoginReminderGate.shouldShow(this)) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Request permission first if needed.
                // If user denies, we just skip (no mess).
                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                    return;
                }
            }

            LoginReminderNotifier.show(this);
            LoginReminderGate.markShown(this);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }

    public void updateCartBadge() {
        if (cartManager != null && bottomNav != null) {
            try {
                int cartCount = cartManager.getItemCount();
                if (cartCount > 0) {
                    BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_cart);
                    badge.setVisible(true);
                    badge.setNumber(cartCount);
                } else {
                    BadgeDrawable badge = bottomNav.getBadge(R.id.nav_cart);
                    if (badge != null) {
                        badge.setVisible(false);
                    }
                }
            } catch (Exception e) {
                // Silently handle badge update errors - theme might not be set correctly
                e.printStackTrace();
            }
        }
    }
}