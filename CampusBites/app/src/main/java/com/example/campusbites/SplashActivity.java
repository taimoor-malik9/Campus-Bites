package com.example.campusbites;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView ivLogo = findViewById(R.id.ivSplashLogo);
        TextView tvAppName = findViewById(R.id.tvAppName);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        ivLogo.startAnimation(fadeIn);
        tvAppName.startAnimation(slideUp);

        new Handler().postDelayed(this::navigateToNextScreen, SPLASH_DURATION);
    }

    private void navigateToNextScreen() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        // Requested behavior: always show role selection first.
        Intent intent = new Intent(this, RoleSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}