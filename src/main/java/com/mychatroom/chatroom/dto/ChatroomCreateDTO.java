package com.mychatroom.chatroom.dto;

public class ChatroomCreateDTO {
    private String reasonCreateNotAllowed;

    public ChatroomCreateDTO() {
        this.reasonCreateNotAllowed = "";
    }

    public ChatroomCreateDTO(String reasonCreateNotAllowed) {
        this.reasonCreateNotAllowed = reasonCreateNotAllowed;
    }

    public String getReasonCreateNotAllowed() {
        return reasonCreateNotAllowed;
    }
}
