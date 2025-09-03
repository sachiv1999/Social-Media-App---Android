package com.example.socialmedia;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Adapter.FriendAdapter;
import Models.user;

public class FriendList extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FriendAdapter friendAdapter;
    private List<user> userList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private ImageView btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        recyclerView = findViewById(R.id.recyclerViewFriends);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setImageResource(R.drawable.back_arrow);
        btnBack.setOnClickListener(v -> finish());

        userList = new ArrayList<>();
        friendAdapter = new FriendAdapter(this, userList);
        recyclerView.setAdapter(friendAdapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        loadAllUsersToAddFriend();
    }

    public void loadAllUsersToAddFriend() {
        Map<String, String> requestStatusMap = new HashMap<>();

        // Step 1: Get all friend requests sent by the current user
        db.collection("FriendRequests")
                .whereEqualTo("from", currentUserId)
                .get()
                .addOnSuccessListener(sentRequests -> {
                    for (DocumentSnapshot doc : sentRequests.getDocuments()) {
                        String to = doc.getString("to");
                        requestStatusMap.put(to, "pending_sent");
                    }

                    // Step 2: Get all friend requests received by the current user
                    db.collection("FriendRequests")
                            .whereEqualTo("to", currentUserId)
                            .get()
                            .addOnSuccessListener(receivedRequests -> {
                                for (DocumentSnapshot doc : receivedRequests.getDocuments()) {
                                    String from = doc.getString("from");
                                    requestStatusMap.put(from, "pending_received");
                                }

                                // Step 3: Get all existing friends
                                db.collection("Friends")
                                        .document(currentUserId)
                                        .collection("list")
                                        .get()
                                        .addOnSuccessListener(friendSnapshot -> {
                                            List<String> friendUids = new ArrayList<>();
                                            for (DocumentSnapshot friendDoc : friendSnapshot) {
                                                friendUids.add(friendDoc.getId());
                                            }

                                            // Step 4: Get all users
                                            db.collection("Users")
                                                    .get()
                                                    .addOnSuccessListener(users -> {
                                                        userList.clear();
                                                        for (DocumentSnapshot userDoc : users.getDocuments()) {
                                                            String uid = userDoc.getId();
                                                            if (!uid.equals(currentUserId)) {
                                                                String name = userDoc.getString("name");
                                                                String profileImage = userDoc.getString("profileImage");

                                                                // Check if the user is already a friend
                                                                boolean isFriend = friendUids.contains(uid);

                                                                // Get the request status from the map
                                                                String requestStatus = requestStatusMap.getOrDefault(uid, "none");

                                                                // This is the key change: We populate the list with all users
                                                                // and let the adapter decide what to show based on the flags.
                                                                user u = new user(uid, name, profileImage, isFriend, requestStatus, null);
                                                                userList.add(u);
                                                            }
                                                        }
                                                        friendAdapter.notifyDataSetChanged();
                                                    });
                                        });
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}