package com.example.campusbites;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class FoodDetailActivity extends AppCompatActivity {

    private ImageView ivFoodImage, ivBack;
    private TextView tvFoodName, tvFoodDesc, tvSelectedPrice, tvQuantity;
    private RadioGroup rgSize;
    private RadioButton rbSmall, rbMedium, rbLarge;
    private Button btnMinus, btnPlus, btnAddToCart;

    private FirebaseRepository repository;
    private FoodItem currentFood;
    private String storeId, storeName;
    private int quantity = 1;
    private String selectedSize = "Medium";
    private double selectedPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        repository = FirebaseRepository.getInstance();

        String foodId = getIntent().getStringExtra("FOOD_ID");
        storeId = getIntent().getStringExtra("STORE_ID");
        storeName = getIntent().getStringExtra("STORE_NAME");

        initializeViews();
        setupListeners();
        loadFoodDetails(foodId);
    }

    private void initializeViews() {
        ivFoodImage = findViewById(R.id.ivFoodImage);
        ivBack = findViewById(R.id.ivBack);
        tvFoodName = findViewById(R.id.tvFoodName);
        tvFoodDesc = findViewById(R.id.tvFoodDesc);
        tvSelectedPrice = findViewById(R.id.tvSelectedPrice);
        tvQuantity = findViewById(R.id.tvQuantity);
        rgSize = findViewById(R.id.rgSize);
        rbSmall = findViewById(R.id.rbSmall);
        rbMedium = findViewById(R.id.rbMedium);
        rbLarge = findViewById(R.id.rbLarge);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        btnAddToCart = findViewById(R.id.btnAddToCart);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        rgSize.setOnCheckedChangeListener((group, checkedId) -> {
            if (currentFood == null) return;

            if (checkedId == R.id.rbSmall) {
                selectedSize = "Small";
                selectedPrice = currentFood.getPriceSmall();
            } else if (checkedId == R.id.rbMedium) {
                selectedSize = "Medium";
                selectedPrice = currentFood.getPriceMedium();
            } else if (checkedId == R.id.rbLarge) {
                selectedSize = "Large";
                selectedPrice = currentFood.getPriceLarge();
            }
            updatePriceDisplay();
        });

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
                updatePriceDisplay();
            }
        });

        btnPlus.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
            updatePriceDisplay();
        });

        btnAddToCart.setOnClickListener(v -> addToCart());
    }

    private void loadFoodDetails(String foodId) {
        repository.getFoodItemById(foodId, new FirebaseRepository.FoodItemCallback() {
            @Override
            public void onSuccess(FoodItem item) {
                currentFood = item;
                displayFoodDetails();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(FoodDetailActivity.this,
                        "Failed to load item", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayFoodDetails() {
        tvFoodName.setText(currentFood.getName());
        tvFoodDesc.setText(currentFood.getDescription());

        rbSmall.setText("Small - Rs " + String.format("%.0f", currentFood.getPriceSmall()));
        rbMedium.setText("Medium - Rs " + String.format("%.0f", currentFood.getPriceMedium()));
        rbLarge.setText("Large - Rs " + String.format("%.0f", currentFood.getPriceLarge()));

        // Default selection
        rbMedium.setChecked(true);
        selectedPrice = currentFood.getPriceMedium();
        updatePriceDisplay();

        if (currentFood.getImageUrl() != null && !currentFood.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentFood.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .into(ivFoodImage);
        }
    }

    private void updatePriceDisplay() {
        double total = selectedPrice * quantity;
        tvSelectedPrice.setText("Total: Rs " + String.format("%.0f", total));
    }

    private void addToCart() {
        if (currentFood == null) return;

        CartManager cartManager = CartManager.getInstance(this);

        // Check if cart has items from different store
        if (cartManager.hasItems() && cartManager.hasItemsFromDifferentStore(storeId)) {
            Toast.makeText(this,
                    "Clear cart first! You can only order from one store at a time.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        CartItem cartItem = new CartItem(currentFood, selectedSize, selectedPrice, quantity);
        cartItem.setStoreName(storeName);
        cartManager.addItem(cartItem);

        Toast.makeText(this, "Added to cart!", Toast.LENGTH_SHORT).show();
        finish();
    }
}