package com.example.chat.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseInitializerConfig {

    @Value("${firebase.config.path}")
    private String firebaseConfigPath;

    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().isEmpty()) { // 중복 초기화 방지
                FileInputStream serviceAccount = new FileInputStream(firebaseConfigPath);
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Admin SDK Initialized");
            }
        } catch (IOException e) {
            System.err.println("Firebase 초기화 실패: " + e.getMessage());
            throw new IllegalStateException("Firebase 초기화 실패", e);
        }
    }
}
