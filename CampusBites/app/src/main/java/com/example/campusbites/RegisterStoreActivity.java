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
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.campusbites.Store;
import com.example.campusbites.FirebaseRepository;

public class RegisterStoreActivity extends AppCompatActivity {

    private EditText etStoreName, etStoreDesc, etOwnerName, etEmail, etPassword;
    private ImageView ivLogo;
    private Button btnSelectLogo, btnRegister;
    private ProgressBar progressBar;
    private TextView tvUploadProgress;

    private Uri selectedImageUri;
    private FirebaseRepository repository;

    ActivityResultLauncher<Intent> logoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivLogo.setImageURI(selectedImageUri);
                    Toast.makeText(this, "Logo Selected", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_store);

        repository = FirebaseRepository.getInstance();
        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        etStoreName = findViewById(R.id.etStoreName);
        etStoreDesc = findViewById(R.id.etStoreDesc);
        etOwnerName = findViewById(R.id.etOwnerName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ivLogo = findViewById(R.id.ivStoreLogoPreview);
        btnSelectLogo = findViewById(R.id.btnSelectLogo);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvUploadProgress = findViewById(R.id.tvUploadProgress);
    }

    private void setupListeners() {
        btnSelectLogo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            logoLauncher.launch(intent);
        });

        btnRegister.setOnClickListener(v -> registerStore());
    }

    private void registerStore() {
        String name = etStoreName.getText().toString().trim();
        String desc = etStoreDesc.getText().toString().trim();
        String owner = etOwnerName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty()) {
            etStoreName.setError("Store name is required");
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        showLoading(true);

        // Upload image to Cloudinary first, then create store
        if (selectedImageUri != null) {
            uploadImageAndRegister(name, desc, owner, email, password);
        } else {
            createStore(name, desc, owner, email, password, "");
        }
    }

    private void uploadImageAndRegister(String name, String desc, String owner,
                                        String email, String password) {
        tvUploadProgress.setVisibility(View.VISIBLE);
        tvUploadProgress.setText("Uploading image: 0%");

        // Upload to Cloudinary with folder "store_logos"
        repository.uploadImage(this, selectedImageUri, "store_logos",
                new FirebaseRepository.ImageUploadCallback() {
                    @Override
                    public void onSuccess(String downloadUrl) {
                        tvUploadProgress.setText("Upload complete!");
                        createStore(name, desc, owner, email, password, downloadUrl);
                    }

                    @Override
                    public void onProgress(int progress) {
                        tvUploadProgress.setText("Uploading image: " + progress + "%");
                    }

                    @Override
                    public void onFailure(String error) {
                        showLoading(false);
                        tvUploadProgress.setVisibility(View.GONE);
                        Toast.makeText(RegisterStoreActivity.this,
                                "Image upload failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createStore(String name, String desc, String owner,
                             String email, String password, String imageUrl) {
        tvUploadProgress.setText("Creating account...");

        Store newStore = new Store(owner, email, name, desc, imageUrl);

        repository.registerStore(email, password, newStore,
                new FirebaseRepository.AuthCallback() {
                    @Override
                    public void onSuccess(String userId, Store store) {
                        // Save store info to SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("SmartCanteenPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("STORE_ID", userId);
                        editor.putString("STORE_NAME", store.getStoreName());
                        editor.apply();
                        
                        // Also save to CampusBitesPrefs for SplashActivity to detect
                        SharedPreferences campusBitesPrefs = getSharedPreferences("CampusBitesPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor campusBitesEditor = campusBitesPrefs.edit();
                        campusBitesEditor.putString("USER_ID", userId);
                        campusBitesEditor.putString("USER_TYPE", "seller");
                        campusBitesEditor.apply();
                        
                        showLoading(false);
                        tvUploadProgress.setVisibility(View.GONE);
                        Toast.makeText(RegisterStoreActivity.this,
                                "Store Registered Successfully!", Toast.LENGTH_SHORT).show();
                        
                        // Navigate to MainActivity
                        Intent intent = new Intent(RegisterStoreActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        showLoading(false);
                        tvUploadProgress.setVisibility(View.GONE);
                        Toast.makeText(RegisterStoreActivity.this,
                                "Registration failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
        btnSelectLogo.setEnabled(!show);
    }
}