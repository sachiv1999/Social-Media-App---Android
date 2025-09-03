package com.example.socialmedia;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_page);

        TextView aboutText = findViewById(R.id.aboutText);
        aboutText.setText("social media app built for Users to share posts, connect with friends, and interact in real-time.\n\nVersion: 1.0\nDeveloper: Manav Pandya , Himanshu Jagatiya And Sachiv Chapadiya");
    }
}
