package com.mychatroom.chatroom.service;

import com.amazonaws.services.dynamodbv2.document.*;
import com.mychatroom.chatroom.event.InMemoryUser;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserServiceImpl implements UserService {
    public static final String USER_TABLE_NAME = "User";

    @Autowired
    private DynamoDB dynamoDB;
    private Map<String, InMemoryUser> users;

    @Override
    public void saveUser(InMemoryUser user) {
        Item userItem = createUserItem(user);
        dynamoDB.getTable(USER_TABLE_NAME).putItem(userItem);
    }

    @Override
    public void updateUser(InMemoryUser user) {
        // TODO: replaces the previous Entry with this new Entry. Implement with updateItem if only needed to change one attribute at a time
        saveUser(user);
    }

    @Override
    public Map<String, InMemoryUser> getUsers() {
        if (users != null)
            return users;
        Map<String, InMemoryUser> users = new ConcurrentHashMap<>();
        ItemCollection<ScanOutcome> items = dynamoDB.getTable(USER_TABLE_NAME).scan();
        for (Item item : items) {
            InMemoryUser user = getUserFromItem(item);
            users.put(user.getUserSessionId(), user);
        }
        this.users = users;
        return users;
    }

    private Item createUserItem(InMemoryUser user) {
        Item item = new Item();
        item.withPrimaryKey(new PrimaryKey("sessionId", user.getUserSessionId()));
        if (user.getUsername() != null && !user.getUsername().isEmpty())
            item.with("username", user.getUsername());
        item.with("lastTimeChangedUsername", user.getLastTimeChangedUsername());

        if (user.getMyChatrooms() != null && user.getMyChatrooms().size() > 0)
            item.withStringSet("myChatrooms", user.getMyChatrooms());
        return item;
    }

    private InMemoryUser getUserFromItem(Item item) {
        InMemoryUser user = new InMemoryUser(item.getString("sessionId"));
        if (item.hasAttribute("username"))
            user.setUsername(item.getString("username"));
        user.setLastTimeChangedUsername(item.getLong("lastTimeChangedUsername"));
        if (item.hasAttribute("myChatrooms")) {
            user.setMyChatrooms(item.getStringSet("myChatrooms"));
        }
        return user;
    }
}
