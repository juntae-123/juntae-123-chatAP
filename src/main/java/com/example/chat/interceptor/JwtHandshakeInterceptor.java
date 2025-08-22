package com.example.chat.interceptor;

import com.example.chat.security.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    public JwtHandshakeInterceptor(JwtProvider jwtProvider, UserDetailsService userDetailsService) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();

            // 1️⃣ Query Parameter로 토큰 받기
            String token = httpRequest.getParameter("token");

            // 2️⃣ 없으면 Authorization 헤더에서 추출
            if (token == null || token.isBlank()) {
                String authHeader = httpRequest.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }

            // 3️⃣ JWT 검증
            if (token != null && !token.isBlank() && jwtProvider.validateToken(token)) {
                String username = jwtProvider.getUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // WebSocket attributes에 인증 정보 저장
                attributes.put("user", userDetails);

                // Spring Security Context에 인증 정보 설정
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);

                // SPRING_SECURITY_CONTEXT도 attributes에 추가 (필요 시)
                attributes.put("SPRING_SECURITY_CONTEXT", context);

                System.out.println("✅ JwtHandshakeInterceptor: handshake authorized for " + username);
                return true;
            } else {
                System.out.println("❌ JwtHandshakeInterceptor: invalid or missing token, handshake denied");
            }
        }

        // 4️⃣ 인증 실패: 연결 거부
        response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // 필요시 로깅
        if (exception != null) {
            System.out.println("❌ afterHandshake exception: " + exception.getMessage());
        }
    }
}
