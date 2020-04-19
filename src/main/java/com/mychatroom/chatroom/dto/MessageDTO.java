package com.mychatroom.chatroom.dto;

public class MessageDTO {
    private String content;
    private String userId;
    private long timestamp;

    public String getContent() {
        return content;
    }

    public String getUserId() {
        return userId;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
