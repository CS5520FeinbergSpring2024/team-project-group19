package edu.northeastern.echolist;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userId;
    private String password;
    private String profileImageUrl;
    private String profileImageData; // Add this field for base64-encoded image data
    private List<String> friends; // List of friends
    private List<String> favorites;

    public User() {

    }

    public User(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }
    public User(String userId, String password, String profileImageUrl) {
        this.userId = userId;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
    }

    public User(String userId, String password, String profileImageUrl, List<String> favorites) {
        this.userId = userId;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
        this.favorites = favorites;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getUserId() {
        return userId;
    }
    public String getPassword() {
        return password;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getProfileImageData() {
        return profileImageData;
    }

    public void setProfileImageData(String profileImageData) {
        this.profileImageData = profileImageData;
    }

    // Methods for adding and delete friends.
    public void addFriend(String friendUserId) {
        if (friends == null) {
            friends = new ArrayList<>();
        }
        if (!friends.contains(friendUserId)) {
            friends.add(friendUserId);
        }
    }

    public List<String> getFriends() {
        return friends;
    }
    public List<String> getFavorites() { return favorites; }

    public void setFavorites(List<String> favorites) {
        this.favorites = favorites;
    }

    public void removeFriend(String friendUserId) {
        if (friends != null) {
            friends.remove(friendUserId);
        }
    }
}
