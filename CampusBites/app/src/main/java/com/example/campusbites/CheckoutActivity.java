package com.example.campusbites;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private EditText etName, etPhone, etAddress;
    private RadioGroup rgPayment;
    private RecyclerView rvOrderSummary;
    private TextView tvStoreName, tvTotalItems, tvTotalPrice;
    private Button btnPlaceOrder;
    private ProgressBar progressBar;

    private CartManager cartManager;
    private FirebaseRepository repository;
    private String customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        cartManager = CartManager.getInstance(this);
        repository = FirebaseRepository.getInstance();

        SharedPreferences prefs = getSharedPreferences("CampusBitesPrefs", MODE_PRIVATE);
        customerId = prefs.getString("USER_ID", null);

        initializeViews();
        loadCustomerInfo();
        displayOrderSummary();
        setupListeners();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        rgPayment = findViewById(R.id.rgPayment);
        rvOrderSummary = findViewById(R.id.rvOrderSummary);
        tvStoreName = findViewById(R.id.tvStoreName);
        tvTotalItems = findViewById(R.id.tvTotalItems);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
    }

    private void loadCustomerInfo() {
        SharedPreferences prefs = getSharedPreferences("CampusBitesPrefs", MODE_PRIVATE);
        etName.setText(prefs.getString("CUSTOMER_NAME", ""));
        etPhone.setText(prefs.getString("CUSTOMER_PHONE", ""));
    }

    private void displayOrderSummary() {
        List<CartItem> items = cartManager.getCartItems();

        if (!items.isEmpty()) {
            tvStoreName.setText("Store: " + items.get(0).getStoreName());
        }

        tvTotalItems.setText("Items: " + cartManager.getItemCount());
        tvTotalPrice.setText("Total: Rs " + String.format("%.0f", cartManager.getTotalPrice()));

        // Simple summary adapter
        rvOrderSummary.setLayoutManager(new LinearLayoutManager(this));
        rvOrderSummary.setAdapter(new CheckoutSummaryAdapter(items));
    }

    private void setupListeners() {
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get payment method
        String paymentMethod = "Cash";
        int selectedId = rgPayment.getCheckedRadioButtonId();
        if (selectedId == R.id.rbCard) {
            paymentMethod = "Card";
        } else if (selectedId == R.id.rbJazzCash) {
            paymentMethod = "JazzCash";
        } else if (selectedId == R.id.rbEasypaisa) {
            paymentMethod = "Easypaisa";
        }

        showLoading(true);

        List<CartItem> items = cartManager.getCartItems();
        if (items.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create order
        OrderEntity order = new OrderEntity();
        order.setCustomerId(customerId);
        order.setCustomerName(name);
        order.setCustomerPhone(phone);
        order.setCustomerAddress(address);
        order.setStoreId(items.get(0).getStoreId());
        order.setStoreName(items.get(0).getStoreName());
        order.setTotalPrice(cartManager.getTotalPrice());
        order.setPaymentMethod(paymentMethod);
        order.setItemsJson(new Gson().toJson(items));
        order.setOrderNumber("ORD" + System.currentTimeMillis());

        String finalPaymentMethod = paymentMethod;
        repository.placeOrder(order, new FirebaseRepository.OrderCallback() {
            @Override
            public void onSuccess(OrderEntity order) {
                showLoading(false);
                cartManager.clearCart();

                Toast.makeText(CheckoutActivity.this,
                        "Order Placed Successfully!", Toast.LENGTH_LONG).show();

                // Go to order confirmation or main screen
                Intent intent = new Intent(CheckoutActivity.this, CustomerMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                Toast.makeText(CheckoutActivity.this,
                        "Failed to place order: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnPlaceOrder.setEnabled(!show);
    }
}