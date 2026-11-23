package com.tikitaka.domain.auth.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.tikitaka.global.common.ApiResponse;
import com.tikitaka.global.error.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

/**
 * 인증되지 않은 (로그인 정보 부재) 요청에 401 응답 반환
 * - 필터에서 거절된 요청은 GlobalExceptionHandler로 가지 않음
 * - Spring Secrity의 인증 예외 처리 과정에서 호출 (일반 예외나 비즈니스 예외는 처리하지 않음)
 *
 * AuthenticationEntryPoint 인터페이스
 * - Spring Security에서 제공하는 인증 실패 요청에 대한 응답 처리 정의 인터페이스
 * - commence() 인증 실패 시, 어떻게 응답할지 정의하는 메소드
 */
@Component
public class AuthEntryPoint implements AuthenticationEntryPoint {

    private final JsonMapper jsonMapper; // (객체 ↔ JSON 문자열) 변환기

    public AuthEntryPoint(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /** 401 실패 ApiResponse를 JSON로 응답 - 인증 실패시 호출
     * request : 요청 정보
     * response : 반환할 응답
     * authException: 인증 실패와 관련된 예외 정보 */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        // 401 실패 응답 구조 셋팅
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;                   //  에러 코드 설정
        response.setStatus(errorCode.getStatus().value());              //  HTTP 상태 코드 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);      // 응답 데이터 형식 설정
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());   // 문자 인코딩 설정

        // 401 실패 ApiResponse객체 JSON로 응답
        jsonMapper.writeValue(response.getWriter(), ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
    }
}
