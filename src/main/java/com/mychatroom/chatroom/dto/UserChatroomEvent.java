package com.mychatroom.chatroom.dto;

public enum UserChatroomEvent {
    JOIN("JOIN"), LEAVE("LEAVE"), CHANGE_USERNAME("CHANGE_USERNAME");

    private String type;

    UserChatroomEvent(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
