package Models;

import com.google.firebase.Timestamp;

public class ChatMessage {
    private String senderId;
    private String message;
    private String chatRoomId;
    private Timestamp timestamp;

    public ChatMessage() {}

    public ChatMessage(String senderId, String message, String chatRoomId, Timestamp timestamp) {
        this.senderId = senderId;
        this.message = message;
        this.chatRoomId = chatRoomId;
        this.timestamp = timestamp;
    }

    public String getSenderId() { return senderId; }
    public String getMessage() { return message; }
    public String getChatRoomId() { return chatRoomId; }
    public Timestamp getTimestamp() { return timestamp; }
}