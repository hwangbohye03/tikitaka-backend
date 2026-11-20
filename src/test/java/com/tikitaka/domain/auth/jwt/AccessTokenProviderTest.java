package com.tikitaka.domain.auth.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.tikitaka.domain.auth.jwt.AccessTokenProvider.AccessTokenInfo;
import com.tikitaka.domain.user.entity.UserRole;

/**
 * Access Token _ 단위 테스트
 */
class AccessTokenProviderTest {
    // 테스트용 JWT 서명 검증 키
    private static final String SECRET = "test-only-jwt-secret-do-not-use-in-prod";

    /** 정상 토큰 검증 테스트
     * - 정상 토큰에서 회원 번호와 역할이 그대로 나오는지 확인 */
    @Test
    void validTokenKeepsUserIdAndRole() {
        // 작업 객체 생성
        AccessTokenProvider provider = prepareProvider(Duration.ofMinutes(30));

        // 토큰 생성
        String token = provider.createAccessToken(1L, UserRole.ROLE_USER);

        Optional<AccessTokenInfo> parsed = provider.parseAccessToken(token);
        assertTrue(parsed.isPresent());
        assertEquals(1L, parsed.get().userId());
        assertEquals(UserRole.ROLE_USER, parsed.get().role());
    }

    /** 비정상 토큰 검증 테스트
     * - 서명이 깨진 토큰은 빈 값으로 거절하는지 확인 */
    @Test
    void tamperedTokenIsEmpty() {
        // 작업 객체 생성
        AccessTokenProvider provider = prepareProvider(Duration.ofMinutes(30));

        // 토큰 생성 및 오염(기존 토큰의 끝 문자만 변경)
        String token = provider.createAccessToken(1L, UserRole.ROLE_USER);
        String tamperedToken = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");

        assertTrue(provider.parseAccessToken(tamperedToken).isEmpty());
    }

    /** 만료 토큰 검증 테스트
     * 만료된 토큰은 빈 값으로 거절하는지 확인 */
    @Test
    void expiredTokenIsEmpty() throws InterruptedException {
        // 작업 객체 생성
        AccessTokenProvider provider = prepareProvider(Duration.ofMillis(1));

        // 토큰 생성 및 만료 대기
        String token = provider.createAccessToken(1L, UserRole.ROLE_USER);
        Thread.sleep(5); // InterruptedException 대비

        assertTrue(provider.parseAccessToken(token).isEmpty());
    }

    /** AccessTokenProvider 준비
     *  - new로 실제 객체를 생성
     *      - 실제 JWT 생성 및 검증 기능을 테스트해야 하므로, mock으로 모의 객체 생성하지 않음
     *      - 스프링 컨테이너를 실행하지 않아 자동 주입을 사용하지 않음
     *  - 테스트 목적에 따른 만료 시간 (jwt)설정 (정상 토큰, 만료 토큰)
     * */
    private AccessTokenProvider prepareProvider(Duration accessExpiration) {
        // 테스트용 JwtProperties 셋팅
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setAccessExpiration(accessExpiration);

        // 테스트용 JwtProperties 적용한 AccessTokenProvider 객체 반환
        return new AccessTokenProvider(properties);
    }
}
