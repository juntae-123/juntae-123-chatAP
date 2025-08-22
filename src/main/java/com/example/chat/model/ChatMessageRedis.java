package com.example.chat.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRedis {

    public enum MessageType {
        ENTER, TALK, LEAVE
    }

    private MessageType type;
    private Long roomId;
    private String message;
    private String sender;
    private LocalDateTime timestamp;
}
