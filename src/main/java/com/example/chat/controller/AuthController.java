package com.example.chat.controller;

import com.example.chat.dto.FcmTokenRequest;
import com.example.chat.dto.SignupRequest;
import com.example.chat.security.JwtProvider;
import com.example.chat.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider; // 토큰 생성용

    // ------------------- 회원가입 -------------------
    @PostMapping("/api/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    // ------------------- 로그인 -------------------
    @PostMapping("/api/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody SignupRequest request) {
        // access + refresh token 발급
        Map<String, String> tokens = authService.loginWithTokens(request);

        Map<String, String> response = new HashMap<>();
        response.put("token", tokens.get("token"));
        response.put("refreshToken", tokens.get("refreshToken"));
        response.put("username", request.getUsername());
        return ResponseEntity.ok(response);
    }

    // ------------------- 토큰 갱신 -------------------
    @PostMapping("/api/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token missing"));
        }

        String username = jwtProvider.getUsernameFromToken(refreshToken);

        if (!authService.isRefreshTokenValid(username, refreshToken)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid refresh token"));
        }

        String newAccessToken = jwtProvider.createToken(username);
        String newRefreshToken = jwtProvider.createRefreshToken(username);

        // refresh token DB 갱신
        authService.saveRefreshToken(username, newRefreshToken);

        Map<String, String> response = new HashMap<>();
        response.put("token", newAccessToken);
        response.put("refreshToken", newRefreshToken);
        return ResponseEntity.ok(response);
    }

    // ------------------- FCM 토큰 저장 -------------------
    @PostMapping("/fcm-token")
    public ResponseEntity<String> updateFcmToken(@RequestBody FcmTokenRequest request,
                                                 @RequestHeader("Authorization") String token) {
        String username = jwtProvider.getUsernameFromToken(token.replace("Bearer ", ""));
        authService.updateFcmToken(username, request.getFcmToken());
        return ResponseEntity.ok("FCM 토큰 저장 완료");
    }
}
