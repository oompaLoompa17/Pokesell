package com.example.Pokemon_TCG_TEST.Utilities;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtUtil {
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    @Autowired
    @Qualifier("redis-0")
    private RedisTemplate<String, String> redisTemplate;

    public String generateToken(String email) {
        String token = Jwts.builder()
            .subject(email)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 86400000))
            .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
            .compact();
        redisTemplate.opsForValue().set("session:" + token, email, 24, TimeUnit.HOURS);
        return token;
    }

    public String extractEmail(String token) {
        String email = redisTemplate.opsForValue().get("session:" + token);
        if (email == null) {
            throw new RuntimeException("Invalid or expired session");
        }
        return email;
    }

    public boolean validateToken(String token) {
        return redisTemplate.hasKey("session:" + token);
    }

    public void invalidateToken(String token) {
        redisTemplate.delete("session:" + token);
    }
}