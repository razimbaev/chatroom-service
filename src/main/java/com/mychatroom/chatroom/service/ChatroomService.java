package com.mychatroom.chatroom.service;

import com.mychatroom.chatroom.dto.MessageDTO;

import java.util.List;
import java.util.Map;

public interface ChatroomService {

    public Map<String, List<MessageDTO>> getChatroomMessages();

    public void insertMessage(MessageDTO message, String chatroom);

    public void createChatroom(String chatroom);
}
