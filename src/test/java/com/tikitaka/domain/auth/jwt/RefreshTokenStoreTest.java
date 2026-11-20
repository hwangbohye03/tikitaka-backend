package com.tikitaka.domain.auth.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.context.annotation.Import;

/**
 * Refresh Token _ 통합 테스트
 * - Refresh Token이 Redis에 회원별로 저장되는지 확인
 * - 로컬 Redis(localhost:6379) 연결되어야 함 (컨테이너 활성)
 * - @SpringBootTest이 아닌 @DataRedisTest로 컴포넌트 스캔이 (Redis 관련 기능 위주 로드) 제한적이므로, 필요 Bean 직접 로드
 */
@DataRedisTest // Redis 연결 테스트 (Redis 관련 기능만 로드)
@Import(RefreshTokenStore.class) // Spring 컨테이너에 Bean으로 로드
@EnableConfigurationProperties(JwtProperties.class) // Spring 컨테이너에 설정 Bean으로 로드 (main의 활성 설정 파일을 기준 적용)
class RefreshTokenStoreTest {
    // 테스트용 user.id
    private static final Long USER_ID = 9_000_000_000_001L;
    // 스프링 컨테이너에 등록한 RefreshTokenStore Bean 주입
    @Autowired
    private RefreshTokenStore refreshTokenStore;

    /** 테스트 종료 - 테스트가 남긴 Redis 키 제거 */
    @AfterEach
    void deleteToken() {
        refreshTokenStore.delete(USER_ID);
    }

    /** 저장된 토큰 조회
     * 저장한 토큰을 같은 회원 번호로 다시 조회할 수 있는지 확인 */
    @Test
    void savedTokenCanBeRead() {
        refreshTokenStore.save(USER_ID, "refresh-token-A");

        assertEquals("refresh-token-A", refreshTokenStore.get(USER_ID).orElseThrow());
    }

    /** 동일 회원 다른 토큰 재저장 테스트
     * 같은 회원에게 새 토큰을 다시 저장하면 이전 토큰이 새 토큰으로 바뀌는지 확인 */
    @Test
    void secondSaveTokenReplacesPreviousToken() {
        refreshTokenStore.save(USER_ID, "refresh-token-A");
        refreshTokenStore.save(USER_ID, "refresh-token-B");

        assertEquals("refresh-token-B", refreshTokenStore.get(USER_ID).orElseThrow());
    }

    /** 삭제 토큰 조회 테스트
     * 삭제한 토큰은 조회되지 않는지 확인 */
    @Test
    void deletedTokenIsEmpty() {
        refreshTokenStore.save(USER_ID, "refresh-token-A");
        refreshTokenStore.delete(USER_ID);

        assertTrue(refreshTokenStore.get(USER_ID).isEmpty());
    }
}
