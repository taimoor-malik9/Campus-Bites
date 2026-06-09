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

public class CustomerRegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private Button btnRegister;
    private ProgressBar progressBar;
    private TextView tvLogin;
    private FirebaseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_register);

        repository = FirebaseRepository.getInstance();
        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> registerCustomer());

        tvLogin.setOnClickListener(v -> {
            finish(); // Go back to login
        });
    }

    private void registerCustomer() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone is required");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        showLoading(true);

        Customer customer = new Customer(fullName, email, phone);

        repository.registerCustomer(email, password, customer,
                new FirebaseRepository.CustomerAuthCallback() {
                    @Override
                    public void onSuccess(String userId, Customer customer) {
                        SharedPreferences prefs = getSharedPreferences("CampusBitesPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("USER_ID", userId);
                        editor.putString("USER_TYPE", "customer");
                        editor.putString("CUSTOMER_NAME", customer.getFullName());
                        editor.putString("CUSTOMER_EMAIL", customer.getEmail());
                        editor.putString("CUSTOMER_PHONE", customer.getPhone());
                        editor.apply();

                        showLoading(false);
                        Toast.makeText(CustomerRegisterActivity.this,
                                "Registration Successful!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(CustomerRegisterActivity.this,
                                CustomerMainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        showLoading(false);
                        Toast.makeText(CustomerRegisterActivity.this,
                                "Registration failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
    }
}