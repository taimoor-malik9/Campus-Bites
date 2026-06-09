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

public class CustomerLoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvSignup, tvBackToRole;
    private FirebaseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        repository = FirebaseRepository.getInstance();
        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        tvBackToRole = findViewById(R.id.tvBackToRole);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> loginCustomer());

        tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(this, CustomerRegisterActivity.class));
        });

        tvBackToRole.setOnClickListener(v -> {
            startActivity(new Intent(this, RoleSelectionActivity.class));
            finish();
        });
    }

    private void loginCustomer() {
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

        repository.loginCustomer(email, password, new FirebaseRepository.CustomerAuthCallback() {
            @Override
            public void onSuccess(String userId, Customer customer) {
                SharedPreferences prefs = getSharedPreferences("CampusBitesPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("USER_ID", userId);
                editor.putString("USER_TYPE", "customer");
                editor.putString("CUSTOMER_NAME", customer.getFullName());
                editor.putString("CUSTOMER_EMAIL", customer.getEmail());
                editor.putString("CUSTOMER_PHONE", customer.getPhone());

                // Reset reminder gate for this login session so CustomerMainActivity can show it.
                editor.putBoolean("LOGIN_REMINDER_SHOWN", false);

                editor.apply();

                showLoading(false);
                Toast.makeText(CustomerLoginActivity.this,
                        "Welcome " + customer.getFullName(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(CustomerLoginActivity.this, CustomerMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                finish();
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                Toast.makeText(CustomerLoginActivity.this,
                        "Login failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
    }
}