package com.mychatroom.chatroom.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mychatroom.chatroom.event.InMemoryUser;

public class MessageDTO {
    private String content;
    private InMemoryUser user;
    private long timestamp;

    public MessageDTO() {
    }

    public MessageDTO(String content) {
        this.content = content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public String getUserId() {
        if (this.user == null)
            return "";
        return this.user.getUsername();
    }

    @JsonIgnore
    public String getUserSessionId() {
        if (this.user == null)
            return "";
        return user.getUserSessionId();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setUser(InMemoryUser user) {
        this.user = user;
    }
}
