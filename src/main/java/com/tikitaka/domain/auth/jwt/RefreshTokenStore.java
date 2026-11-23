package com.tikitaka.domain.auth.jwt;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis에서 Refresh Token을 관리
 * - 회원별 토큰 저장 : set()
 * - 저장된 토큰 조회 : get()
 * - 로그아웃 시 토큰 삭제 : delete()
 *
 * - Redis 저장 구조 EX
 *      Key   : auth:refresh:1
 *      Value : refresh-token-A
 *      TTL   : 14일
 *
 * - Refresh Token발급은 로그인 API에서 처리
 */
@Component
public class RefreshTokenStore {

    // Redis Key Prefix. 회원 ID를 붙여 회원별 Key 생성
    private static final String KEY_PREFIX = "auth:refresh:";
    // Redis에 (조회·저장·삭제)명령을 보내는 클라이언트
    private final StringRedisTemplate redisTemplate;
    // Refresh Token 만료 시간 (TTL 만료시 Redis가 키 자동 제거)
    private final Duration refreshExpiration;

    public RefreshTokenStore(StringRedisTemplate redisTemplate, JwtProperties properties) {
        this.redisTemplate = redisTemplate;
        this.refreshExpiration = properties.getRefreshExpiration();
    }

    /** 회원별 토큰 저장 - 같은 회원(key)이면 이전 토큰(vlaue,ttl)을 덮어씀 */
    public void save(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(key(userId), refreshToken, refreshExpiration);
    }

    /** 저장된 토큰 조회 */
    public Optional<String> get(Long userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(userId)));
    }

    /** 로그아웃 시 토큰 삭제 */
    public void delete(Long userId) {
        redisTemplate.delete(key(userId));
    }
    
    /** 회원별 Redis의 Refresh Token 접근 키 */
    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }
}
