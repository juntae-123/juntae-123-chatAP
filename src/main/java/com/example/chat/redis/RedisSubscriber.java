package com.example.chat.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {


    @Override
    public void onMessage(Message message, byte[] pattern) {
        String receivedMessage = new String(message.getBody());
        String channel = new String(message.getChannel());
        System.out.println("[RedisSubscriber] 채널: " + channel + ", 메시지: " + receivedMessage);


    }
}
