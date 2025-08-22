package com.example.chat.dto;

import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class ChatRoomCreateRequest {
    private String name;
}
