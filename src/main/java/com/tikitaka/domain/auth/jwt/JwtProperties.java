package com.tikitaka.domain.auth.jwt;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * JWT 발급에 쓰는 설정값
 * - 서명 비밀키 , Access/Refresh Token 만료시간, Access Token 쿠키명
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt") // 설정 파일의 prefix 필드에 자동 바인딩
public class JwtProperties {

    /** HS256 서명 키 (32바이트 이상) */
    private String secret;

    /** Access Token 만료 시간 */
    private Duration accessExpiration;

    /** Refresh Token 만료 시간 (Redis 저장 시 사용) */
    private Duration refreshExpiration;

    /** Access Token을 담는 쿠키 이름 */
    private String accessCookieName;
}
