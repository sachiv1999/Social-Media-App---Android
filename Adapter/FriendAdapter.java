package Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.socialmedia.Chat;
import com.example.socialmedia.FriendList;
import com.example.socialmedia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Models.user;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    private Context context;
    private List<user> userList;
    private FirebaseFirestore db;
    private String currentUid;

    public FriendAdapter(Context context, List<user> userList) {
        this.context = context;
        this.userList = userList;
        this.db = FirebaseFirestore.getInstance();
        this.currentUid = FirebaseAuth.getInstance().getUid();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView friendName;
        ImageView friendImage;
        Button btnAdd, btnAccept, btnChat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            friendName = itemView.findViewById(R.id.friendName);
            friendImage = itemView.findViewById(R.id.friendImage);
            btnAdd = itemView.findViewById(R.id.btnAddFriend);
            btnAccept = itemView.findViewById(R.id.btnAcceptRequest);
            btnChat = itemView.findViewById(R.id.btnChat);
        }
    }

    @NonNull
    @Override
    public FriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendAdapter.ViewHolder holder, int position) {
        user userModel = userList.get(position);

        holder.friendName.setText(userModel.getName());
        setProfileImage(holder.friendImage, userModel.getProfileImage());

        // Logic to show/hide buttons based on friendship status
        String requestStatus = userModel.getRequestStatus();
        boolean isFriend = userModel.isFriend();

        // Always hide all buttons first
        holder.btnAdd.setVisibility(View.GONE);
        holder.btnAccept.setVisibility(View.GONE);
        holder.btnChat.setVisibility(View.GONE);

        // Check the most definitive state first: are they friends?
        if (isFriend) {
            holder.btnChat.setVisibility(View.VISIBLE);
        } else if ("pending_sent".equalsIgnoreCase(requestStatus)) {
            // Current user sent the request
            holder.btnAdd.setVisibility(View.VISIBLE);
            holder.btnAdd.setText("Pending");
            holder.btnAdd.setEnabled(false);
        } else if ("pending_received".equalsIgnoreCase(requestStatus)) {
            // Current user received the request, show accept button
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnAccept.setEnabled(true);
        } else {
            // No request sent or received, show add friend button
            holder.btnAdd.setVisibility(View.VISIBLE);
            holder.btnAdd.setText("Add Friend");
            holder.btnAdd.setEnabled(true);
        }

        // Set click listeners
        holder.btnAdd.setOnClickListener(v -> sendFriendRequest(userModel, holder));
        holder.btnAccept.setOnClickListener(v -> acceptFriendRequest(userModel, holder));
        holder.btnChat.setOnClickListener(v -> startChat(userModel));
    }

    private void setProfileImage(ImageView imageView, String base64Image) {
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(decodedImage);
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.profile);
            }
        } else {
            imageView.setImageResource(R.drawable.profile);
        }
    }

    private void sendFriendRequest(user userModel, ViewHolder holder) {
        if (currentUid == null || userModel.getUid() == null) return;

        holder.btnAdd.setEnabled(false);
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("from", currentUid);
        requestMap.put("to", userModel.getUid());
        requestMap.put("status", "pending");
        requestMap.put("timestamp", System.currentTimeMillis());

        db.collection("FriendRequests")
                .add(requestMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Friend request sent", Toast.LENGTH_SHORT).show();

                    // Crucially, update the UI. The user's status should change from 'none' to 'pending_sent'.
                    userModel.setRequestStatus("pending_sent");
                    notifyItemChanged(userList.indexOf(userModel));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    holder.btnAdd.setEnabled(true);
                });
    }

    private void acceptFriendRequest(user userModel, ViewHolder holder) {
        db.collection("FriendRequests")
                .whereEqualTo("from", userModel.getUid())
                .whereEqualTo("to", currentUid)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot requestDoc = queryDocumentSnapshots.getDocuments().get(0);

                        addFriendship(currentUid, userModel.getUid());
                        addFriendship(userModel.getUid(), currentUid);

                        requestDoc.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Friend request accepted!", Toast.LENGTH_SHORT).show();

                                    // Update the UI state of the adapter. This user is now a friend.
                                    userModel.setFriend(true);
                                    notifyItemChanged(userList.indexOf(userModel));

                                    // Reload the main list to ensure the other user's list is also updated (if they're on a separate device).
                                    if (context instanceof FriendList) {
                                        ((FriendList) context).loadAllUsersToAddFriend();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Error accepting request", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void addFriendship(String userId1, String userId2) {
        db.collection("Friends").document(userId1)
                .collection("list").document(userId2)
                .set(new HashMap<>())
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to add friendship", Toast.LENGTH_SHORT).show();
                });
    }

    private void startChat(user userModel) {
        Intent intent = new Intent(context, Chat.class);
        intent.putExtra("friendUid", userModel.getUid());
        intent.putExtra("friendName", userModel.getName());
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}