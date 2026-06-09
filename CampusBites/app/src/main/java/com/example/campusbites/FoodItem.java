package com.example.campusbites;

public class FoodItem {
    private String id;
    private String storeId;
    private String name;
    private String description;
    private String imageUrl;
    private double priceSmall;
    private double priceMedium;
    private double priceLarge;
    private long createdAt;
    private boolean available;

    public FoodItem() {
        // Required empty constructor for Firebase
        this.createdAt = System.currentTimeMillis();
        this.available = true;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getPriceSmall() { return priceSmall; }
    public void setPriceSmall(double priceSmall) { this.priceSmall = priceSmall; }

    public double getPriceMedium() { return priceMedium; }
    public void setPriceMedium(double priceMedium) { this.priceMedium = priceMedium; }

    public double getPriceLarge() { return priceLarge; }
    public void setPriceLarge(double priceLarge) { this.priceLarge = priceLarge; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
