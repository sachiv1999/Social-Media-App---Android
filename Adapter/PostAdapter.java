package Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateUtils;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.socialmedia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Models.post;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<post> postList;
    private boolean isUserProfile;
    private final Map<String, String> userCache = new HashMap<>();

    public PostAdapter(Context context, List<post> postList, boolean isUserProfile) {
        this.context = context;
        this.postList = postList;
        this.isUserProfile = isUserProfile;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, postTime, likeCount, tvUsername;
        ImageView imgPost;
        ImageButton btnLike, btnDelete;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            imgPost = itemView.findViewById(R.id.imgPost);
            postTime = itemView.findViewById(R.id.postTime);
            likeCount = itemView.findViewById(R.id.likeCount);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        post currentPost = postList.get(position);
        String userId = currentPost.getUserId();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference postRef = db.collection("Posts").document(currentPost.getPostId());

        // Set post content
        holder.tvContent.setText(currentPost.getContent());

        // Load username from cache or fetch
        if (userId != null) {
            if (userCache.containsKey(userId)) {
                holder.tvUsername.setText("Posted by " + userCache.get(userId));
            } else {
                db.collection("Users").document(userId).get()
                        .addOnSuccessListener(snapshot -> {
                            if (snapshot.exists()) {
                                String name = snapshot.getString("name");
                                if (name != null && holder.getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                                    userCache.put(userId, name);
                                    holder.tvUsername.setText("Posted by " + name);
                                } else {
                                    holder.tvUsername.setText("Posted by Unknown");
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (holder.getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                                holder.tvUsername.setText("Posted by Unknown");
                            }
                        });
            }
        } else {
            holder.tvUsername.setText("Posted by Unknown");
        }

        // Timestamp
        long timeInMillis = currentPost.getTimestamp();
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                timeInMillis,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        );
        holder.postTime.setText(timeAgo);

        // Image rendering
        if (currentPost.getImageBase64() != null && !currentPost.getImageBase64().isEmpty()) {
            byte[] decodedBytes = Base64.decode(currentPost.getImageBase64(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            holder.imgPost.setImageBitmap(bitmap);
            holder.imgPost.setVisibility(View.VISIBLE);
        } else {
            holder.imgPost.setVisibility(View.GONE);
        }

        // Likes UI
        List<String> likedBy = currentPost.getLikedBy();
        if (likedBy != null && likedBy.contains(currentUserId)) {
            holder.btnLike.setImageResource(R.drawable.fh);
        } else {
            holder.btnLike.setImageResource(R.drawable.liked);
        }

        holder.likeCount.setText(currentPost.getLikeCount() + " Likes");

        // Like button logic
        holder.btnLike.setOnClickListener(v -> {
            List<String> updatedLikedBy = currentPost.getLikedBy();
            if (updatedLikedBy == null) updatedLikedBy = new ArrayList<>();

            if (updatedLikedBy.contains(currentUserId)) {
                updatedLikedBy.remove(currentUserId);
                holder.btnLike.setImageResource(R.drawable.fh);
            } else {
                updatedLikedBy.add(currentUserId);
                holder.btnLike.setImageResource(R.drawable.liked);
            }

            currentPost.setLikedBy(updatedLikedBy);
            currentPost.setLikeCount(updatedLikedBy.size());

            postRef.update("likedBy", updatedLikedBy);
            postRef.update("likeCount", updatedLikedBy.size());
            holder.likeCount.setText(updatedLikedBy.size() + " Likes");
        });

        if (isUserProfile) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                db.collection("Posts").document(currentPost.getPostId())
                        .delete()
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                            postList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, postList.size());
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show();
                        });
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
}