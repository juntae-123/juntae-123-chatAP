package com.example.chat.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    private String secret = "my-very-secure-secret-key-for-jwt";
    private SecretKey secretKey;

    private final long validTime = 1000 * 60 * 60; // 1시간
    private final long refreshTokenValidTime = 1000 * 60 * 60 * 24 * 7; // 7일

    @PostConstruct
    public void init() {

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    public String createToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validTime))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }


    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("유효성 검증 실패: " + e.getMessage());
            return false;
        }
    }
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public String createRefreshToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenValidTime))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }


}
