package com.mychatroom.chatroom.event;

import com.mychatroom.chatroom.dto.MessageDTO;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MockDB {

    // TODO - for now assuming that sessionId is unique identifier for single user (as opposed to socket connection since user can have two tabs open)
    private Set<String> takenUsernames = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private Map<String, InMemoryUser> sessionToUserMap = new HashMap<>();
    private Map<String, Set<InMemoryUser>> roomToUsersMap = new HashMap<>();
    private Map<String, List<MessageDTO>> roomToMessageMap = new HashMap<>();

    public MockDB() {
        // temporarily hardcoded values
        // TODO - LOAD FROM DB
        String[] rooms = new String[]{"Corona", "France", "Swimming", "Economics"};

        for (String room : rooms) {
            roomToUsersMap.put(room, new HashSet<>());
            roomToMessageMap.put(room, new ArrayList<>());
        }
    }

    public void changeUsername(String previousName, String newName) {
        this.takenUsernames.remove(previousName);
        this.takenUsernames.add(newName);
    }

    public boolean isUsernameTaken(String username) {
        return this.takenUsernames.contains(username);
    }

    public InMemoryUser getInMemoryUser(String sessionId, boolean createIfNotExists) {
        InMemoryUser user = this.sessionToUserMap.getOrDefault(sessionId, null);
        if (!createIfNotExists || user != null)
            return user;

        // TODO - SAVE TO DB
        user = new InMemoryUser(sessionId);
        this.sessionToUserMap.put(sessionId, user);
        return user;
    }

    public InMemoryUser getInMemoryUser(String sessionId) {
        return this.getInMemoryUser(sessionId, false);
    }

    public Set<String> getChatrooms() {
        return this.roomToUsersMap.keySet();
    }

    public void createNewChatroom(String chatroom) {
        // TODO - SAVE TO DB
        this.roomToUsersMap.put(chatroom, new HashSet<>());
        this.roomToMessageMap.put(chatroom, new ArrayList<>());
    }

    public int getNumUsersInChatroom(String chatroom) {
        return this.roomToUsersMap.getOrDefault(chatroom, new HashSet<>()).size();
    }

    public Set<InMemoryUser> getUsersInChatroom(String chatroom) {
        return this.roomToUsersMap.getOrDefault(chatroom, null);
    }

    public boolean isExistingChatroom(String chatroom) {
        return this.roomToUsersMap.containsKey(chatroom);
    }

    public void addUserToChatroom(InMemoryUser user, String chatroom) {
        // TODO - SAVE TO DB
        if (this.isExistingChatroom(chatroom))
            this.roomToUsersMap.get(chatroom).add(user);
    }

    public boolean removeUserFromChatroom(InMemoryUser user, String chatroom) {
        // TODO - SAVE TO DB
        if (this.isExistingChatroom(chatroom))
            return this.roomToUsersMap.get(chatroom).remove(user);
        return false;
    }

    public List<MessageDTO> getMessages(String chatroom) {
        return this.roomToMessageMap.getOrDefault(chatroom, new ArrayList<>());
    }

    public void addMessage(MessageDTO message, String chatroom) {
        // TODO - SAVE TO DB
        if (this.isExistingChatroom(chatroom)) {
            this.roomToMessageMap.get(chatroom).add(message);
        }
    }
}
