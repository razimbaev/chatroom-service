package com.mychatroom.chatroom.dto;

public class UserHomeDTO {
    private String chatroom;
    private int numUsers;

    public UserHomeDTO(String chatroom, int numUsers) {
        this.chatroom = chatroom;
        this.numUsers = numUsers;
    }

    public String getChatroom() {
        return chatroom;
    }

    public long getNumUsers() {
        return numUsers;
    }
}
