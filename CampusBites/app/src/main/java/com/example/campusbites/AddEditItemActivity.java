package com.example.campusbites;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.campusbites.FoodItem;
import com.example.campusbites.FirebaseRepository;


public class AddEditItemActivity extends AppCompatActivity {

    private EditText etName, etPriceSmall, etPriceMedium, etPriceLarge, etDesc;
    private ImageView ivPreview;
    private Button btnSave, btnUpload;
    private ProgressBar progressBar;

    private String itemId = null;
    private String currentStoreId;
    private Uri selectedImageUri;
    private String currentImageUrl = "";
    private FirebaseRepository repository;
    private boolean isEditMode = false;

    ActivityResultLauncher<Intent> foodImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivPreview.setImageURI(selectedImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);

        repository = FirebaseRepository.getInstance();

        SharedPreferences prefs = getSharedPreferences("SmartCanteenPrefs", MODE_PRIVATE);
        currentStoreId = prefs.getString("STORE_ID", null);

        initializeViews();

        // Check if editing existing item
        if (getIntent().hasExtra("ITEM_ID")) {
            itemId = getIntent().getStringExtra("ITEM_ID");
            isEditMode = true;
            setTitle("Edit Item");
            loadItemData();
        } else {
            setTitle("Add New Item");
        }

        setupListeners();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etPriceSmall = findViewById(R.id.etPriceSmall);
        etPriceMedium = findViewById(R.id.etPriceMedium);
        etPriceLarge = findViewById(R.id.etPriceLarge);
        etDesc = findViewById(R.id.etDescription);
        ivPreview = findViewById(R.id.ivFoodImagePreview);
        btnSave = findViewById(R.id.btnSave);
        btnUpload = findViewById(R.id.btnUploadImage);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            foodImageLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> saveItem());
    }

    private void loadItemData() {
        showLoading(true);

        repository.getFoodItemById(itemId, new FirebaseRepository.FoodItemCallback() {
            @Override
            public void onSuccess(FoodItem item) {
                etName.setText(item.getName());
                etDesc.setText(item.getDescription());
                etPriceSmall.setText(String.valueOf(item.getPriceSmall()));
                etPriceMedium.setText(String.valueOf(item.getPriceMedium()));
                etPriceLarge.setText(String.valueOf(item.getPriceLarge()));
                currentImageUrl = item.getImageUrl();

                // Load image using Glide or Picasso if you have it
                // For now, just store the URL
                showLoading(false);
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                Toast.makeText(AddEditItemActivity.this,
                        "Failed to load item: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveItem() {
        String name = etName.getText().toString().trim();
        String priceSmallStr = etPriceSmall.getText().toString().trim();
        String priceMedStr = etPriceMedium.getText().toString().trim();
        String priceLargeStr = etPriceLarge.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        double pSmall = priceSmallStr.isEmpty() ? 0 : Double.parseDouble(priceSmallStr);
        double pMed = priceMedStr.isEmpty() ? 0 : Double.parseDouble(priceMedStr);
        double pLarge = priceLargeStr.isEmpty() ? 0 : Double.parseDouble(priceLargeStr);

        showLoading(true);

        // If new image selected, upload it first
        if (selectedImageUri != null) {
            uploadImageAndSave(name, desc, pSmall, pMed, pLarge);
        } else {
            saveFoodItem(name, desc, pSmall, pMed, pLarge, currentImageUrl);
        }
    }

    private void uploadImageAndSave(String name, String desc,
                                    double pSmall, double pMed, double pLarge) {
        String imagePath = "food_images/" + System.currentTimeMillis() + ".jpg";

        // ADD 'this' as first parameter
        repository.uploadImage(this, selectedImageUri, imagePath,
                new FirebaseRepository.ImageUploadCallback() {
                    @Override
                    public void onSuccess(String downloadUrl) {
                        saveFoodItem(name, desc, pSmall, pMed, pLarge, downloadUrl);
                    }

                    @Override
                    public void onProgress(int progress) {
                        // Show progress if needed
                    }

                    @Override
                    public void onFailure(String error) {
                        showLoading(false);
                        Toast.makeText(AddEditItemActivity.this,
                                "Image upload failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveFoodItem(String name, String desc,
                              double pSmall, double pMed, double pLarge, String imageUrl) {
        FoodItem item = new FoodItem();
        item.setStoreId(currentStoreId);
        item.setName(name);
        item.setDescription(desc);
        item.setPriceSmall(pSmall);
        item.setPriceMedium(pMed);
        item.setPriceLarge(pLarge);
        item.setImageUrl(imageUrl);

        if (isEditMode) {
            item.setId(itemId);
            repository.updateFoodItem(itemId, item, new FirebaseRepository.GenericCallback() {
                @Override
                public void onSuccess() {
                    showLoading(false);
                    Toast.makeText(AddEditItemActivity.this,
                            "Item Updated", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    showLoading(false);
                    Toast.makeText(AddEditItemActivity.this,
                            "Update failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            repository.addFoodItem(item, new FirebaseRepository.FoodItemCallback() {
                @Override
                public void onSuccess(FoodItem item) {
                    showLoading(false);
                    Toast.makeText(AddEditItemActivity.this,
                            "Item Added", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    showLoading(false);
                    Toast.makeText(AddEditItemActivity.this,
                            "Failed to add item: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
        btnUpload.setEnabled(!show);
    }
}