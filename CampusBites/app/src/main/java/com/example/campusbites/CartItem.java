package com.example.campusbites;

public class CartItem {
    private String id;
    private String foodItemId;
    private String foodName;
    private String foodImageUrl;
    private String storeId;
    private String storeName;
    private String size; // "Small", "Medium", "Large"
    private double price;
    private int quantity;

    public CartItem() {}

    public CartItem(FoodItem foodItem, String size, double price, int quantity) {
        this.foodItemId = foodItem.getId();
        this.foodName = foodItem.getName();
        this.foodImageUrl = foodItem.getImageUrl();
        this.storeId = foodItem.getStoreId();
        this.size = size;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFoodItemId() { return foodItemId; }
    public void setFoodItemId(String foodItemId) { this.foodItemId = foodItemId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public String getFoodImageUrl() { return foodImageUrl; }
    public void setFoodImageUrl(String foodImageUrl) {
        this.foodImageUrl = foodImageUrl;
    }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() {
        return price * quantity;
    }
}