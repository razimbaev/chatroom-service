package com.mychatroom.chatroom.dto;

public class MessageHomeDTO extends MessageDTO {
    private String content;
    private String userId;
    private String chatroom;

    public MessageHomeDTO(MessageDTO message, String chatroom) {
        this.content = message.getContent();
        this.userId = message.getUserId();
        this.chatroom = chatroom;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    public String getChatroom() {
        return chatroom;
    }
}
