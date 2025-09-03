package com.example.socialmedia;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

import Adapter.PostAdapter;
import Models.post;

public class HomePage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<post> postList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration postListener;
    private BottomNavigationView bottomNav;
    private ImageButton menuBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList,false);
        recyclerView.setAdapter(postAdapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        menuBtn = findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(HomePage.this, menuBtn);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.top_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_settings) {
                    startActivity(new Intent(this, SettingsPage.class));
                    return true;
                } else if (id == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginPage.class));
                    finish();
                    return true;
                } else if (id == R.id.menu_about) {
                    startActivity(new Intent(this, AboutPage.class));
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        loadAllPublicPosts();

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent;
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_profile) {

                startActivity(new Intent(this, UserProfile.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_search) {
                intent = new Intent(this, addsActivity.class);
                intent.putExtra("target", "search");
                startActivity(intent);
//                startActivity(new Intent(this, FriendList.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void loadAllPublicPosts() {
        postListener = db.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (value != null) {
                        postList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            post p = doc.toObject(post.class);
                            if (p != null) {
                                postList.add(p);
                            }
                        }
                        postAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (postListener != null) {
            postListener.remove();
        }
    }
}
