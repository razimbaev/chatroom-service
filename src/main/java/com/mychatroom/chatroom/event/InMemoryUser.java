package com.mychatroom.chatroom.event;

import java.util.*;

public class InMemoryUser {
    private String userSessionId, username;
    private Map<String, ChatroomSubscription> socketSessionToChatroomMap;

    public InMemoryUser(String sessionId) {
        this.userSessionId = sessionId;
        this.username = "";
        this.socketSessionToChatroomMap = new HashMap<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserSessionId() {
        return userSessionId;
    }

    public void setUserSessionId(String userSessionId) {
        this.userSessionId = userSessionId;
    }

    public void addSocketSessionIfNotExists(String socketSessionId) {
        if (!this.socketSessionToChatroomMap.containsKey(socketSessionId)) {
            this.socketSessionToChatroomMap.put(socketSessionId, null);
        }
    }

    public void updateSocketChatroom(String socketSessionId, String chatroom, String subscriptionId) {
        this.socketSessionToChatroomMap.put(socketSessionId, new ChatroomSubscription(chatroom, subscriptionId));
    }

    public String[] getChatroomSubscriptionFromSocketSession(String socketSessionId) {
        if (!this.socketSessionToChatroomMap.containsKey(socketSessionId))
            return null;
        ChatroomSubscription chatSub = this.socketSessionToChatroomMap.get(socketSessionId);
        if (chatSub == null)
            return null;
        return new String[]{chatSub.chatroom, chatSub.subscriptionId};
    }

    public void removeSocketSession(String socketSessionId) {
        this.socketSessionToChatroomMap.remove(socketSessionId);
    }

    public boolean isChatroomInSocketSession(String chatroom) {
        for (ChatroomSubscription socketChatroom : this.socketSessionToChatroomMap.values()) {
            if (chatroom.equals(socketChatroom.chatroom))
                return true;
        }
        return false;
    }

    public Collection<String> getChatrooms() {
        List<String> chatrooms = new ArrayList<>();
        for (ChatroomSubscription chatroomSub : this.socketSessionToChatroomMap.values()) {
            chatrooms.add(chatroomSub.chatroom);
        }
        return chatrooms;
    }

    // TODO - check to make sure this equals and hashcode method is sufficient
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InMemoryUser that = (InMemoryUser) o;
        return userSessionId.equals(that.userSessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userSessionId);
    }

    private static class ChatroomSubscription {
        private String chatroom, subscriptionId;

        public ChatroomSubscription(String chatroom, String subscriptionId) {
            this.chatroom = chatroom;
            this.subscriptionId = subscriptionId;
        }
    }
}
