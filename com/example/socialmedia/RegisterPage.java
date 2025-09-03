package com.example.socialmedia;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterPage extends AppCompatActivity {

    private EditText nameRegister, emailRegister, passwordRegister;
    private Button registerBtn;
    private TextView goToLogin;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        nameRegister = findViewById(R.id.nameRegister);
        emailRegister = findViewById(R.id.emailRegister);
        passwordRegister = findViewById(R.id.passwordRegister);
        registerBtn = findViewById(R.id.registerBtn);
        goToLogin = findViewById(R.id.goToLogin);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        registerBtn.setOnClickListener(view -> registerUser());

        goToLogin.setOnClickListener(view -> {
            startActivity(new Intent(RegisterPage.this, LoginPage.class));
            finish();
        });
    }

    private void registerUser() {
        String name = nameRegister.getText().toString().trim();
        String email = emailRegister.getText().toString().trim();
        String password = passwordRegister.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameRegister.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailRegister.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordRegister.setError("Password must be 6+ characters");
            return;
        }

        registerBtn.setEnabled(false);
        registerBtn.setText("Registering...");

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    registerBtn.setEnabled(true);
                    registerBtn.setText("Register");

                    if (task.isSuccessful()) {
                        FirebaseUser currentUser = auth.getCurrentUser();
                        if (currentUser != null) {
                            String uid = currentUser.getUid();

                            Map<String, Object> user = new HashMap<>();
                            user.put("uid", uid);
                            user.put("name", name);
                            user.put("email", email);
                            user.put("bio", "Hey! I'm new here.");
                            user.put("profilePic", "");
                            user.put("following", new HashMap<>());

                            db.collection("Users").document(uid)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterPage.this, "Account created!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterPage.this, LoginPage.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(RegisterPage.this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                    );
                        }
                    } else {
                        Toast.makeText(RegisterPage.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
