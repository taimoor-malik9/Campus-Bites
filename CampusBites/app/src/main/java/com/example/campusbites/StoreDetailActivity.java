package com.example.campusbites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class StoreDetailActivity extends AppCompatActivity
        implements CustomerFoodAdapter.OnFoodClickListener {

    private ImageView ivStoreLogo, ivBack;
    private TextView tvStoreName, tvStoreDesc, tvNoItems;
    private RecyclerView rvFoodItems;
    private ProgressBar progressBar;

    private FirebaseRepository repository;
    private CustomerFoodAdapter adapter;
    private List<FoodItem> foodList = new ArrayList<>();
    private String storeId;
    private String storeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_detail);

        repository = FirebaseRepository.getInstance();

        storeId = getIntent().getStringExtra("STORE_ID");
        storeName = getIntent().getStringExtra("STORE_NAME");

        initializeViews();
        setupRecyclerView();
        loadStoreData();
        loadFoodItems();
    }

    private void initializeViews() {
        ivStoreLogo = findViewById(R.id.ivStoreLogo);
        ivBack = findViewById(R.id.ivBack);
        tvStoreName = findViewById(R.id.tvStoreName);
        tvStoreDesc = findViewById(R.id.tvStoreDesc);
        tvNoItems = findViewById(R.id.tvNoItems);
        rvFoodItems = findViewById(R.id.rvFoodItems);
        progressBar = findViewById(R.id.progressBar);

        tvStoreName.setText(storeName);

        ivBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvFoodItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomerFoodAdapter(foodList, this);
        rvFoodItems.setAdapter(adapter);
    }

    private void loadStoreData() {
        repository.getStoreById(storeId, new FirebaseRepository.StoreCallback() {
            @Override
            public void onSuccess(Store store) {
                tvStoreName.setText(store.getStoreName());
                tvStoreDesc.setText(store.getStoreDescription());

                if (store.getStoreImageUrl() != null && !store.getStoreImageUrl().isEmpty()) {
                    Glide.with(StoreDetailActivity.this)
                            .load(store.getStoreImageUrl())
                            .placeholder(R.drawable.placeholder)
                            .into(ivStoreLogo);
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(StoreDetailActivity.this,
                        "Failed to load store", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFoodItems() {
        showLoading(true);

        repository.getFoodItemsByStoreId(storeId, new FirebaseRepository.FoodListCallback() {
            @Override
            public void onSuccess(List<FoodItem> items) {
                showLoading(false);
                foodList.clear();
                foodList.addAll(items);
                adapter.notifyDataSetChanged();

                if (items.isEmpty()) {
                    tvNoItems.setVisibility(View.VISIBLE);
                    rvFoodItems.setVisibility(View.GONE);
                } else {
                    tvNoItems.setVisibility(View.GONE);
                    rvFoodItems.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                Toast.makeText(StoreDetailActivity.this,
                        "Failed to load menu: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFoodClick(FoodItem item) {
        Intent intent = new Intent(this, FoodDetailActivity.class);
        intent.putExtra("FOOD_ID", item.getId());
        intent.putExtra("STORE_ID", storeId);
        intent.putExtra("STORE_NAME", storeName);
        startActivity(intent);
    }

    @Override
    public void onAddToCart(FoodItem item, String size, double price) {
        CartManager cartManager = CartManager.getInstance(this);

        // Check if cart has items from different store
        if (cartManager.hasItems() && cartManager.hasItemsFromDifferentStore(storeId)) {
            Toast.makeText(this,
                    "Please clear your cart first. You can only order from one store at a time.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        CartItem cartItem = new CartItem(item, size, price, 1);
        cartItem.setStoreName(storeName);
        cartManager.addItem(cartItem);

        Toast.makeText(this, item.getName() + " added to cart", Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}