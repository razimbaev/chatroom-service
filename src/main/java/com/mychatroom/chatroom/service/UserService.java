package com.mychatroom.chatroom.service;

import com.mychatroom.chatroom.event.InMemoryUser;

import java.util.Map;

public interface UserService {

    public void saveUser(InMemoryUser user);

    public void updateUser(InMemoryUser user);

    public Map<String, InMemoryUser> getUsers();
}
