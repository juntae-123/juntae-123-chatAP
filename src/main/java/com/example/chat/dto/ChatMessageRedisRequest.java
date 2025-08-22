package com.example.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRedisRequest {

    public enum MessageType {
        ENTER, TALK, LEAVE
    }

    private MessageType type;     // ENTER, TALK, LEAVE
    private Long roomId;          // 채팅방 ID
    private String sender;        // 보낸 사람 username
    private String message;       // 메시지 내용
    private LocalDateTime timestamp;
}
