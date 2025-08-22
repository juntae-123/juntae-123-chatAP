package com.example.chat.controller;
import com.example.chat.model.User;
import com.example.chat.dto.ChatMessageRequest;
import com.example.chat.dto.ChatMessageResponse;
import com.example.chat.mapper.ChatMessageMapper;
import com.example.chat.model.ChatMessage;
import com.example.chat.model.ChatRoom;
import com.example.chat.repository.UserRepository;
import com.example.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatrooms")
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    @PostMapping("/{roomId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @PathVariable Long roomId,
            @RequestBody ChatMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User sender = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("유저 못 찾음"));

        ChatRoom room = chatService.getChatRoomById(roomId);

        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .message(request.getMessage())
                .type(ChatMessage.MessageType.TALK)
                .build();

        ChatMessage saved = chatService.saveMessage(message); // FCM 자동 전송
        return ResponseEntity.ok(ChatMessageMapper.toDto(saved));
    }

    @GetMapping("/{roomId}/messages")
    public List<ChatMessageResponse> getMessages(@PathVariable Long roomId) {
        return chatService.getMessagesByRoom(roomId).stream()
                .map(ChatMessageMapper::toDto)
                .collect(Collectors.toList());
    }
}


