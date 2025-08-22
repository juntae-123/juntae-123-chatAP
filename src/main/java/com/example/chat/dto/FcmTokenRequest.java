package com.example.chat.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Data;

@Getter
@Setter
@Data
public class FcmTokenRequest {
    private String fcmToken;
}
