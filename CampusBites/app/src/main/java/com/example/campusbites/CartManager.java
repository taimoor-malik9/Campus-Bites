package com.example.campusbites;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static final String CART_PREFS = "CartPrefs";
    private static final String CART_ITEMS_KEY = "cart_items";

    private static CartManager instance;
    private List<CartItem> cartItems;
    private SharedPreferences prefs;
    private Gson gson;

    private CartManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(CART_PREFS, Context.MODE_PRIVATE);
        gson = new Gson();
        loadCart();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
        return instance;
    }

    private void loadCart() {
        try {
            String json = prefs.getString(CART_ITEMS_KEY, null);
            if (json != null && !json.isEmpty()) {
                Type type = new TypeToken<List<CartItem>>(){}.getType();
                cartItems = gson.fromJson(json, type);
                if (cartItems == null) {
                    cartItems = new ArrayList<>();
                }
            } else {
                cartItems = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            cartItems = new ArrayList<>();
        }
    }

    private void saveCart() {
        String json = gson.toJson(cartItems);
        prefs.edit().putString(CART_ITEMS_KEY, json).apply();
    }

    public void addItem(CartItem item) {
        // Check if item already exists with same size
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem existing = cartItems.get(i);
            if (existing.getFoodItemId().equals(item.getFoodItemId())
                    && existing.getSize().equals(item.getSize())) {
                // Update quantity
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                saveCart();
                return;
            }
        }
        // Add new item
        item.setId(String.valueOf(System.currentTimeMillis()));
        cartItems.add(item);
        saveCart();
    }

    public void updateItem(CartItem item) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getId().equals(item.getId())) {
                cartItems.set(i, item);
                saveCart();
                return;
            }
        }
    }

    public void removeItem(String itemId) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getId().equals(itemId)) {
                cartItems.remove(i);
                saveCart();
                return;
            }
        }
    }

    public void clearCart() {
        cartItems.clear();
        saveCart();
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public int getItemCount() {
        if (cartItems == null) {
            return 0;
        }
        int count = 0;
        for (CartItem item : cartItems) {
            if (item != null) {
                count += item.getQuantity();
            }
        }
        return count;
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public boolean hasItems() {
        return !cartItems.isEmpty();
    }

    // Check if cart has items from different stores
    public boolean hasItemsFromDifferentStore(String storeId) {
        for (CartItem item : cartItems) {
            if (!item.getStoreId().equals(storeId)) {
                return true;
            }
        }
        return false;
    }
}