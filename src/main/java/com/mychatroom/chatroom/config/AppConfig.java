package com.mychatroom.chatroom.config;

import com.amazonaws.regions.Regions;
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
        return new ChatroomServiceImpl(userService());
    }

    @Bean
    public UserService userService() {
        return new UserServiceImpl();
    }

    @Bean
    public DynamoDB dynamoDB() {
        // TODO - find way to dynamically use localhost in dev and actual DynamoDB in prod
//        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
//                new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-east-2"))
//                .build();
//        return new DynamoDB(client);
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_2)
                .build();
        return new DynamoDB(client);
    }
}
