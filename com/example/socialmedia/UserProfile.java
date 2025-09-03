package com.example.socialmedia;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import Adapter.PostAdapter;
import Models.post;

public class UserProfile extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 101;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private RecyclerView postRecyclerView;
    private List<post> postList;
    private PostAdapter postAdapter;
    private TextView userName, bio, postCount, friendCount;
    private Button editProfileBtn, createPostBtn;
    private ImageView backBtn, friendRequestBtn, profileImage;
    private Bitmap selectedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        postRecyclerView = findViewById(R.id.postRecyclerView);
        userName = findViewById(R.id.userName);
        bio = findViewById(R.id.bio);
        postCount = findViewById(R.id.postCount);
        friendCount = findViewById(R.id.friendCount);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        createPostBtn = findViewById(R.id.createPostBtn);
        backBtn = findViewById(R.id.backBtn);
        friendRequestBtn = findViewById(R.id.btnFriendRequests);
        profileImage = findViewById(R.id.profileImage);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList, true);
        postRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        postRecyclerView.setAdapter(postAdapter);

        loadProfileInfo();
        loadUserPosts();
        loadFriendCount();

        createPostBtn.setOnClickListener(v -> startActivity(new Intent(this, create_Post.class)));
        friendRequestBtn.setOnClickListener(v -> startActivity(new Intent(this, FriendRequest.class)));
        backBtn.setOnClickListener(v -> onBackPressed());
        profileImage.setOnClickListener(v -> pickProfileImage());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserPosts();
        loadFriendCount();
    }

    private void pickProfileImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                profileImage.setImageBitmap(selectedBitmap);
                uploadProfileImage(selectedBitmap);
            } catch (IOException e) {
                Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadProfileImage(Bitmap bitmap) {
        String uid = auth.getCurrentUser().getUid();
        String encodedImage = encodeImageToBase64(bitmap);

        firestore.collection("Users").document(uid)
                .update("profileImage", encodedImage)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void loadUserPosts() {
        String uid = auth.getCurrentUser().getUid();

        firestore.collection("Posts")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("PostLoad", "Error getting posts: ", error);
                        return;
                    }

                    postList.clear();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            post p = doc.toObject(post.class);
                            if (p != null) postList.add(p);
                        }
                    }

                    postCount.setText(String.valueOf(postList.size()));
                    postAdapter.notifyDataSetChanged();
                });
    }

    private void loadProfileInfo() {
        String uid = auth.getCurrentUser().getUid();
        firestore.collection("Users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        userName.setText(doc.getString("name") != null ? doc.getString("name") : "Username");
                        bio.setText(doc.getString("bio") != null ? doc.getString("bio") : "No bio provided.");

                        String base64Image = doc.getString("profileImage");
                        if (base64Image != null && !base64Image.isEmpty()) {
                            try {
                                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                profileImage.setImageBitmap(bitmap);
                            } catch (Exception e) {
                                Log.e("ImageLoad", "Failed to decode image", e);
                                profileImage.setImageBitmap(null);
                            }
                        } else {
                            profileImage.setImageBitmap(null);
                        }
                    }
                });
    }

    private void loadFriendCount() {
        String uid = auth.getCurrentUser().getUid();

        firestore.collection("Users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long count = documentSnapshot.getLong("friendsCount");
                        friendCount.setText(String.valueOf(count != null ? count : 0));
                    } else {
                        friendCount.setText("0");
                    }
                })
                .addOnFailureListener(e -> {
                    friendCount.setText("0");
                    Log.e("FriendCount", "Failed to load friend count", e);
                });
    }
}