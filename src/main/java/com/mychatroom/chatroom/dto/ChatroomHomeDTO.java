package com.mychatroom.chatroom.dto;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ChatroomHomeDTO {
    private String chatroom;
    private int numUsers;
    private List<MessageDTO> mostRecentMessages;

    public ChatroomHomeDTO(String chatroom) {
        this.chatroom = chatroom;
        this.numUsers = 0;
        this.mostRecentMessages = new ArrayList<>();
    }

    public ChatroomHomeDTO(String chatroom, int numUsers, List<MessageDTO> mostRecentMessages) {
        this.chatroom = chatroom;
        this.numUsers = numUsers;
        this.mostRecentMessages = getMostRecent(mostRecentMessages, 3);
    }

    private List<MessageDTO> getMostRecent(List<MessageDTO> mostRecentMessages, int numToSend) {
        if (mostRecentMessages == null || mostRecentMessages.isEmpty()) {
            return new ArrayList<>();
        }

        List<MessageDTO> messagesToSend = new LinkedList<>();

        int size = mostRecentMessages.size();
        int numWillSend = Math.min(numToSend, size);

        for (int x = 1; x <= numWillSend; x++) {
            messagesToSend.add(0, mostRecentMessages.get(size - x));
        }

        return messagesToSend;
    }

    public String getChatroom() {
        return chatroom;
    }

    public int getNumUsers() {
        return numUsers;
    }

    public List<MessageDTO> getMostRecentMessages() {
        return mostRecentMessages;
    }
}
