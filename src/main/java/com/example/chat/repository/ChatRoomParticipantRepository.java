package com.example.chat.repository;

import com.example.chat.model.ChatRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {

    Optional<ChatRoomParticipant> findByUserUsernameAndChatRoomId(String username, Long roomId);

    @Query("SELECT c.user.username FROM ChatRoomParticipant c WHERE c.chatRoom.id = :roomId")
    List<String> findUsernamesByRoomId(Long roomId);
}
