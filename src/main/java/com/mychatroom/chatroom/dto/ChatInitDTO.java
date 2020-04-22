package com.mychatroom.chatroom.dto;

import java.util.List;

public class ChatInitDTO {
    private List<MessageDTO> messages;
    private List<String> usernames;
    private String myUsername;
    private long nextUsernameChangeAllowed;

    public List<MessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageDTO> messages) {
        this.messages = messages;
    }

    public List<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }

    public String getMyUsername() {
        return myUsername;
    }

    public void setMyUsername(String myUsername) {
        this.myUsername = myUsername;
    }

    public long getNextUsernameChangeAllowed() {
        return nextUsernameChangeAllowed;
    }

    public void setNextUsernameChangeAllowed(long nextUsernameChangeAllowed) {
        this.nextUsernameChangeAllowed = nextUsernameChangeAllowed;
    }
}
