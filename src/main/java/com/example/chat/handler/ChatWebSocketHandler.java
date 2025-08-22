package com.example.chat.handler;

import com.example.chat.dto.ChatMessageRedisRequest;
import com.example.chat.model.ChatMessage;
import com.example.chat.model.ChatRoom;
import com.example.chat.model.User;
import com.example.chat.repository.ChatMessageRepository;
import com.example.chat.repository.UserRepository;
import com.example.chat.repository.ChatRoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ChannelTopic topic;

    private final Map<Long, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> roomUsers = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UserDetails userDetails = (UserDetails) session.getAttributes().get("user");
        if (userDetails == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthorized"));
            return;
        }

        Long roomId = parseRoomId(session);

        // 세션 관리
        roomSessions.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
        roomSessions.get(roomId).add(session);

        roomUsers.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
        roomUsers.get(roomId).add(userDetails.getUsername());

        sendRecentMessages(session, roomId);

        // ENTER 메시지 브로드캐스트 (Redis Set 기반, 중복 방지)
        String username = userDetails.getUsername();
        String redisKey = "chat:room:" + roomId + ":enterUsers";

        Boolean alreadyEntered = redisTemplate.opsForSet().isMember(redisKey, username);
        if (!Boolean.TRUE.equals(alreadyEntered)) {
            // Redis Set에 추가
            redisTemplate.opsForSet().add(redisKey, username);

            ChatMessageRedisRequest enterMsg = ChatMessageRedisRequest.builder()
                    .type(ChatMessageRedisRequest.MessageType.ENTER)
                    .roomId(roomId)
                    .sender(username)
                    .message(username + "님이 입장했습니다")
                    .timestamp(LocalDateTime.now())
                    .build();

            broadcastMessage(roomId, enterMsg, null);
            saveToRedis(roomId, enterMsg);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        UserDetails userDetails = (UserDetails) session.getAttributes().get("user");
        if (userDetails == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthorized"));
            return;
        }

        ChatMessageRedisRequest redisMsg = mapper.readValue(message.getPayload(), ChatMessageRedisRequest.class);
        Long roomId = redisMsg.getRoomId();
        String username = userDetails.getUsername();

        if (redisMsg.getType() == ChatMessageRedisRequest.MessageType.TALK) {
            redisMsg.setSender(username);
            redisMsg.setTimestamp(LocalDateTime.now());
            broadcastMessage(roomId, redisMsg, session);
            saveToRedis(roomId, redisMsg);
            saveToDB(redisMsg);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long roomId = parseRoomId(session);
        roomSessions.getOrDefault(roomId, ConcurrentHashMap.newKeySet()).remove(session);

        UserDetails userDetails = (UserDetails) session.getAttributes().get("user");
        if (userDetails != null) {
            String username = userDetails.getUsername();
            roomUsers.getOrDefault(roomId, ConcurrentHashMap.newKeySet()).remove(username);

            // Redis Set에서 제거하여 다음 입장 시 ENTER 메시지 발생 가능
            String redisKey = "chat:room:" + roomId + ":enterUsers";
            redisTemplate.opsForSet().remove(redisKey, username);
        }
    }

    // ======================
    // 공통 유틸
    // ======================
    private Long parseRoomId(WebSocketSession session) {
        String query = Objects.requireNonNull(session.getUri()).getQuery();
        Map<String, String> params = Arrays.stream(query.split("&"))
                .map(s -> s.split("="))
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));
        return Long.valueOf(params.get("roomId"));
    }

    private void broadcastMessage(Long roomId, ChatMessageRedisRequest msg, WebSocketSession exclude) {
        try {
            String json = mapper.writeValueAsString(msg);
            roomSessions.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
            roomSessions.get(roomId).forEach(sess -> {
                if (exclude == null || !sess.getId().equals(exclude.getId())) {
                    sendMessageSafely(sess, new TextMessage(json));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveToRedis(Long roomId, ChatMessageRedisRequest msg) {
        try {
            String json = mapper.writeValueAsString(msg);
            String key = "chat:room:" + roomId;
            redisTemplate.opsForList().rightPush(key, json);
            redisTemplate.opsForList().trim(key, -100, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveToDB(ChatMessageRedisRequest msg) {
        try {
            ChatRoom room = chatRoomRepository.findById(msg.getRoomId()).orElseThrow();
            User sender = userRepository.findByUsername(msg.getSender()).orElseThrow();

            ChatMessage chatEntity = ChatMessage.builder()
                    .chatRoom(room)
                    .sender(sender)
                    .message(msg.getMessage())
                    .type(ChatMessage.MessageType.TALK)
                    .timestamp(msg.getTimestamp())
                    .build();

            chatMessageRepository.save(chatEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessageSafely(WebSocketSession session, TextMessage message) {
        synchronized (session) {
            try {
                if (session != null && session.isOpen()) {
                    session.sendMessage(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendRecentMessages(WebSocketSession session, Long roomId) {
        String key = "chat:room:" + roomId;
        List<String> messages = redisTemplate.opsForList().range(key, 0, -1);
        if (messages != null) {
            messages.forEach(json -> sendMessageSafely(session, new TextMessage(json)));
        }
    }
}
