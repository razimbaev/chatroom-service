package com.mychatroom.chatroom.controller;

import com.mychatroom.chatroom.dto.*;
import com.mychatroom.chatroom.event.ApplicationState;
import com.mychatroom.chatroom.event.InMemoryUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.util.*;

@Controller
public class MessageController {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ApplicationState applicationState;

    // TODO - maybe add pagination where needed and consider using SimpUserRegistry

    @SubscribeMapping("/mychatrooms")
    public Set<String> getMyChatrooms(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionAttributes().get("sessionId").toString();
        InMemoryUser user = applicationState.getInMemoryUser(sessionId);

        return user.getMyChatrooms();
    }

    @SubscribeMapping("/chatroomSuggestions")
    public Set<String> getChatroomSuggestions(SimpMessageHeaderAccessor headerAccessor) {
        return applicationState.getChatrooms();
    }

    @SubscribeMapping("/chatroom/create/{newChatroom}")
    public ChatroomCreateDTO createNewChatroom(@DestinationVariable String newChatroom) {
        if (newChatroom == null || newChatroom.isEmpty()) {
            return new ChatroomCreateDTO("Chatroom Name is blank");
        }

        if (applicationState.isExistingChatroom(newChatroom)) {
            return new ChatroomCreateDTO(newChatroom + " already exists");
        }

        if (!isChatroomNameValid(newChatroom)) {
            return new ChatroomCreateDTO(newChatroom + " is not a valid name");
        }

        applicationState.createNewChatroom(newChatroom);

        simpMessagingTemplate.convertAndSend("/topic/home/chatroom", new ChatroomHomeDTO(newChatroom));

        return new ChatroomCreateDTO();
    }

    private boolean isChatroomNameValid(String chatroomName) {
        if (chatroomName == null)
            return false;
        return chatroomName.matches("^[a-zA-Z0-9_-]{3,30}$");
    }

    @SubscribeMapping("/setUsername/{newUsername}")
    public UsernameChangeDTO setUsername(@DestinationVariable String newUsername, SimpMessageHeaderAccessor headerAccessor) {
        // TODO - add checks to make sure user name is valid, not taken (and maybe it does not look similar to another e.g. capital i and lower case L)
        // TODO - maybe send message as to why username is not allowed (for now assuming it is taken if false)
        // TODO - also do not allow users to rapidly change username over and over

        String sessionId = headerAccessor.getSessionAttributes().get("sessionId").toString();
        InMemoryUser user = applicationState.getInMemoryUser(sessionId);
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

        applicationState.changeUsername(previousUsername, newUsername, user);
        return new UsernameChangeDTO(previousUsername, newUsername,
                "", user.getTimeWhenUsernameChangeAllowed());
    }

    private boolean isUsernameTaken(String username) {
        if (username == null)
            return false;
        return applicationState.isUsernameTaken(username);
    }

    private boolean isUsernameValid(String username) {
        if (username == null)
            return false;
        return username.matches("^[a-zA-Z0-9_-]{3,20}$");
    }

    public void updateChatUsers(String chatroomName, String previousName, String newName) {
        Set<InMemoryUser> users = applicationState.getUsersInChatroom(chatroomName);
        Map<String, String> changed = new HashMap<>();
        changed.put(previousName, newName);

        this.simpMessagingTemplate.convertAndSend("/topic/chatroom/" + chatroomName + "/users",
                new UserChatroomUpdateDTO(users, changed));
    }

    public void updateChatUsers(String chatroomName) {
        Set<InMemoryUser> users = applicationState.getUsersInChatroom(chatroomName);

        this.simpMessagingTemplate.convertAndSend("/topic/chatroom/" + chatroomName + "/users",
                new UserChatroomUpdateDTO(users));

        int numUsers = applicationState.getNumUsersInChatroom(chatroomName);
        simpMessagingTemplate.convertAndSend("/topic/home/user", new UserHomeDTO(chatroomName, numUsers));
    }

    @SubscribeMapping("/home/init")
    public List<ChatroomHomeDTO> getAllChatrooms() {
        List<ChatroomHomeDTO> chatroomHomeData = new ArrayList<>();

        Set<String> chatrooms = applicationState.getChatrooms();
        for (String chatroom : chatrooms) {
            int numUser = applicationState.getNumUsersInChatroom(chatroom);
            List<MessageDTO> messages = applicationState.getMessages(chatroom);
            chatroomHomeData.add(new ChatroomHomeDTO(chatroom, numUser, messages));
        }
        return chatroomHomeData;
    }

    @SubscribeMapping("/chatroom/{chatroomName}/init")
    public ChatInitDTO initChatroomData(@DestinationVariable String chatroomName, SimpMessageHeaderAccessor headerAccessor) {
        // TODO - add some sort of way to pass in what the latest message the user already has (so chat can be cached on UI)
        List<MessageDTO> messages = applicationState.getMessages(chatroomName);

        String sessionId = headerAccessor.getSessionAttributes().get("sessionId").toString();
        InMemoryUser user = applicationState.getInMemoryUser(sessionId);

        ChatInitDTO chatInitDTO = new ChatInitDTO();
        chatInitDTO.setMessages(messages);
        chatInitDTO.setUsernames(getUsersInChatroom(chatroomName));
        chatInitDTO.setMyUsername(user.getUsername());
        chatInitDTO.setNextUsernameChangeAllowed(user.getTimeWhenUsernameChangeAllowed());
        return chatInitDTO;
    }

    private List<String> getUserUpdatesInChatroom(String chatroom) {
        List<String> usernames = new ArrayList<>();

        Set<InMemoryUser> users = applicationState.getUsersInChatroom(chatroom);
        if (users == null)
            return usernames;

        for (InMemoryUser user : users) {
            usernames.add(user.getUsername());
        }

        return usernames;
    }

    private List<String> getUsersInChatroom(String chatroom) {
        List<String> usernames = new ArrayList<>();

        Set<InMemoryUser> users = applicationState.getUsersInChatroom(chatroom);
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
        InMemoryUser user = applicationState.getInMemoryUser(sessionId);
        String username = user.getUsername();
        if (username == null || username.isEmpty())
            throw new Exception("Messages should not be sent without a user");  // TODO - maybe find better way to handle this
        message.setUser(user);

        applicationState.addMessage(message, chatroomName);

        simpMessagingTemplate.convertAndSend("/topic/home/message", new MessageHomeDTO(message, chatroomName));

        applicationState.addChatroomToUsersChatrooms(user, chatroomName);

        return message;
    }
}
