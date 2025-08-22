package com.example.chat.fcm;

import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

@Service
public class FcmService {

    public void sendMessage(String targetToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("FCM 전송 성공: " + response);

        } catch (FirebaseMessagingException e) {
            System.err.println("FCM 전송 실패: " + e.getMessage());
        }
    }
}
