package com.example.chat.repository;

import com.example.chat.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m JOIN FETCH m.chatRoom JOIN FETCH m.sender WHERE m.chatRoom.id = :roomId ORDER BY m.timestamp ASC")
    List<ChatMessage> findByChatRoomIdFetchRoomAndSenderOrderByTimestampAsc(@Param("roomId") Long roomId);

    @Query("SELECT m FROM ChatMessage m JOIN FETCH m.chatRoom JOIN FETCH m.sender WHERE m.chatRoom.id = :roomId AND m.timestamp > :timestamp ORDER BY m.timestamp ASC")
    List<ChatMessage> findByChatRoomIdAndTimestampAfterFetchRoomAndSenderOrderByTimestampAsc(@Param("roomId") Long roomId, @Param("timestamp") LocalDateTime timestamp);
}

