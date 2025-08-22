package com.example.chat.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    public enum MessageType {
        ENTER, TALK, LEAVE, CHAT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    private String message;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    private LocalDateTime timestamp;


    public String getRoomId() {
        return chatRoom != null ? chatRoom.getId().toString() : null;
    }
}
