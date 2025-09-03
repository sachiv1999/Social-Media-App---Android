package com.example.socialmedia;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.*;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        TextView appName = findViewById(R.id.appName);

        AnimationSet animationSet = new AnimationSet(true);
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1000);

        ScaleAnimation scale = new ScaleAnimation(
                0f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(1000);

        animationSet.addAnimation(fadeIn);
        animationSet.addAnimation(scale);
        animationSet.setFillAfter(true);

        appName.startAnimation(animationSet);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashScreen.this, LoginPage.class));
            finish();
        }, SPLASH_DURATION);
    }
}
