package com.example.chat.mapper;

import com.example.chat.model.User;
import com.example.chat.dto.UserResponse;

public class UserMapper {

    public static UserResponse toDto(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build();
    }
}
