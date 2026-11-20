package com.tikitaka.domain.auth.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.tikitaka.domain.user.entity.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Access Token 발급과 검증
 * - (userId, role)이 담긴 Access Token 생성
 * - 토큰에서 (userId, role) 추출
 *
 * - 토큰 구조 EX
 *      {
 *          "sub": "1",
 *          "role": "ROLE_USER",
 *          "iat": 0000000000,
 *          "exp": 0000000000
 *      }
 */
@Component
public class AccessTokenProvider {

    private final SecretKey key;             // JWT 서명,검증 키
    private final Duration accessExpiration; // Access Token 만료 시간

    public AccessTokenProvider(JwtProperties properties) { // JwtProperties기반 설정값 셋팅
        this.key = createKey(properties.getSecret());
        this.accessExpiration = properties.getAccessExpiration();
    }


    /** 검증에 성공한 Access Token의 회원 정보
     *  - 검증 성곤 토큰에서 추출한 (userId, role)  */
    public record AccessTokenInfo(Long userId, UserRole role) { }


    /** Access Token 생성 */
    public String createAccessToken(Long userId, UserRole role) {
       // 회원 정보 부족한 경우 예외 처리
        if (userId == null || role == null) {
            throw new IllegalArgumentException("userId와 role은 필수입니다.");
        }

        // 토큰 생성
        Instant now = Instant.now(); // 현재 시각
        return Jwts.builder()
                .subject(String.valueOf(userId))                    // 토큰 주체
                .claim("role", role.name())                   // 토큰 Payload
                .issuedAt(Date.from(now))                           // 발급 시각
                .expiration(Date.from(now.plus(accessExpiration)))  // 만료 시각
                .signWith(key)                                      // 토큰 서명
                .compact();                                         // 최종 jwt 압축&직렬화
    }

    /** Access Token 검증 및 회원 정보 추출 */
    public Optional<AccessTokenInfo> parseAccessToken(String token) {
        // 토큰 존재 여부 확인
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        try {
            // JWT 검증 및 데이터 추출
            Claims claims = Jwts.parser()       // JWT 파서 생성
                    .verifyWith(key)            // 검증 키 설정
                    .build()                    // 파서 객체 생성
                    .parseSignedClaims(token)   // JWT 파싱 및 검증
                    .getPayload();              // 검증 성공 시 payload 반환

            // 회원 정보 추출
            Long userId = Long.valueOf(claims.getSubject()); // 값 없으면 예외
            String roleName = claims.get("role", String.class);
            if (roleName == null) {
                return Optional.empty();
            }

            //회원 정보 반환
            return Optional.of(new AccessTokenInfo(userId, UserRole.valueOf(roleName)));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** JWT 서명 및 검증에 사용할 키 생성
     * - secret 문자열을 JWT 서명용 SecretKey 객체로 변환
     * - HS256 키는 32바이트보다 짧으면 만들 수 없음 */
    private static SecretKey createKey(String secret) {
        // secret 필드 유효성 검사
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret은 32바이트 이상이어야 합니다.");
        }
        // SecretKey 생성&반환
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
