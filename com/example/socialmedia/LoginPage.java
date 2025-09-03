package com.example.socialmedia;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginPage extends AppCompatActivity {

    private EditText emailLogin, passwordLogin;
    private Button loginBtn;
    private TextView goToRegister, forgotPassword;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginPage.this, HomePage.class));
            finish();
            return;
        }

        emailLogin = findViewById(R.id.emailLogin);
        passwordLogin = findViewById(R.id.passwordLogin);
        loginBtn = findViewById(R.id.loginBtn);
        goToRegister = findViewById(R.id.goToRegister);
        forgotPassword = findViewById(R.id.forgotPassword);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");

        loginBtn.setOnClickListener(v -> loginUser());

        goToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginPage.this, RegisterPage.class));
            finish();
        });

        forgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void loginUser() {
        String email = emailLogin.getText().toString().trim();
        String password = passwordLogin.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailLogin.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLogin.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            passwordLogin.setError("Password must be at least 6 characters");
            return;
        }

        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginPage.this, task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginPage.this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginPage.this, HomePage.class));
                        finish();
                    } else {
                        Toast.makeText(LoginPage.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showForgotPasswordDialog() {
        EditText resetMail = new EditText(this);
        resetMail.setHint("Enter your registered email");

        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(this);
        passwordResetDialog.setTitle("Reset Password?");
        passwordResetDialog.setMessage("Enter your email to receive reset link.");
        passwordResetDialog.setView(resetMail);

        passwordResetDialog.setPositiveButton("Send", (dialog, which) -> {
            String mail = resetMail.getText().toString().trim();
            if (TextUtils.isEmpty(mail)) {
                Toast.makeText(LoginPage.this, "Email is required", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseAuth.sendPasswordResetEmail(mail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginPage.this, "Email Sent In Spam Folder", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginPage.this, "Error! Reset link not sent: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        passwordResetDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        passwordResetDialog.create().show();
    }
}
