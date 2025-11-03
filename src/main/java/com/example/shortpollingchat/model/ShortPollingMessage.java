package com.example.shortpollingchat.model;

public class ShortPollingMessage {
    private String username;
    private String text;
    private long timestamp;

    public ShortPollingMessage() {}

    public ShortPollingMessage(String username, String text, long timestamp) {
        this.username = username;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
