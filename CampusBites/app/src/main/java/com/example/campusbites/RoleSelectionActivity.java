package com.example.campusbites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        CardView btnSeller = findViewById(R.id.btnSeller);
        CardView btnCustomer = findViewById(R.id.btnCustomer);

        btnSeller.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        btnCustomer.setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomerLoginActivity.class);
            startActivity(intent);
        });
    }
}