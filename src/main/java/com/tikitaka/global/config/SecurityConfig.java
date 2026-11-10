package com.tikitaka.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * CSRF 보호 비활성화 + 세션 생성 X
 * JWT 필터는 인증 도메인에서 붙임
 * authorizeHttpRequests 설정 없으면 기본값 때문에 모든 API가 401이 됨
 *
 * 스프링 시큐리티는 세션 없는 CSRF를 막지는 않지만,
 * 기본 CSRF는 세션에 토큰을 저장하므로 STATELESS(세션 미사용)와 맞지 않고,
 * 쿠키 기반 토큰 인증의 CSRF 방어는 인증 쿠키의 SameSite=Lax로 보호
 *
 * HttpSecurity : 보안 설정을 구성하기 위한 도구
 * SecurityFilterChain : 설정이 완료된 보안 필터 체인
**/
@Configuration
@EnableWebSecurity // 웹 보안 기능 활성화 (SecurityFilterChain을 통해 HTTP 요청의 인증&인가 규칙을 적용)
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// 보안 정책을 설정하는 객체로, 보안 정책 정의
		http
			.cors(Customizer.withDefaults()) // CORS지원 기능 활성화 (webConfig에서 등록한 corsConfigurationSource 빈으로 CORS 필터 적용)
			.csrf(csrf -> csrf.disable()) // CSRF 비활성화 (쿠키 기반 토큰 인증의 CSRF 방어는 인증 쿠키 SameSite=Lax로 처리)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // HTTP 세션 사용 X
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // 현재 인증기능 도입 전이으로, 모든 API 접근 허용

		// 보안 정책이 적용된 SecurityFilterChain 객체 생성
		return http.build();
	}
}