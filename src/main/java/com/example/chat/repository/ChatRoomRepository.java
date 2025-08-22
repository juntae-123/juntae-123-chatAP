package com.example.chat.repository;

import com.example.chat.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
        SELECT DISTINCT cr
        FROM ChatRoom cr
        LEFT JOIN FETCH cr.participants p
        LEFT JOIN FETCH p.user
    """)
    List<ChatRoom> findAllWithParticipants();

    @Query("""
        SELECT DISTINCT cr
        FROM ChatRoom cr
        JOIN FETCH cr.participants p
        JOIN FETCH p.user
        WHERE p.user.username = :username
    """)
    List<ChatRoom> findAllByParticipantUsernameFetchJoin(@Param("username") String username);

    @Query("""
        SELECT DISTINCT cr
        FROM ChatRoom cr
        LEFT JOIN FETCH cr.messages m
        LEFT JOIN FETCH m.sender
        LEFT JOIN FETCH cr.participants p
        LEFT JOIN FETCH p.user
        WHERE cr.id = :id
    """)
    Optional<ChatRoom> findWithMessagesAndParticipantsById(@Param("id") Long id);
}
