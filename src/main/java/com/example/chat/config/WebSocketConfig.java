package com.example.chat.config;

import com.example.chat.handler.ChatWebSocketHandler;
import com.example.chat.interceptor.JwtHandshakeInterceptor;
import com.example.chat.security.JwtProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler handler;
    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    public WebSocketConfig(ChatWebSocketHandler handler, JwtProvider jwtProvider, UserDetailsService userDetailsService) {
        this.handler = handler;
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/chat")
                .addInterceptors(
                        new JwtHandshakeInterceptor(jwtProvider, userDetailsService),
                        new HttpSessionHandshakeInterceptor()

                )
                .setAllowedOriginPatterns("*"); // CORS 문제 방지
    }
}
