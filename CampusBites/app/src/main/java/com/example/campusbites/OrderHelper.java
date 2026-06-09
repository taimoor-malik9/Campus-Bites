package com.example.campusbites;

import android.content.Context;
import com.example.campusbites.FoodItem;
import com.example.campusbites.OrderEntity;
import com.example.campusbites.FirebaseRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public class OrderHelper {

    /**
     * Generate a unique order number for the store
     */
    public static void generateOrderNumber(Context context, String storeId, OrderNumberCallback callback) {
        FirebaseRepository repository = FirebaseRepository.getInstance();

        repository.getOrderCount(storeId, new FirebaseRepository.OrderCountCallback() {
            @Override
            public void onSuccess(int count) {
                String orderNumber = String.format("ORD%03d", count + 1);
                callback.onGenerated(orderNumber);
            }

            @Override
            public void onFailure(String error) {
                // Fallback to timestamp-based order number
                String orderNumber = "ORD" + System.currentTimeMillis();
                callback.onGenerated(orderNumber);
            }
        });
    }

    /**
     * Create a new order (to be called by customer app)
     * This is a sample method showing how customers would place orders
     */
    public static void placeOrder(Context context, String storeId, String customerName,
                                  String customerPhone, List<FoodItem> items,
                                  double totalPrice, OrderCallback callback) {
        FirebaseRepository repository = FirebaseRepository.getInstance();

        // First generate order number
        generateOrderNumber(context, storeId, orderNumber -> {
            OrderEntity order = new OrderEntity();
            order.setStoreId(storeId);
            order.setOrderNumber(orderNumber);
            order.setCustomerName(customerName);
            order.setCustomerPhone(customerPhone);
            order.setTotalPrice(totalPrice);
            order.setItemsJson(convertItemsToJson(items));

            // Add order to Firestore
            repository.addOrder(order, new FirebaseRepository.GenericCallback() {
                @Override
                public void onSuccess() {
                    callback.onSuccess(order);
                }

                @Override
                public void onFailure(String error) {
                    callback.onFailure(error);
                }
            });
        });
    }

    /**
     * Convert list of food items to JSON string for storage
     */
    private static String convertItemsToJson(List<FoodItem> items) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (FoodItem item : items) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", item.getName());
                jsonObject.put("price", item.getPriceSmall()); // You can adjust based on size
                jsonArray.put(jsonObject);
            }
            return jsonArray.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    /**
     * Sample method to create a test order (for testing purposes)
     */
    public static void createTestOrder(Context context, String storeId, OrderCallback callback) {
        FirebaseRepository repository = FirebaseRepository.getInstance();

        generateOrderNumber(context, storeId, orderNumber -> {
            OrderEntity order = new OrderEntity();
            order.setStoreId(storeId);
            order.setOrderNumber(orderNumber);
            order.setCustomerName("Test Customer");
            order.setCustomerPhone("+92 300 1234567");
            order.setTotalPrice(250.00);
            order.setItemsJson("[\"Burger - Small\", \"Fries - Medium\"]");

            repository.addOrder(order, new FirebaseRepository.GenericCallback() {
                @Override
                public void onSuccess() {
                    callback.onSuccess(order);
                }

                @Override
                public void onFailure(String error) {
                    callback.onFailure(error);
                }
            });
        });
    }

    // Callback interfaces
    public interface OrderNumberCallback {
        void onGenerated(String orderNumber);
    }

    public interface OrderCallback {
        void onSuccess(OrderEntity order);
        void onFailure(String error);
    }
}