package Adapter;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.socialmedia.R;
import java.util.List;
import Models.ChatMessage;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatList;
    private final String currentUserId;

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatList, String currentUserId) {
        this.chatList = chatList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        return chatList.get(position).getSenderId().equals(currentUserId)
                ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recived, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = chatList.get(position);
        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).sentText.setText(msg.getMessage());
        } else {
            ((ReceivedViewHolder) holder).receivedText.setText(msg.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView sentText;

        SentViewHolder(View itemView) {
            super(itemView);
            sentText = itemView.findViewById(R.id.sentText);
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView receivedText;

        ReceivedViewHolder(View itemView) {
            super(itemView);
            receivedText = itemView.findViewById(R.id.receivedText);
        }
    }
}