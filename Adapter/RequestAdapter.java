package Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmedia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.List;
import java.util.Map;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private final Context context;
    private final List<DocumentSnapshot> requestList;

    public RequestAdapter(Context context, List<DocumentSnapshot> requestList) {
        this.context = context;
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public RequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.ViewHolder holder, int position) {
        DocumentSnapshot doc = requestList.get(position);

        String fromUid = doc.getString("from");
        String docId = doc.getId();

        // Fetch sender's full profile info
        FirebaseFirestore.getInstance().collection("Users").document(fromUid)
                .get().addOnSuccessListener(userDoc -> {
                    String name = userDoc.getString("name");
                    String profileImage = userDoc.getString("profileImage");

                    holder.textViewName.setText(name != null ? name : "Unknown");

                    if (profileImage != null && !profileImage.isEmpty()) {
                        byte[] decodedBytes = Base64.decode(profileImage, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        holder.imageViewProfile.setImageBitmap(bitmap);
                    }
                });

        holder.btnAccept.setOnClickListener(v -> {
            String currentUid = FirebaseAuth.getInstance().getUid();
            if (currentUid == null) return;

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Map<String, Object> friendMap1 = Map.of("uid", fromUid);
            Map<String, Object> friendMap2 = Map.of("uid", currentUid);

            db.collection("Friends").document(currentUid)
                    .collection("list").document(fromUid)
                    .set(friendMap1);

            db.collection("Friends").document(fromUid)
                    .collection("list").document(currentUid)
                    .set(friendMap2);

            // Delete friend request
            db.collection("FriendRequests").document(docId).delete();

            Toast.makeText(context, "Friend request accepted", Toast.LENGTH_SHORT).show();
        });

        holder.btnReject.setOnClickListener(v -> {
            FirebaseFirestore.getInstance().collection("FriendRequests")
                    .document(docId).delete();

            Toast.makeText(context, "Friend request rejected", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        ImageView imageViewProfile;
        Button btnAccept, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnDecline);
        }
    }
}
