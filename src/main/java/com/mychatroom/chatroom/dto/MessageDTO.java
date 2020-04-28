package com.mychatroom.chatroom.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MessageDTO {
    private String content;
    private String userId;
    private long timestamp;

    @JsonIgnore
    private String userSessionId;

    public MessageDTO() {
    }

    public MessageDTO(String content, String userId) {
        this.content = content;
        this.userId = userId;
    }

    public void setContent(String content) {
        this.content = content;
    }

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

    public String getUserSessionId() {
        return userSessionId;
    }

    public void setUserSessionId(String userSessionId) {
        this.userSessionId = userSessionId;
    }
}
