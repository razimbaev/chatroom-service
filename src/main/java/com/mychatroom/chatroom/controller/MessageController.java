package com.mychatroom.chatroom.controller;

import com.mychatroom.chatroom.model.MessageInfo;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {

    @MessageMapping("/sendMessage")
    @SendTo("/topic/chatroom")
    public MessageInfo greeting(MessageInfo message) {
        // TODO - save the message and find way to send all messages if they don't already have them
        return message;
    }
}
