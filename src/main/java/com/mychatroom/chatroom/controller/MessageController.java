package com.mychatroom.chatroom.controller;

import com.mychatroom.chatroom.dto.ChatInitDTO;
import com.mychatroom.chatroom.dto.MessageDTO;
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
    public boolean setUsername(@DestinationVariable String newUsername, SimpMessageHeaderAccessor headerAccessor) {
        // TODO - add checks to make sure user name is valid, not taken (and maybe it does not look similar to another e.g. capital i and lower case L)
        // TODO - maybe send message as to why username is not allowed (for now assuming it is taken if false)
        // TODO - also do not allow users to rapidly change username over and over

        String sessionId = headerAccessor.getSessionAttributes().get("sessionId").toString();
        InMemoryUser user = MockDB.sessionToUserMap.get(sessionId);
        user.setUsername(newUsername);
        for (String chatroom : user.getChatrooms())
            updateChatUsers(chatroom);

        return true;
    }

    public void updateChatUsers(String chatroomName) {
        this.simpMessagingTemplate.convertAndSend("/topic/chatroom/" + chatroomName + "/users", getUsersInChatroom(chatroomName));
    }

    @SubscribeMapping("/chatrooms")
    public Set<String> getAllChatrooms() {
        return MockDB.roomToMessageMap.keySet();
    }

    @SubscribeMapping("/chatroom/{chatroomName}/init")
    public ChatInitDTO initChatroomData(@DestinationVariable String chatroomName) {
        // TODO - add some sort of way to pass in what the latest message the user already has (so chat can be cached on UI)
        List<MessageDTO> messages = new ArrayList<>();
        if (MockDB.roomToMessageMap.containsKey(chatroomName)) {
            messages = MockDB.roomToMessageMap.get(chatroomName);
        }
        ChatInitDTO chatInitDTO = new ChatInitDTO();
        chatInitDTO.setMessages(messages);
        chatInitDTO.setUsernames(getUsersInChatroom(chatroomName));
        return chatInitDTO;
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
    public MessageDTO sendMessage(@DestinationVariable String chatroomName, MessageDTO message) {
        if (MockDB.roomToMessageMap.containsKey(chatroomName)) {
            List<MessageDTO> messages = MockDB.roomToMessageMap.get(chatroomName);
            messages.add(message);
        }

        return message;
    }
}
