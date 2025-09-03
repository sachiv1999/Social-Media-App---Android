package com.example.socialmedia;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

public class SettingsPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);

        findViewById(R.id.helpBtn).setOnClickListener(v ->
                Toast.makeText(this, "Help section clicked", Toast.LENGTH_SHORT).show());

        findViewById(R.id.termsBtn).setOnClickListener(v ->
                Toast.makeText(this, "Terms clicked", Toast.LENGTH_SHORT).show());

        findViewById(R.id.privacyBtn).setOnClickListener(v ->
                Toast.makeText(this, "Privacy Policy clicked", Toast.LENGTH_SHORT).show());
    }
}
