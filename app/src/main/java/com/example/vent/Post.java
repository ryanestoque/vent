package com.example.vent;

import java.util.ArrayList;
import java.util.List;

public class Post {
    private String anonUsername;
    private String content;
    private int feelCount;
    private long timestamp; // Unix timestamp in milliseconds
    private String uid;
    private String id;

    private List<String> feelUsers; // <-- ADD THIS

    public Post() {}

    public Post(String anonUsername, String content, int feelCount, long timestamp, String uid) {
        this.anonUsername = anonUsername;
        this.content = content;
        this.feelCount = feelCount;
        this.timestamp = timestamp;
        this.uid = uid;
        this.feelUsers = new ArrayList<>(); // Initialize by default
    }

    public List<String> getFeelUsers() {
        return feelUsers;
    }

    public void setFeelUsers(List<String> feelUsers) {
        this.feelUsers = feelUsers;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
    public String getAnonUsername() { return anonUsername; }
    public String getContent() { return content; }
    public int getFeelCount() { return feelCount; }
    public void setFeelCount(int feelCount) { this.feelCount = feelCount; }
    public long getTimestamp() { return timestamp; }
    public String getUid() { return uid; }
}
