package com.example.campusbites;

public class Store {
    private String id;
    private String ownerName;
    private String ownerEmail;
    private String storeName;
    private String storeDescription;
    private String storeImageUrl;
    private long createdAt;

    public Store() {
        // Required empty constructor for Firebase
        this.createdAt = System.currentTimeMillis();
    }

    public Store(String ownerName, String ownerEmail, String storeName,
                 String storeDescription, String storeImageUrl) {
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
        this.storeName = storeName;
        this.storeDescription = storeDescription;
        this.storeImageUrl = storeImageUrl;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getStoreDescription() { return storeDescription; }
    public void setStoreDescription(String storeDescription) {
        this.storeDescription = storeDescription;
    }

    public String getStoreImageUrl() { return storeImageUrl; }
    public void setStoreImageUrl(String storeImageUrl) {
        this.storeImageUrl = storeImageUrl;
    }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public float getRating() {
        // Placeholder implementation, replace with actual logic
        return 4.5f;
    }
}