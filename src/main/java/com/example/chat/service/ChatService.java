package com.example.chat.service;

import com.example.chat.model.ChatMessage;
import com.example.chat.model.ChatRoom;
import com.example.chat.model.User;
import com.example.chat.repository.ChatMessageRepository;
import com.example.chat.repository.ChatRoomParticipantRepository;
import com.example.chat.repository.ChatRoomRepository;
import com.example.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final FirebaseMessageService firebaseMessageService;
    private final UserRepository userRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;


    public ChatMessage saveMessage(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        ChatMessage savedMessage = chatMessageRepository.save(message);
        sendPushNotification(savedMessage);
        return savedMessage;
    }

    private void sendPushNotification(ChatMessage message) {
        User sender = message.getSender();
        Long roomId = message.getChatRoom().getId();

        // 채팅방에 참여 중인 모든 유저 ID 조회
        List<String> usernames = chatRoomParticipantRepository.findUsernamesByRoomId(roomId);

        // 본인을 제외한 모든 상대에게 알림 전송
        for (String username : usernames) {
            if (username.equals(sender.getUsername())) continue;

            userRepository.findByUsername(username).ifPresent(receiverUser -> {
                if (receiverUser.getFcmToken() != null) {
                    firebaseMessageService.sendMessage(
                            receiverUser.getFcmToken(),
                            sender.getUsername() + "님의 새 메시지",
                            message.getMessage()
                    );
                }
            });
        }
    }



    public List<ChatMessage> getMessagesByRoom(Long roomId) {
        return chatMessageRepository.findByChatRoomIdFetchRoomAndSenderOrderByTimestampAsc(roomId);
    }

    public List<ChatMessage> getMessagesAfter(Long roomId, LocalDateTime joinedAt) {
        return chatMessageRepository.findByChatRoomIdAndTimestampAfterFetchRoomAndSenderOrderByTimestampAsc(roomId, joinedAt);
    }
    public ChatRoom getChatRoomById(Long roomId) {
        return chatRoomRepository.findWithMessagesAndParticipantsById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found. id: " + roomId));
    }
}



