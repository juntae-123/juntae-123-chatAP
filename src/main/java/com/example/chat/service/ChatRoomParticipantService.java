package com.example.chat.service;

import com.example.chat.model.ChatRoom;
import com.example.chat.model.ChatRoomParticipant;
import com.example.chat.model.User;
import com.example.chat.repository.ChatRoomParticipantRepository;
import com.example.chat.repository.ChatRoomRepository;
import com.example.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomParticipantService {

    private final ChatRoomParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    public LocalDateTime recordParticipation(String username, String roomId) {
        // 기존 참여자 확인
        ChatRoomParticipant existing = participantRepository.findByUserUsernameAndChatRoomId(username, Long.valueOf(roomId)).orElse(null);


        if (existing != null) {
            return existing.getJoinedAt();
        }

        // 없으면  조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ChatRoom chatRoom = chatRoomRepository.findById(Long.valueOf(roomId))
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        // 새로 참여자 생성 및 저장
        ChatRoomParticipant participant = ChatRoomParticipant.builder()
                .user(user)
                .chatRoom(chatRoom)
                .joinedAt(LocalDateTime.now())
                .build();

        participantRepository.save(participant);
        return participant.getJoinedAt();
    }

    public String getOtherParticipant(String roomId, String sender) {
        Long roomIdLong = Long.valueOf(roomId);
        List<String> participants = participantRepository.findUsernamesByRoomId(roomIdLong);
        return participants.stream()
                .filter(username -> !username.equals(sender))
                .findFirst()
                .orElse(null);
    }

}
