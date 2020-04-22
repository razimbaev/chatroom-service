package com.mychatroom.chatroom.dto;

public class MessageDTO {
    private String content;
    private String userId;
    private long timestamp;

    public String getContent() {
        return content;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
