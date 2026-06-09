package com.example.campusbites;

import android.content.Context;

import com.google.gson.Gson;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds a small JSON payload describing the "current menu data".
 *
 * NOTE: Your actual food items live in Firestore per-store, but this gives the AI agent
 * stable context immediately without touching your ordering flow.
 *
 * You can enhance this later by fetching store + items from FirebaseRepository.
 */
public final class MenuContextProvider {
    private MenuContextProvider() {}

    public static String buildMenuContextJson(Context context) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("app", "CampusBites");

        // Minimal context (safe): bottom nav items + intent of screens.
        Map<String, String> screens = new LinkedHashMap<>();
        screens.put("Home", "Browse stores and search");
        screens.put("Cart", "View and checkout selected items");
        screens.put("Orders", "Track your orders");
        screens.put("Profile", "Manage your profile");
        root.put("screens", screens);

        return new Gson().toJson(root);
    }
}
