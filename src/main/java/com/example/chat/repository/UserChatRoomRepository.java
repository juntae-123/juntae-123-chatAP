package com.example.chat.repository;

import com.example.chat.model.UserChatRoom;
import com.example.chat.model.User;
import com.example.chat.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
    List<UserChatRoom> findByUser(User user);
    List<UserChatRoom> findByChatRoom(ChatRoom chatRoom);  // ← 추가
    void deleteByUserAndChatRoom(User user, ChatRoom chatRoom);
    boolean existsByUserAndChatRoom(User user, ChatRoom chatRoom);
}
