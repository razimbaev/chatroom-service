package com.mychatroom.chatroom.event;

import com.mychatroom.chatroom.dto.MessageDTO;
import com.mychatroom.chatroom.service.ChatroomService;
import com.mychatroom.chatroom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MockDB {

    private ChatroomService chatroomService;
    private UserService userService;

    // TODO - for now assuming that sessionId is unique identifier for single user (as opposed to socket connection since user can have two tabs open)
    private Set<String> takenUsernames = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private Map<String, InMemoryUser> sessionToUserMap;
    private Map<String, Set<InMemoryUser>> roomToUsersMap = new HashMap<>();
    private Map<String, List<MessageDTO>> roomToMessageMap;

    @Autowired
    public MockDB(ChatroomService chatroomService, UserService userService) {
        this.chatroomService = chatroomService;
        this.userService = userService;

        this.sessionToUserMap = this.userService.getUsers();
        for (InMemoryUser user : this.sessionToUserMap.values()) {
            if (user.getUsername() != null && !user.getUsername().isEmpty())
                takenUsernames.add(user.getUsername());
        }

        this.roomToMessageMap = this.chatroomService.getChatroomMessages();
        for (String room : this.roomToMessageMap.keySet()) {
            this.roomToUsersMap.put(room, new HashSet<>());
        }
    }

    public void changeUsername(String previousName, String newName, InMemoryUser user) {
        this.takenUsernames.remove(previousName);
        this.takenUsernames.add(newName);
        this.userService.updateUser(user);
    }

    public boolean isUsernameTaken(String username) {
        return this.takenUsernames.contains(username);
    }

    public InMemoryUser getInMemoryUser(String sessionId, boolean createIfNotExists) {
        InMemoryUser user = this.sessionToUserMap.getOrDefault(sessionId, null);
        if (!createIfNotExists || user != null)
            return user;

        user = new InMemoryUser(sessionId);
        this.sessionToUserMap.put(sessionId, user);
        this.userService.saveUser(user);
        return user;
    }

    public InMemoryUser getInMemoryUser(String sessionId) {
        return this.getInMemoryUser(sessionId, false);
    }

    public Set<String> getChatrooms() {
        return this.roomToUsersMap.keySet();
    }

    public void createNewChatroom(String chatroom) {
        this.roomToUsersMap.put(chatroom, new HashSet<>());
        this.roomToMessageMap.put(chatroom, new ArrayList<>());
        this.chatroomService.createChatroom(chatroom);
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
        if (this.isExistingChatroom(chatroom))
            this.roomToUsersMap.get(chatroom).add(user);
    }

    public boolean removeUserFromChatroom(InMemoryUser user, String chatroom) {
        if (this.isExistingChatroom(chatroom))
            return this.roomToUsersMap.get(chatroom).remove(user);
        return false;
    }

    public List<MessageDTO> getMessages(String chatroom) {
        return this.roomToMessageMap.getOrDefault(chatroom, new ArrayList<>());
    }

    public void addMessage(MessageDTO message, String chatroom) {
        if (this.isExistingChatroom(chatroom)) {
            this.roomToMessageMap.get(chatroom).add(message);
            this.chatroomService.insertMessage(message, chatroom);
        }
    }

    public void addChatroomToUsersChatrooms(InMemoryUser user, String chatroom) {
        if (user.addToMyChatrooms(chatroom)) {
            userService.updateUser(user);
        }
    }
}
