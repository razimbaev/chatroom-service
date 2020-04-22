package com.mychatroom.chatroom.dto;

public class UserChatroomUpdateDTO {
    private String previousName, currentName;
    private UserChatroomEvent event;

    public UserChatroomUpdateDTO(String previousName, String currentName) {
        this.previousName = previousName;
        this.currentName = currentName;
        this.event = UserChatroomEvent.CHANGE_USERNAME;
    }

    public UserChatroomUpdateDTO(String currentName, UserChatroomEvent event) {
        this.currentName = currentName;
        this.event = event;
    }

    public String getPreviousName() {
        return previousName;
    }

    public String getCurrentName() {
        return currentName;
    }

    public UserChatroomEvent getEvent() {
        return event;
    }
}
