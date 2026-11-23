package com.tikitaka.domain.auth.security;

import com.tikitaka.domain.user.entity.UserRole;

import lombok.Getter;

/**
 * 이번 요청에서 인증된 회원
 * - 토큰 검증 성공 사용자를 스프링 시큐리티에 등록하려고
 *   JwtAuthenticationFilter가 만드는 값
 */
@Getter
public class LoginUser {

    private final Long userId;
    private final UserRole role;

    /** 인증에 성공한 회원 번호와 역할을 담는다 */
    public LoginUser(Long userId, UserRole role) {
        this.userId = userId;
        this.role = role;
    }
}