package com.mychatroom.chatroom.event;

import com.mychatroom.chatroom.dto.MessageDTO;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MockDB {

    // TODO - for now assuming that sessionId is unique identifier for single user (as opposed to socket connection since user can have two tabs open)
    public static Set<String> takenUsernames = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public static Map<String, InMemoryUser> sessionToUserMap = new HashMap<>();
    public static Map<String, Set<InMemoryUser>> roomToUsersMap = new HashMap<>();
    public static Map<String, List<MessageDTO>> roomToMessageMap = new HashMap<>();

    static {
        // temporarily hardcoded values
        String[] rooms = new String[]{"Corona", "France", "Swimming", "Economics"};

        for (String room : rooms) {
            roomToUsersMap.put(room, new HashSet<>());
            roomToMessageMap.put(room, new ArrayList<>());
        }
    }
}
