package com.example.chat.fcm;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fcm")
@RequiredArgsConstructor
public class FcmTestController {

    private final FcmService fcmService;

    //  토큰, 제목, 내용 받아서 전송
    @PostMapping("/send")
    public String send(
            @RequestParam String token,
            @RequestParam String title,
            @RequestParam String body) {
        fcmService.sendMessage(token, title, body);
        return "푸시 전송 완료";
    }

    //테스트용 고정 메시지 전송
    @GetMapping("/send/test")
    public String sendTest(@RequestParam String token) {
        fcmService.sendMessage(token, "테스트 제목", "테스트 내용입니다.");
        return "테스트 푸시 전송 완료";
    }
}
