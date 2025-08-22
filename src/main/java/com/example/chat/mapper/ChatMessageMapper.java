package com.example.chat.mapper;

import com.example.chat.dto.ChatMessageRequest;
import com.example.chat.dto.ChatMessageResponse;
import com.example.chat.model.ChatMessage;
import com.example.chat.model.ChatRoom;
import com.example.chat.model.User;

import java.time.LocalDateTime;

public class ChatMessageMapper {

    public static ChatMessageResponse toDto(ChatMessage entity) {
        return ChatMessageResponse.builder()
                .sender(entity.getSender().getUsername())
                .message(entity.getMessage())
                .timestamp(entity.getTimestamp())
                .type(entity.getType().name())
                .build();
    }

    public static ChatMessage toEntity(ChatMessageRequest dto, User sender, ChatRoom room) {
        return ChatMessage.builder()
                .sender(sender)
                .chatRoom(room)
                .message(dto.getMessage())
                .type(ChatMessage.MessageType.TALK)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
