package com.mychatroom.chatroom.event;

import com.mychatroom.chatroom.controller.MessageController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Component
public class WebSocketEventListener {

    @Autowired
    private MessageController messageController;

    @Autowired
    private ApplicationState applicationState;

    @EventListener
    private void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        // TODO - might have to add logic to refresh sessionId in case it's old
        String sessionId = headerAccessor.getSessionAttributes().get("sessionId").toString();

        InMemoryUser user = applicationState.getInMemoryUser(sessionId, true);
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
            if (applicationState.isExistingChatroom(chatroom)) {
                String sessionId = headerAccessor.getSessionAttributes().get("sessionId").toString();
                InMemoryUser user = applicationState.getInMemoryUser(sessionId);
                applicationState.addUserToChatroom(user, chatroom);
                user.updateSocketChatroom(headerAccessor.getSessionId(), chatroom, headerAccessor.getSubscriptionId());
                messageController.updateChatUsers(chatroom);
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
        InMemoryUser user = applicationState.getInMemoryUser(sessionId);
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

        if (applicationState.isExistingChatroom(chatroom)) {
            if (applicationState.removeUserFromChatroom(user, chatroom))
                messageController.updateChatUsers(chatroom);
        }
    }
}
