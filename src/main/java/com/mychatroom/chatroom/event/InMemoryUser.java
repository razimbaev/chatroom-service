package com.mychatroom.chatroom.event;

import java.util.*;

public class InMemoryUser {
    private static long TIME_UNTIL_USERNAME_CHANGE_ALLOWED = 24 * 60 * 60 * 1000;

    private String userSessionId, username;
    private Map<String, ChatroomSubscription> socketSessionToChatroomMap;
    private long lastTimeChangedUsername;
    private Set<String> myChatrooms;

    public InMemoryUser(String sessionId) {
        this.userSessionId = sessionId;
        this.username = "";
        this.socketSessionToChatroomMap = new HashMap<>();
        this.lastTimeChangedUsername = 0;
        this.myChatrooms = new LinkedHashSet<>() {
            // maintains a LRU order
            @Override
            public boolean add(String e) {
                boolean wasThere = remove(e);
                super.add(e);
                return !wasThere;
            }
        };
    }

    public String getUsername() {
        return username;
    }

    public boolean setUsername(String username) {
        if (!this.isUsernameChangedMoreThanDayAgo()) {
            return false;
        }
        this.username = username;
        this.lastTimeChangedUsername = System.currentTimeMillis();
        return true;
    }

    private boolean isUsernameChangedMoreThanDayAgo() {
        long currentTimeInMillis = System.currentTimeMillis();
        return currentTimeInMillis > this.getTimeWhenUsernameChangeAllowed();
    }

    public long getTimeWhenUsernameChangeAllowed() {
        if (this.lastTimeChangedUsername == 0)
            return 0;
        return this.lastTimeChangedUsername + TIME_UNTIL_USERNAME_CHANGE_ALLOWED;
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
            if (socketChatroom != null && chatroom.equals(socketChatroom.chatroom))
                return true;
        }
        return false;
    }

    public Collection<String> getChatrooms() {
        List<String> chatrooms = new ArrayList<>();
        for (ChatroomSubscription chatroomSub : this.socketSessionToChatroomMap.values()) {
            if (chatroomSub != null)
                chatrooms.add(chatroomSub.chatroom);
        }
        return chatrooms;
    }

    public void addToMyChatrooms(String chatroom) {
        this.myChatrooms.add(chatroom);
    }

    public Set<String> getMyChatrooms() {
        return myChatrooms;
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
