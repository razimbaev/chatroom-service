package com.mychatroom.chatroom.controller;

import com.mychatroom.chatroom.dto.*;
import com.mychatroom.chatroom.event.InMemoryUser;
import com.mychatroom.chatroom.event.MockDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
public class MessageController {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    // TODO - maybe add pagination where needed and consider using SimpUserRegistry

    @SubscribeMapping("/setUsername/{newUsername}")
    public UsernameChangeDTO setUsername(@DestinationVariable String newUsername, SimpMessageHeaderAccessor headerAccessor) {
        // TODO - add checks to make sure user name is valid, not taken (and maybe it does not look similar to another e.g. capital i and lower case L)
        // TODO - maybe send message as to why username is not allowed (for now assuming it is taken if false)
        // TODO - also do not allow users to rapidly change username over and over

        String sessionId = headerAccessor.getSessionAttributes().get("sessionId").toString();
        InMemoryUser user = MockDB.sessionToUserMap.get(sessionId);
        String previousUsername = user.getUsername();

        if (newUsername == null || newUsername.isEmpty()) {
            return new UsernameChangeDTO(previousUsername, newUsername,
                    "Username is blank", user.getTimeWhenUsernameChangeAllowed());
        }

        // check if exists
        if (isUsernameTaken(newUsername)) {
            return new UsernameChangeDTO(previousUsername, newUsername,
                    newUsername + " is taken", user.getTimeWhenUsernameChangeAllowed());
        }

        // check if passes regex
        if (!isUsernameValid(newUsername)) {
            return new UsernameChangeDTO(previousUsername, newUsername,
                    newUsername + " is invalid",
                    user.getTimeWhenUsernameChangeAllowed());
        }

        if (!user.setUsername(newUsername))
            return new UsernameChangeDTO(previousUsername, newUsername,
                    "24 hours has not passed since last username change", user.getTimeWhenUsernameChangeAllowed());

        for (String chatroom : user.getChatrooms())
            updateChatUsers(chatroom, previousUsername, newUsername);

        MockDB.takenUsernames.add(newUsername);
        MockDB.takenUsernames.remove(previousUsername);
        return new UsernameChangeDTO(previousUsername, newUsername,
                "", user.getTimeWhenUsernameChangeAllowed());
    }

    private boolean isUsernameTaken(String username) {
        if (username == null)
            return false;
        return MockDB.takenUsernames.contains(username);
    }

    private boolean isUsernameValid(String username) {
        if (username == null)
            return false;
        return username.matches("^[a-zA-Z0-9_-]{3,20}$");
    }

    public void updateChatUsers(String chatroomName, InMemoryUser user, UserChatroomEvent event) {
        this.simpMessagingTemplate.convertAndSend("/topic/chatroom/" + chatroomName + "/users",
                new UserChatroomUpdateDTO(user.getUsername(), event));
    }

    public void updateChatUsers(String chatroomName, String oldName, String newName) {
        this.simpMessagingTemplate.convertAndSend("/topic/chatroom/" + chatroomName + "/users",
                new UserChatroomUpdateDTO(oldName, newName));
    }

    @SubscribeMapping("/chatrooms")
    public Set<String> getAllChatrooms() {
        return MockDB.roomToMessageMap.keySet();
    }

    @SubscribeMapping("/chatroom/{chatroomName}/init")
    public ChatInitDTO initChatroomData(@DestinationVariable String chatroomName, SimpMessageHeaderAccessor headerAccessor) {
        // TODO - add some sort of way to pass in what the latest message the user already has (so chat can be cached on UI)
        List<MessageDTO> messages = new ArrayList<>();
        if (MockDB.roomToMessageMap.containsKey(chatroomName)) {
            messages = MockDB.roomToMessageMap.get(chatroomName);
        }

        String sessionId = headerAccessor.getSessionAttributes().get("sessionId").toString();
        InMemoryUser user = MockDB.sessionToUserMap.get(sessionId);

        ChatInitDTO chatInitDTO = new ChatInitDTO();
        chatInitDTO.setMessages(messages);
        chatInitDTO.setUsernames(getUsersInChatroom(chatroomName));
        chatInitDTO.setMyUsername(user.getUsername());
        chatInitDTO.setNextUsernameChangeAllowed(user.getTimeWhenUsernameChangeAllowed());
        return chatInitDTO;
    }

    private List<String> getUserUpdatesInChatroom(String chatroom) {
        List<String> usernames = new ArrayList<>();

        Set<InMemoryUser> users = MockDB.roomToUsersMap.getOrDefault(chatroom, null);
        if (users == null)
            return usernames;

        for (InMemoryUser user : users) {
            usernames.add(user.getUsername());
        }

        return usernames;
    }

    private List<String> getUsersInChatroom(String chatroom) {
        List<String> usernames = new ArrayList<>();

        Set<InMemoryUser> users = MockDB.roomToUsersMap.getOrDefault(chatroom, null);
        if (users == null)
            return usernames;

        for (InMemoryUser user : users) {
            usernames.add(user.getUsername());
        }

        return usernames;
    }

    @MessageMapping("/sendMessage/{chatroomName}")
    @SendTo("/topic/chatroom/{chatroomName}")
    public MessageDTO sendMessage(@DestinationVariable String chatroomName, MessageDTO message, SimpMessageHeaderAccessor headerAccessor) throws Exception {
        String sessionId = headerAccessor.getSessionAttributes().get("sessionId").toString();
        String username = MockDB.sessionToUserMap.get(sessionId).getUsername();
        if (username == null || username.isEmpty())
            throw new Exception("Messages should not be sent without a user");  // TODO - maybe find better way to handle this
        message.setUserId(username);    // prevent username spoofing

        if (MockDB.roomToMessageMap.containsKey(chatroomName)) {
            List<MessageDTO> messages = MockDB.roomToMessageMap.get(chatroomName);
            messages.add(message);
        }

        return message;
    }
}
