package com.mychatroom.chatroom.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.mychatroom.chatroom.service.ChatroomService;
import com.mychatroom.chatroom.service.ChatroomServiceImpl;
import com.mychatroom.chatroom.service.UserService;
import com.mychatroom.chatroom.service.UserServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public ChatroomService chatroomService() {
        return new ChatroomServiceImpl();
    }

    @Bean
    public UserService userService() {
        return new UserServiceImpl();
    }

    @Bean
    public DynamoDB dynamoDB() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-east-2"))
                .build();
        return new DynamoDB(client);
    }
}
