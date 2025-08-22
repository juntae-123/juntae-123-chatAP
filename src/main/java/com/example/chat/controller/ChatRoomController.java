package com.example.chat.controller;

import com.example.chat.dto.ChatRoomResponse;
import com.example.chat.mapper.ChatRoomMapper;
import com.example.chat.model.ChatRoom;
import com.example.chat.security.JwtProvider;
import com.example.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatrooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final JwtProvider jwtProvider;

    @PostMapping
    public ChatRoomResponse createRoom(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("채팅방 이름은 비어 있을 수 없습니다.");
        }
        ChatRoom room = chatRoomService.createRoom(name.trim());
        return ChatRoomMapper.toDto(room);
    }

    @GetMapping
    public List<ChatRoomResponse> getAllRooms() {
        return chatRoomService.getAllRooms().stream()
                .map(ChatRoomMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ChatRoomResponse getRoom(@PathVariable Long id) {
        ChatRoom room = chatRoomService.getRoomWithMessages(id);
        return ChatRoomMapper.toDto(room);
    }

    @DeleteMapping("/{id}")
    public void deleteRoom(@PathVariable Long id) {
        chatRoomService.deleteRoom(id);
    }

    @DeleteMapping("/{id}/leave")
    public void leaveRoom(@PathVariable Long id,
                          @RequestHeader("Authorization") String token) {
        String username = jwtProvider.getUsername(extractToken(token));
        chatRoomService.leaveRoom(username, id);
    }

    @PostMapping("/{id}/join")
    public void joinRoom(@PathVariable Long id,
                         @RequestHeader("Authorization") String token) {
        String username = jwtProvider.getUsername(extractToken(token));
        chatRoomService.joinRoom(username, id);
    }

    @GetMapping("/my")
    public List<ChatRoomResponse> getMyRooms(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        List<ChatRoom> rooms = chatRoomService.getRoomsByUsername(username);
        return rooms.stream()
                .map(ChatRoomMapper::toDto)
                .collect(Collectors.toList());
    }

    private String extractToken(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        throw new IllegalArgumentException("잘못된 Authorization 헤더 형식입니다.");
    }
}
