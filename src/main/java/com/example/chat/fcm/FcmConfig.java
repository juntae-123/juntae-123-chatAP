package com.example.chat.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FcmConfig {

    @PostConstruct
    public void initialize() {
        try {
            ClassPathResource resource = new ClassPathResource("firebase/chat-63b7d-firebase-adminsdk-fbsvc-993a8a2ec9.json");
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            throw new RuntimeException("FCM 초기화 실패", e);
        }
    }
}
