package com.mychatroom.chatroom.dto;

public class UsernameChangeDTO {
    private String previousName, newName, reasonChangeNotAllowed;
    private long timeNextChangeAllowed;

    public UsernameChangeDTO(String previousName, String newName, String reasonChangeNotAllowed, long timeNextChangeAllowed) {
        this.previousName = previousName;
        this.newName = newName;
        this.reasonChangeNotAllowed = reasonChangeNotAllowed;
        this.timeNextChangeAllowed = timeNextChangeAllowed;
    }

    public String getPreviousName() {
        return previousName;
    }

    public String getNewName() {
        return newName;
    }

    public String getReasonChangeNotAllowed() {
        return reasonChangeNotAllowed;
    }

    public long getTimeNextChangeAllowed() {
        return timeNextChangeAllowed;
    }
}
