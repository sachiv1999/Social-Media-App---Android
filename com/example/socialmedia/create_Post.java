package com.example.socialmedia;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class create_Post extends AppCompatActivity {
    private EditText postEditText, imageDescriptionEditText;
    private Button addBlogBtn, addPostBtn, finalPostBtn;
    private ImageView imagePreview, backBtn;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private Bitmap selectedBitmap = null;
    private static final int IMAGE_PICK_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        postEditText = findViewById(R.id.postEditText);
        imageDescriptionEditText = findViewById(R.id.imageDescriptionEditText);
        addBlogBtn = findViewById(R.id.addBlogBtn);
        addPostBtn = findViewById(R.id.addPostBtn);
        finalPostBtn = findViewById(R.id.finalPostBtn);
        imagePreview = findViewById(R.id.imagePreview);
        backBtn = findViewById(R.id.backBtn);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        addBlogBtn.setOnClickListener(v -> uploadPost(false));
        addPostBtn.setOnClickListener(v -> chooseImage());
        finalPostBtn.setOnClickListener(v -> {
            if (selectedBitmap != null) uploadPost(true);
            else Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        });

        backBtn.setOnClickListener(v -> onBackPressed());
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                imagePreview.setImageBitmap(selectedBitmap);
                imagePreview.setVisibility(ImageView.VISIBLE);
                imageDescriptionEditText.setVisibility(EditText.VISIBLE);
                finalPostBtn.setVisibility(Button.VISIBLE);
                Toast.makeText(this, "Image selected. Add description & click POST.", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, "Image load failed", Toast.LENGTH_SHORT).show();
                selectedBitmap = null;
            }
        }
    }

    private void uploadPost(boolean withImage) {
        String text = postEditText.getText().toString().trim();
        String imageDescription = imageDescriptionEditText.getText().toString().trim();

        if (text.isEmpty() && !withImage) {
            Toast.makeText(this, "Please write something or add image", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        long timestamp = System.currentTimeMillis();
        DocumentReference docRef = firestore.collection("Posts").document();
        String postId = docRef.getId();

        Map<String, Object> postMap = new HashMap<>();
        postMap.put("postId", postId);
        postMap.put("userId", uid);
        postMap.put("content", text);
        postMap.put("timestamp", timestamp);

        if (withImage && selectedBitmap != null) {
            postMap.put("imageBase64", encodeImageToBase64(selectedBitmap));
            if (!imageDescription.isEmpty()) {
                postMap.put("imageDescription", imageDescription);
            }
        }

        docRef.set(postMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Post uploaded!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }
}
