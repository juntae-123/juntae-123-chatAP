package com.example.chat.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatRoomResponse {
    private Long id;
    private String name;
    private int participantCount;
}