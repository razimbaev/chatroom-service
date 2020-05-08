package com.mychatroom.chatroom.config;

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
}
