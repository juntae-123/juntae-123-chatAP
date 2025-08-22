package com.example.chat.mapper;

import com.example.chat.dto.ChatRoomCreateRequest;
import com.example.chat.dto.ChatRoomResponse;
import com.example.chat.model.ChatRoom;

public class ChatRoomMapper {

    public static ChatRoom toEntity(ChatRoomCreateRequest dto) {
        return ChatRoom.builder()
                .name(dto.getName())
                .build();
    }

    public static ChatRoomResponse toDto(ChatRoom room) {
        int participantCount = room.getParticipants() != null ? room.getParticipants().size() : 0;

        return ChatRoomResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .participantCount(participantCount)
                .build();
    }
}

