package com.example.campusbites;

import java.util.List;

public class OrderEntity {
    private String id;
    private String storeId;
    private String storeName;
    private String customerId;
    private String orderNumber;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private String itemsJson;
    private List<CartItem> items; // For easier handling
    private double totalPrice;
    private String status; // "Pending", "Preparing", "Ready", "Delivered"
    private String paymentMethod; // "Cash", "Card", "JazzCash", "Easypaisa"
    private boolean isPaid;
    private long timestamp;

    public OrderEntity() {
        this.status = "Pending";
        this.timestamp = System.currentTimeMillis();
        this.isPaid = false;
    }

    // Existing Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getItemsJson() { return itemsJson; }
    public void setItemsJson(String itemsJson) { this.itemsJson = itemsJson; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}