package com.example.chat.service;

import com.example.chat.model.ChatRoom;
import com.example.chat.model.User;
import com.example.chat.model.UserChatRoom;
import com.example.chat.repository.ChatRoomRepository;
import com.example.chat.repository.UserChatRoomRepository;
import com.example.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatRoom createRoom(String name) {
        ChatRoom room = ChatRoom.builder().name(name).build();
        return chatRoomRepository.save(room);
    }

    @Transactional(readOnly = true)
    public List<ChatRoom> getAllRooms() {
        return chatRoomRepository.findAllWithParticipants();
    }

    @Transactional(readOnly = true)
    public ChatRoom getRoomWithMessages(Long roomId) {
        return chatRoomRepository.findWithMessagesAndParticipantsById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방"));
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방 없음"));

        // FK 문제 예방: 참여자 삭제
        List<UserChatRoom> participants = userChatRoomRepository.findByChatRoom(room);
        userChatRoomRepository.deleteAll(participants);

        // 채팅방 삭제
        chatRoomRepository.delete(room);
    }

    public void leaveRoom(String username, Long roomId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방 없음"));

        userChatRoomRepository.deleteByUserAndChatRoom(user, room);
    }

    public void joinRoom(String username, Long roomId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방 없음"));

        boolean alreadyJoined = userChatRoomRepository.existsByUserAndChatRoom(user, room);
        if (alreadyJoined) return;

        UserChatRoom participation = UserChatRoom.builder()
                .user(user)
                .chatRoom(room)
                .joinedAt(LocalDateTime.now())
                .build();

        userChatRoomRepository.save(participation);
    }

    @Transactional(readOnly = true)
    public List<ChatRoom> getRoomsByUsername(String username) {
        return chatRoomRepository.findAllByParticipantUsernameFetchJoin(username);
    }
}
