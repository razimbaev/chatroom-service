package com.mychatroom.chatroom.event;

import com.mychatroom.chatroom.controller.MessageController;
import com.mychatroom.chatroom.dto.UserChatroomEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Set;

@Component
public class WebSocketEventListener {

    @Autowired
    private MessageController messageController;

    @EventListener
    private void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        // TODO - might have to add logic to refresh sessionId in case it's old
        String sessionId = headerAccessor.getSessionAttributes().get("sessionId").toString();

        InMemoryUser user;
        if (!MockDB.sessionToUserMap.containsKey(sessionId)) {
            user = new InMemoryUser(sessionId);
            MockDB.sessionToUserMap.put(sessionId, user);
        } else {
            user = MockDB.sessionToUserMap.get(sessionId);
        }

        user.addSocketSessionIfNotExists(headerAccessor.getHeader("simpSessionId").toString());
    }

    @EventListener
    private void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionAttributes().get("sessionId").toString();
        String socketSessionId = headerAccessor.getSessionId();

        disconnectSocket(sessionId, socketSessionId, "");
    }

    @EventListener
    private void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String chatroom = getChatroomFromDestination(headerAccessor.getDestination());
        if (chatroom != null) {
            if (MockDB.roomToUsersMap.containsKey(chatroom)) {
                String sessionId = headerAccessor.getSessionAttributes().get("sessionId").toString();
                InMemoryUser user = MockDB.sessionToUserMap.get(sessionId);
                MockDB.roomToUsersMap.get(chatroom).add(user);
                user.updateSocketChatroom(headerAccessor.getSessionId(), chatroom, headerAccessor.getSubscriptionId());
                messageController.updateChatUsers(chatroom, user, UserChatroomEvent.JOIN);
            }
        }
    }

    @EventListener
    private void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionAttributes().get("sessionId").toString();
        String socketSessionId = headerAccessor.getSessionId();

        disconnectSocket(sessionId, socketSessionId, headerAccessor.getSubscriptionId());
    }

    private String getChatroomFromDestination(String destination) {
        String before = "/topic/chatroom/";
        if (destination.matches(before + ".*")) {
            return destination.substring(before.length());
        }
        return null;
    }

    private void disconnectSocket(String sessionId, String socketSessionId, String subscriptionId) {
        InMemoryUser user = MockDB.sessionToUserMap.getOrDefault(sessionId, null);
        if (user == null)
            return;

        String[] chatroomSub = user.getChatroomSubscriptionFromSocketSession(socketSessionId);
        if (chatroomSub == null)
            return;
        String chatroom = chatroomSub[0];
        String subId = chatroomSub[1];

        if (!subscriptionId.isEmpty() && !subscriptionId.equals(subId))
            return;

        user.removeSocketSession(socketSessionId);

        if (user.isChatroomInSocketSession(chatroom))
            return;
        Set<InMemoryUser> usersInChatroom = MockDB.roomToUsersMap.getOrDefault(chatroom, null);
        if (usersInChatroom != null) {
            if (usersInChatroom.remove(user)) {
                messageController.updateChatUsers(chatroom, user, UserChatroomEvent.LEAVE);
            }
        }

    }
}
