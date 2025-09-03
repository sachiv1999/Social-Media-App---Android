package Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.socialmedia.ChatPage;
import com.example.socialmedia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

import Models.user;
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<user> userList;
    private FirebaseFirestore db;
    private String currentUid;

    public UserAdapter(Context context, List<user> userList) {
        this.context = context;
        this.userList = userList;
        this.db = FirebaseFirestore.getInstance();
        this.currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        user model = userList.get(position);

        holder.txtName.setText(model.getName());

        if (model.getProfileImage() != null && !model.getProfileImage().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(model.getProfileImage(), Base64.DEFAULT);
                Glide.with(context).asBitmap().load(decodedString).into(holder.userProfileImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        holder.btnAddFriend.setVisibility(View.GONE);
        holder.btnAccept.setVisibility(View.GONE);
        holder.btnChat.setVisibility(View.GONE);

        if (!model.isFriend() && model.getRequestStatus() == null) {
            holder.btnAddFriend.setVisibility(View.VISIBLE);
        }

        holder.btnAddFriend.setOnClickListener(v -> {
            Map<String, Object> request = new HashMap<>();
            request.put("from", currentUid);
            request.put("to", model.getUid());
            request.put("status", "sent");
            request.put("timestamp", FieldValue.serverTimestamp());

            db.collection("FriendRequests")
                    .add(request)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(context, "Friend request sent", Toast.LENGTH_SHORT).show();
                        holder.btnAddFriend.setVisibility(View.GONE);
                    });
        });

        holder.btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatPage.class);
            intent.putExtra("uid", model.getUid());
            intent.putExtra("name", model.getName());
            intent.putExtra("profilePic", model.getProfileImage());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        ImageView userProfileImage;
        TextView txtName;
        Button btnAddFriend, btnAccept, btnChat;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            txtName = itemView.findViewById(R.id.txtName);
            btnAddFriend = itemView.findViewById(R.id.btnAddFriend);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnChat = itemView.findViewById(R.id.btnChat);
        }
    }
}