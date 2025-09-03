package com.example.socialmedia;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

public class addsActivity extends AppCompatActivity {
    ImageView ad1, ad2;

    ImageButton btnCloseAds;
    Handler handler = new Handler();
    String targetActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_adds);
        targetActivity = getIntent().getStringExtra("target");
        if (targetActivity == null) targetActivity = "home";


        btnCloseAds = findViewById(R.id.btnCloseAds);

        ad1 = findViewById(R.id.adImage);
        ad2 = findViewById(R.id.adImage2);

        ad1.setVisibility(View.GONE);
        ad2.setVisibility(View.GONE);
        btnCloseAds.setVisibility(View.GONE);

        ad1.setVisibility(View.VISIBLE);

        handler.postDelayed(() -> {
            ad1.setVisibility(View.GONE);
            ad2.setVisibility(View.VISIBLE);
            }, 3000);

        new Handler().postDelayed(() -> btnCloseAds.setVisibility(View.VISIBLE), 6000);

        btnCloseAds.setOnClickListener(v -> {

            Intent intent;
            switch (targetActivity) {
                case "search":
                    intent = new Intent(addsActivity.this, FriendList.class);
                    break;
                default:
                    intent = new Intent(addsActivity.this, HomePage.class);
                    break;
            }

//            Intent intent = new Intent(addsActivity.this, SecondActivity.class);
              startActivity(intent);
              finish();
        });
    }
}



