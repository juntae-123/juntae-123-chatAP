package com.example.chat.service;

import com.example.chat.dto.SignupRequest;
import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import com.example.chat.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 유저입니다.");
        }

        String encodedPw = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
                .username(request.getUsername())
                .password(encodedPw)
                .build();

        userRepository.save(user);
    }


    public Map<String, String> loginWithTokens(SignupRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.createToken(user.getUsername());
        String refreshToken = jwtProvider.createRefreshToken(user.getUsername());


        saveRefreshToken(user.getUsername(), refreshToken);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("token", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    public void updateFcmToken(String username, String fcmToken) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }


    public void saveRefreshToken(String username, String refreshToken) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
    }


    public boolean isRefreshTokenValid(String username, String refreshToken) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        return refreshToken.equals(user.getRefreshToken());
    }
}

