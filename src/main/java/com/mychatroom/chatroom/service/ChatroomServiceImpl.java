package com.mychatroom.chatroom.service;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.model.*;
import com.mychatroom.chatroom.dto.MessageDTO;
import com.mychatroom.chatroom.event.InMemoryUser;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatroomServiceImpl implements ChatroomService {

    private Map<String, Long> chatroomToMessageNumber = new ConcurrentHashMap<>();

    private Map<String, InMemoryUser> users;

    @Autowired
    private DynamoDB dynamoDB;

    @Autowired
    public ChatroomServiceImpl(UserService userService) {
        users = userService.getUsers();
    }

    @Override
    public Map<String, List<MessageDTO>> getChatroomMessages() {
        Map<String, List<MessageDTO>> chatroomMessages = new ConcurrentHashMap<>();

        // TODO - for now assuming results are not paginated and all result are immediately brought in (maybe create paginated version later)
        TableCollection<ListTablesResult> tables = dynamoDB.listTables();

        for (Table table : tables) {
            if (table.getTableName().equals(UserServiceImpl.USER_TABLE_NAME))
                continue;
            List<MessageDTO> messages = new ArrayList<>();
            ItemCollection<ScanOutcome> items = table.scan();
            List<Item> sortedItems = getSortMessageItems(items);
            for (Item item : sortedItems) {
                MessageDTO message = new MessageDTO();
                message.setContent(item.getString("messageContent"));
                message.setUser(this.users.getOrDefault(item.getString("userId"), null));
                messages.add(message);
            }

            chatroomMessages.put(table.getTableName(), messages);
            this.chatroomToMessageNumber.put(table.getTableName(), (long) messages.size());
        }

        return chatroomMessages;
    }

    @Override
    public void insertMessage(MessageDTO message, String chatroom) {
        Item messageItem = createMessageItem(message, chatroom);
        dynamoDB.getTable(chatroom).putItem(messageItem);
    }

    @Override
    public void createChatroom(String chatroom) {
        List<AttributeDefinition> attributeDefinitions = getChatroomAttributeDefinitions();
        List<KeySchemaElement> keySchema = getChatroomKeySchema();

        CreateTableRequest request = new CreateTableRequest()
                .withTableName(chatroom)
                .withKeySchema(keySchema)
                .withAttributeDefinitions(attributeDefinitions)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits(1L)
                        .withWriteCapacityUnits(1L));

        Table table = dynamoDB.createTable(request);
        chatroomToMessageNumber.put(chatroom, 0L);
    }

    private Item createMessageItem(MessageDTO message, String chatroom) {
        long counter = chatroomToMessageNumber.get(chatroom);
        this.chatroomToMessageNumber.put(chatroom, counter + 1);
        Item item = new Item();
        item.withPrimaryKey(new PrimaryKey("messageNumber", counter));
        item.with("messageContent", message.getContent());
        item.with("userId", message.getUserSessionId());
        ;
        counter++;
        return item;
    }

    private List<AttributeDefinition> getChatroomAttributeDefinitions() {
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("messageNumber").withAttributeType("N"));
        return attributeDefinitions;
    }

    private List<KeySchemaElement> getChatroomKeySchema() {
        List<KeySchemaElement> keySchema = new ArrayList<>();
        keySchema.add(new KeySchemaElement().withAttributeName("messageNumber").withKeyType(KeyType.HASH));
        return keySchema;
    }

    private List<Item> getSortMessageItems(ItemCollection<ScanOutcome> items) {
        List<Item> sortedItems = new ArrayList<>();
        for (Item item : items) {
            sortedItems.add(item);
        }
        Collections.sort(sortedItems, new SortByMessageNumber());

        return sortedItems;
    }

    private class SortByMessageNumber implements Comparator<Item> {

        @Override
        public int compare(Item x, Item y) {
            long xVal = x.getLong("messageNumber");
            long yVal = y.getLong("messageNumber");
            return Long.compare(xVal, yVal);
        }
    }
}
