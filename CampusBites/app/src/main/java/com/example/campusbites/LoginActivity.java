package com.example.campusbites;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.campusbites.Store;
import com.example.campusbites.FirebaseRepository;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvSignup, tvBackToRole;
    private SharedPreferences prefs;
    private FirebaseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        repository = FirebaseRepository.getInstance();
        prefs = getSharedPreferences("SmartCanteenPrefs", MODE_PRIVATE);

        // Check if user is already logged in
        try {
            String currentUserId = repository.getCurrentUserId();
            if (currentUserId != null) {
                String storeId = prefs.getString("STORE_ID", null);
                if (storeId != null && !storeId.isEmpty()) {
                    startMainActivity();
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        try {
            etEmail = findViewById(R.id.etEmail);
            etPassword = findViewById(R.id.etPassword);
            btnLogin = findViewById(R.id.btnLogin);
            tvSignup = findViewById(R.id.tvSignup);
            tvBackToRole = findViewById(R.id.tvBackToRole);
            progressBar = findViewById(R.id.progressBar);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        try {
            if (btnLogin != null) {
                btnLogin.setOnClickListener(v -> loginUser());
            }

            if (tvSignup != null) {
                tvSignup.setOnClickListener(v -> {
                    startActivity(new Intent(LoginActivity.this, RegisterStoreActivity.class));
                });
            }

            if (tvBackToRole != null) {
                tvBackToRole.setOnClickListener(v -> {
                    startActivity(new Intent(LoginActivity.this, RoleSelectionActivity.class));
                    finish();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loginUser() {
        try {
            if (etEmail == null || etPassword == null) {
                return;
            }

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email is required");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Password is required");
                return;
            }

            showLoading(true);

            if (repository == null) {
                showLoading(false);
                Toast.makeText(LoginActivity.this,
                        "Error: Repository not initialized", Toast.LENGTH_SHORT).show();
                return;
            }

            repository.loginStore(email, password, new FirebaseRepository.AuthCallback() {
                @Override
                public void onSuccess(String userId, Store store) {
                    try {
                        if (userId == null || userId.isEmpty() || store == null) {
                            showLoading(false);
                            Toast.makeText(LoginActivity.this,
                                    "Login failed: Invalid data", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Save store info to SharedPreferences
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("STORE_ID", userId);
                        if (store.getStoreName() != null) {
                            editor.putString("STORE_NAME", store.getStoreName());
                        }
                        editor.apply();
                        
                        // Also save to CampusBitesPrefs for SplashActivity to detect
                        SharedPreferences campusBitesPrefs = getSharedPreferences("CampusBitesPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor campusBitesEditor = campusBitesPrefs.edit();
                        campusBitesEditor.putString("USER_ID", userId);
                        campusBitesEditor.putString("USER_TYPE", "seller");
                        campusBitesEditor.apply();

                        showLoading(false);
                        Toast.makeText(LoginActivity.this,
                                "Welcome " + (store.getStoreName() != null ? store.getStoreName() : "Seller"), 
                                Toast.LENGTH_SHORT).show();
                        startMainActivity();
                    } catch (Exception e) {
                        e.printStackTrace();
                        showLoading(false);
                        Toast.makeText(LoginActivity.this,
                                "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(String error) {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this,
                            "Login failed: " + (error != null ? error : "Unknown error"), 
                            Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showLoading(false);
            Toast.makeText(LoginActivity.this,
                    "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        try {
            if (progressBar != null) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
            if (btnLogin != null) {
                btnLogin.setEnabled(!show);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}