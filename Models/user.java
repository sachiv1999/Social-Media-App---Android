package Models;

public class user {
    private String uid;
    private String name;
    private String profileImage;
    private boolean isFriend;
    private String requestStatus;
    private String requestDocId;
    private long timestamp;

    public user() {
    }

    // Constructor for full usage
    public user(String uid, String name, String profileImage, boolean isFriend, String requestStatus, String requestDocId) {
        this.uid = uid;
        this.name = name;
        this.profileImage = profileImage;
        this.isFriend = isFriend;
        this.requestStatus = requestStatus;
        this.requestDocId = requestDocId;
    }

    public user(String uid, String name, String profileImage, long timestamp) {
        this.uid = uid;
        this.name = name;
        this.profileImage = profileImage;
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public String getRequestDocId() {
        return requestDocId;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }
    public void setRequestDocId(String requestDocId) {
        this.requestDocId = requestDocId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
