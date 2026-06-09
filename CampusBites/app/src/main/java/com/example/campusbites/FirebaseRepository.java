package com.example.campusbites;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FirebaseRepository {

    private static final String TAG = "FirebaseRepository";
    private static FirebaseRepository instance;

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final CloudinaryHelper cloudinary;

    // Collection names
    private static final String STORES_COLLECTION = "stores";
    private static final String FOOD_ITEMS_COLLECTION = "foodItems";
    private static final String ORDERS_COLLECTION = "orders";
    private static final String CUSTOMERS_COLLECTION = "customers";
    private static final String CART_COLLECTION = "cart";

    private FirebaseRepository() {
        db = FirebaseFirestore.getInstance();

        // Enable offline persistence
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        auth = FirebaseAuth.getInstance();
        cloudinary = CloudinaryHelper.getInstance();
    }

    public static synchronized FirebaseRepository getInstance() {
        if (instance == null) {
            instance = new FirebaseRepository();
        }
        return instance;
    }

    // ============================================================================================
    // 1. AUTHENTICATION (SELLER & CUSTOMER)
    // ============================================================================================

    // --- Interfaces ---
    public interface AuthCallback {
        void onSuccess(String userId, Store store); // Fixed: Returns Store
        void onFailure(String error);
    }

    public interface CustomerAuthCallback {
        void onSuccess(String userId, Customer customer);
        void onFailure(String error);
    }

    // --- Seller Auth Methods ---
    public void registerStore(String email, String password, Store store, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = authResult.getUser().getUid();
                    store.setId(userId);

                    db.collection(STORES_COLLECTION)
                            .document(userId)
                            .set(store)
                            .addOnSuccessListener(aVoid -> callback.onSuccess(userId, store))
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void loginStore(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = authResult.getUser().getUid();
                    getStoreById(userId, new StoreCallback() {
                        @Override
                        public void onSuccess(Store store) {
                            callback.onSuccess(userId, store);
                        }

                        @Override
                        public void onFailure(String error) {
                            callback.onFailure(error);
                        }
                    });
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // --- Customer Auth Methods ---
    public void registerCustomer(String email, String password, Customer customer,
                                 CustomerAuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = authResult.getUser().getUid();
                    customer.setId(userId);

                    db.collection(CUSTOMERS_COLLECTION)
                            .document(userId)
                            .set(customer)
                            .addOnSuccessListener(aVoid -> callback.onSuccess(userId, customer))
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void loginCustomer(String email, String password, CustomerAuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = authResult.getUser().getUid();
                    getCustomerById(userId, new CustomerCallback() {
                        @Override
                        public void onSuccess(Customer customer) {
                            callback.onSuccess(userId, customer);
                        }

                        @Override
                        public void onFailure(String error) {
                            callback.onFailure("Not a customer account or data missing");
                        }
                    });
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void logout() {
        auth.signOut();
    }

    public String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    // ============================================================================================
    // 2. CUSTOMER OPERATIONS
    // ============================================================================================

    public interface CustomerCallback {
        void onSuccess(Customer customer);
        void onFailure(String error);
    }

    public void getCustomerById(String customerId, CustomerCallback callback) {
        db.collection(CUSTOMERS_COLLECTION)
                .document(customerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Customer customer = documentSnapshot.toObject(Customer.class);
                        if (customer != null) {
                            customer.setId(documentSnapshot.getId());
                            callback.onSuccess(customer);
                        } else {
                            callback.onFailure("Customer data is null");
                        }
                    } else {
                        callback.onFailure("Customer not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateCustomer(String customerId, Customer customer, GenericCallback callback) {
        db.collection(CUSTOMERS_COLLECTION)
                .document(customerId)
                .set(customer)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteCustomer(String customerId, GenericCallback callback) {
        db.collection(CUSTOMERS_COLLECTION)
                .document(customerId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ============================================================================================
    // 3. STORE OPERATIONS
    // ============================================================================================

    public interface StoreCallback {
        void onSuccess(Store store);
        void onFailure(String error);
    }

    public interface StoreListCallback {
        void onSuccess(List<Store> stores);
        void onFailure(String error);
    }

    public void getStoreById(String storeId, StoreCallback callback) {
        db.collection(STORES_COLLECTION)
                .document(storeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Store store = documentSnapshot.toObject(Store.class);
                        if (store != null) {
                            store.setId(documentSnapshot.getId());
                            callback.onSuccess(store);
                        } else {
                            callback.onFailure("Store data is null");
                        }
                    } else {
                        callback.onFailure("Store not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getAllStores(StoreListCallback callback) {
        db.collection(STORES_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Store> stores = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Store store = doc.toObject(Store.class);
                        if (store != null) {
                            store.setId(doc.getId());
                            stores.add(store);
                        }
                    }
                    callback.onSuccess(stores);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateStore(String storeId, Store store, GenericCallback callback) {
        db.collection(STORES_COLLECTION)
                .document(storeId)
                .set(store)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteStore(String storeId, GenericCallback callback) {
        db.collection(STORES_COLLECTION)
                .document(storeId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ============================================================================================
    // 4. FOOD ITEM OPERATIONS
    // ============================================================================================

    public interface FoodListCallback {
        void onSuccess(List<FoodItem> items);
        void onFailure(String error);
    }

    public interface FoodItemCallback {
        void onSuccess(FoodItem item);
        void onFailure(String error);
    }

    public void addFoodItem(FoodItem item, FoodItemCallback callback) {
        db.collection(FOOD_ITEMS_COLLECTION)
                .add(item)
                .addOnSuccessListener(documentReference -> {
                    item.setId(documentReference.getId());
                    callback.onSuccess(item);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateFoodItem(String itemId, FoodItem item, GenericCallback callback) {
        db.collection(FOOD_ITEMS_COLLECTION)
                .document(itemId)
                .set(item)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteFoodItem(String itemId, GenericCallback callback) {
        db.collection(FOOD_ITEMS_COLLECTION)
                .document(itemId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getFoodItemsByStoreId(String storeId, FoodListCallback callback) {
        db.collection(FOOD_ITEMS_COLLECTION)
                .whereEqualTo("storeId", storeId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<FoodItem> items = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        FoodItem item = doc.toObject(FoodItem.class);
                        if (item != null) {
                            item.setId(doc.getId());
                            items.add(item);
                        }
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getFoodItemById(String itemId, FoodItemCallback callback) {
        db.collection(FOOD_ITEMS_COLLECTION)
                .document(itemId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        FoodItem item = documentSnapshot.toObject(FoodItem.class);
                        if (item != null) {
                            item.setId(documentSnapshot.getId());
                            callback.onSuccess(item);
                        }
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ============================================================================================
    // 5. CART OPERATIONS
    // ============================================================================================

    public interface CartCallback {
        void onSuccess(List<CartItem> cartItems);
        void onFailure(String error);
    }

    public void addToCart(String customerId, CartItem cartItem, GenericCallback callback) {
        db.collection(CUSTOMERS_COLLECTION)
                .document(customerId)
                .collection(CART_COLLECTION)
                .add(cartItem)
                .addOnSuccessListener(documentReference -> {
                    cartItem.setId(documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getCartItems(String customerId, CartCallback callback) {
        db.collection(CUSTOMERS_COLLECTION)
                .document(customerId)
                .collection(CART_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<CartItem> items = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        CartItem item = doc.toObject(CartItem.class);
                        if (item != null) {
                            item.setId(doc.getId());
                            items.add(item);
                        }
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateCartItem(String customerId, CartItem cartItem, GenericCallback callback) {
        db.collection(CUSTOMERS_COLLECTION)
                .document(customerId)
                .collection(CART_COLLECTION)
                .document(cartItem.getId())
                .set(cartItem)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void removeFromCart(String customerId, String cartItemId, GenericCallback callback) {
        db.collection(CUSTOMERS_COLLECTION)
                .document(customerId)
                .collection(CART_COLLECTION)
                .document(cartItemId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void clearCart(String customerId, GenericCallback callback) {
        db.collection(CUSTOMERS_COLLECTION)
                .document(customerId)
                .collection(CART_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ============================================================================================
    // 6. ORDER OPERATIONS
    // ============================================================================================

    public interface OrderListCallback {
        void onSuccess(List<OrderEntity> orders);
        void onFailure(String error);
    }

    public interface OrderCallback {
        void onSuccess(OrderEntity order);
        void onFailure(String error);
    }

    public interface OrderCountCallback {
        void onSuccess(int count);
        void onFailure(String error);
    }

    public void placeOrder(OrderEntity order, OrderCallback callback) {
        db.collection(ORDERS_COLLECTION)
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    order.setId(documentReference.getId());
                    callback.onSuccess(order);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Alias for placeOrder (for compatibility)
    public void addOrder(OrderEntity order, GenericCallback callback) {
        db.collection(ORDERS_COLLECTION)
                .add(order)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateOrder(String orderId, OrderEntity order, GenericCallback callback) {
        db.collection(ORDERS_COLLECTION)
                .document(orderId)
                .set(order)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // For Sellers
    public void getOrdersByStoreId(String storeId, OrderListCallback callback) {
        if (storeId == null || storeId.isEmpty()) {
            callback.onFailure("Store ID is required");
            return;
        }
        
        try {
            // First try with orderBy - if index is missing, fallback to without orderBy
            db.collection(ORDERS_COLLECTION)
                    .whereEqualTo("storeId", storeId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<OrderEntity> orders = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            OrderEntity order = doc.toObject(OrderEntity.class);
                            if (order != null) {
                                order.setId(doc.getId());
                                orders.add(order);
                            }
                        }
                        // Sort manually if needed (fallback)
                        orders.sort((o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                        callback.onSuccess(orders);
                    })
                    .addOnFailureListener(e -> {
                        // If orderBy fails (index missing), try without orderBy
                        if (e.getMessage() != null && e.getMessage().contains("index")) {
                            db.collection(ORDERS_COLLECTION)
                                    .whereEqualTo("storeId", storeId)
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        List<OrderEntity> orders = new ArrayList<>();
                                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                            OrderEntity order = doc.toObject(OrderEntity.class);
                                            if (order != null) {
                                                order.setId(doc.getId());
                                                orders.add(order);
                                            }
                                        }
                                        // Sort manually by timestamp
                                        orders.sort((o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                                        callback.onSuccess(orders);
                                    })
                                    .addOnFailureListener(e2 -> callback.onFailure(e2.getMessage()));
                        } else {
                            callback.onFailure(e.getMessage());
                        }
                    });
        } catch (Exception e) {
            callback.onFailure(e.getMessage());
        }
    }

    // For Customers
    public void getOrdersByCustomerId(String customerId, OrderListCallback callback) {
        if (customerId == null || customerId.isEmpty()) {
            callback.onFailure("Customer ID is required");
            return;
        }
        
        try {
            // First try with orderBy - if index is missing, fallback to without orderBy
            db.collection(ORDERS_COLLECTION)
                    .whereEqualTo("customerId", customerId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<OrderEntity> orders = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            OrderEntity order = doc.toObject(OrderEntity.class);
                            if (order != null) {
                                order.setId(doc.getId());
                                orders.add(order);
                            }
                        }
                        // Sort manually if needed (fallback)
                        orders.sort((o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                        callback.onSuccess(orders);
                    })
                    .addOnFailureListener(e -> {
                        // If orderBy fails (index missing), try without orderBy
                        if (e.getMessage() != null && e.getMessage().contains("index")) {
                            db.collection(ORDERS_COLLECTION)
                                    .whereEqualTo("customerId", customerId)
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        List<OrderEntity> orders = new ArrayList<>();
                                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                            OrderEntity order = doc.toObject(OrderEntity.class);
                                            if (order != null) {
                                                order.setId(doc.getId());
                                                orders.add(order);
                                            }
                                        }
                                        // Sort manually by timestamp
                                        orders.sort((o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                                        callback.onSuccess(orders);
                                    })
                                    .addOnFailureListener(e2 -> callback.onFailure(e2.getMessage()));
                        } else {
                            callback.onFailure(e.getMessage());
                        }
                    });
        } catch (Exception e) {
            callback.onFailure(e.getMessage());
        }
    }

    public void getOrderCount(String storeId, OrderCountCallback callback) {
        db.collection(ORDERS_COLLECTION)
                .whereEqualTo("storeId", storeId)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        callback.onSuccess(querySnapshot.size()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ============================================================================================
    // 7. IMAGE UPLOAD & UTILS
    // ============================================================================================

    public interface ImageUploadCallback {
        void onSuccess(String downloadUrl);
        void onProgress(int progress);
        void onFailure(String error);
    }

    public interface GenericCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public void uploadImage(Context context, Uri imageUri, String folder, ImageUploadCallback callback) {
        cloudinary.initialize(context);

        cloudinary.uploadImage(context, imageUri, folder,
                new CloudinaryHelper.CloudinaryUploadCallback() {
                    @Override
                    public void onUploadStart() {
                        callback.onProgress(0);
                    }

                    @Override
                    public void onProgress(int progress) {
                        callback.onProgress(progress);
                    }

                    @Override
                    public void onSuccess(String imageUrl) {
                        callback.onSuccess(imageUrl);
                    }

                    @Override
                    public void onError(String error) {
                        callback.onFailure(error);
                    }
                });
    }
}