package com.example.campusbites;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            SharedPreferences prefs = getSharedPreferences("SmartCanteenPrefs", MODE_PRIVATE);
            // Read it as a String
            String storeId = null;
            try {
                storeId = prefs.getString("STORE_ID", null);
            } catch (Exception e) {
                // Try as int if string fails
                try {
                    int idAsInt = prefs.getInt("STORE_ID", -1);
                    if (idAsInt != -1) {
                        storeId = String.valueOf(idAsInt);
                        // Fix it for next time
                        prefs.edit().putString("STORE_ID", storeId).apply();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
            if (storeId == null || storeId.isEmpty()) {
                // Redirect to role selection to maintain proper flow
                Intent intent = new Intent(this, RoleSelectionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }

            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            if (bottomNav == null) {
                finish();
                return;
            }

            if (savedInstanceState == null) {
                try {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment())
                            .commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            bottomNav.setOnItemSelectedListener(item -> {
                try {
                    Fragment selectedFragment = null;
                    int itemId = item.getItemId();

                    if (itemId == R.id.nav_home) {
                        selectedFragment = new HomeFragment();
                    } else if (itemId == R.id.nav_orders) {
                        selectedFragment = new OrdersFragment();
                    } else if (itemId == R.id.nav_profile) {
                        selectedFragment = new ProfileFragment();
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
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to role selection if anything goes wrong
            Intent intent = new Intent(this, RoleSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}