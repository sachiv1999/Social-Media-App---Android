package com.example.socialmedia;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

import Adapter.RequestAdapter;

public class FriendRequest extends AppCompatActivity {

    private RecyclerView recyclerRequest;
    private TextView txtEmptyRequest;
    private List<DocumentSnapshot> requestList;
    private RequestAdapter adapter;
    private FirebaseFirestore db;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        recyclerRequest = findViewById(R.id.recyclerRequest);
        txtEmptyRequest = findViewById(R.id.txtEmptyRequest);

        recyclerRequest.setLayoutManager(new LinearLayoutManager(this));
        requestList = new ArrayList<>();
        adapter = new RequestAdapter(this, requestList);
        recyclerRequest.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadFriendRequests();
    }

    private void loadFriendRequests() {
        db.collection("FriendRequests")
                .whereEqualTo("to", currentUid)
                .whereEqualTo("status", "pending")  // optional filter
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    requestList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        txtEmptyRequest.setVisibility(View.VISIBLE);
                        recyclerRequest.setVisibility(View.GONE);
                    } else {
                        txtEmptyRequest.setVisibility(View.GONE);
                        recyclerRequest.setVisibility(View.VISIBLE);
                        requestList.addAll(queryDocumentSnapshots.getDocuments());
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load requests", Toast.LENGTH_SHORT).show();
                });
    }
}
