package com.example.chat.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class FirebaseMessageService {

    public void sendMessage(String targetToken, String title, String body) {
        Message message = Message.builder()
                .setToken(targetToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("푸시 전송 성공: " + response);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException("푸시 전송 실패: " + e.getMessage(), e);
        }
    }
}
