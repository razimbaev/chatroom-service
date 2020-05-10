package com.mychatroom.chatroom.dto;

import com.mychatroom.chatroom.event.InMemoryUser;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserChatroomUpdateDTO {
    private Set<String> users;
    private Map<String, String> modifiedUsernames;

    public UserChatroomUpdateDTO(Set<InMemoryUser> inMemoryUsers) {
        users = new HashSet<>();
        for (InMemoryUser user : inMemoryUsers) {
            users.add(user.getUsername());
        }
    }

    public UserChatroomUpdateDTO(Set<InMemoryUser> inMemoryUsers, Map<String, String> modifiedUsernames) {
        this(inMemoryUsers);
        this.modifiedUsernames = modifiedUsernames;
    }

    public Set<String> getUsers() {
        return users;
    }

    public Map<String, String> getModifiedUsernames() {
        return modifiedUsernames;
    }
}
