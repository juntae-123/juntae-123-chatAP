package com.example.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {
    private String sender;
    private String message;
    private LocalDateTime timestamp;
    private String type;
}
