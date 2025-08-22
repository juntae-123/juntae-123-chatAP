package com.example.chat.controller;

import com.example.chat.dto.UserDto;
import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import com.example.chat.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String token = authHeader.substring(7);
        String username = jwtProvider.getUsername(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        String createdAt = user.getCreatedAt() != null ? user.getCreatedAt().toString() : "";

        UserDto userDto = new UserDto(
                user.getId(),
                user.getUsername(),
                createdAt
        );

        return ResponseEntity.ok(userDto);
    }
}
