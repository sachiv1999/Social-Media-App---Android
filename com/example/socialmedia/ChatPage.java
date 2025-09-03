// ✅ ChatPage.java (uses flat chatRoomId, user profile image and name loading, message sending)
package com.example.socialmedia;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

import Adapter.ChatAdapter;
import Models.ChatMessage;

public class ChatPage extends AppCompatActivity {

    private String receiverId;
    private String senderId;
    private FirebaseFirestore db;
    private EditText messageInput;
    private ImageButton sendBtn;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList = new ArrayList<>();

    private TextView chatUserName;
    private ImageView chatUserProfileImage;

    private String getChatRoomId(String uid1, String uid2) {
        return uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);

        receiverId = getIntent().getStringExtra("receiverId");
        senderId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (receiverId == null || senderId == null) {
            Toast.makeText(this, "Chat setup failed.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        messageInput = findViewById(R.id.messageInput);
        sendBtn = findViewById(R.id.sendBtn);
        recyclerView = findViewById(R.id.chatRecyclerView);
        chatUserName = findViewById(R.id.chatUserName);
        chatUserProfileImage = findViewById(R.id.chatUserProfileImage);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(messageList, senderId);
        recyclerView.setAdapter(chatAdapter);

        db.collection("Users").document(receiverId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        chatUserName.setText(doc.getString("name"));

                        String profileImageBase64 = doc.getString("profileImage");
                        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                            try {
                                byte[] imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                chatUserProfileImage.setImageBitmap(bitmap);
                            } catch (Exception e) {
                                chatUserProfileImage.setImageResource(R.drawable.profile);
                            }
                        } else {
                            chatUserProfileImage.setImageResource(R.drawable.profile);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    chatUserName.setText("User");
                    chatUserProfileImage.setImageResource(R.drawable.profile);
                });

        String chatRoomId = getChatRoomId(senderId, receiverId);

        db.collection("ChatRooms")
                .document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load messages.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        messageList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            ChatMessage msg = doc.toObject(ChatMessage.class);
                            if (msg != null) messageList.add(msg);
                        }
                        chatAdapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    }
                });

        sendBtn.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) {
                sendBtn.setEnabled(false);
                sendMessage(chatRoomId, message);
            }
        });
    }

    private void sendMessage(String chatRoomId, String message) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("chatRoomId", chatRoomId);
        msg.put("senderId", senderId);
        msg.put("message", message);
        msg.put("timestamp", FieldValue.serverTimestamp());

        db.collection("ChatRooms")
                .document(chatRoomId)
                .collection("messages")
                .add(msg)
                .addOnSuccessListener(doc -> {
                    messageInput.setText("");
                    sendBtn.setEnabled(true);
                    recyclerView.scrollToPosition(messageList.size() - 1);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Message failed to send.", Toast.LENGTH_SHORT).show();
                    sendBtn.setEnabled(true);
                });

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("chatRoomId", chatRoomId);
        metadata.put("senderId", senderId);
        metadata.put("receiverId", receiverId);
        metadata.put("timestamp", FieldValue.serverTimestamp());

        db.collection("ChatRooms")
                .document(chatRoomId)
                .set(metadata, SetOptions.merge());
    }
}