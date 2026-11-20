package com.tikitaka.global.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.tikitaka.domain.auth.security.AuthEntryPoint;
import com.tikitaka.domain.auth.security.JwtAuthenticationFilter;

/**
 * Spring Security 보안 정책 설정 클래스
 * - HTTP 요청의 인증·인가를 위한 보안 정책 및 필터 설정
 *
 * HttpSecurity : http 보안 정책을 구성하는 클래스
 * SecurityFilterChain : 설정된 보안 정첵에 따라 요청을 처리하는 보안 필터 체인
 * JwtAuthenticationFilter : Access 토큰 기반 사용자 인증 필터
 * AuthEntryPoint : 인증 실패 (401) 응답 처리
**/

@Configuration
@EnableWebSecurity // 웹 보안 기능 활성화 (SecurityFilterChain을 통해 HTTP 요청의 인증&인가 규칙을 적용)
public class SecurityConfig {

	/** 비밀번호 암호화 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/** JWT 인증 필터의 서블릿 필터 등록 비활성화
	 * - JwtAuthenticationFilter와 같이 '@Component로 등록된 Filter 구현체'는 Spring Boot에 의해 서블릿 필터로 자동 등록 (==서블릿 필터 자동 등록 시 해당 메소드 불필요)
	 * - JWT 필터는 Spring Security Filter Chain에 등록해야 하므로, 서블릿 필터로 중복 등록되는 것을 방지
	 *
	 * [서블릿 필터 체인 구조]
	 *  - Servlet Filter Chain = (A 필터 → Spring Security Filter Chain → B 필터)
	 *  - Spring Security Filter Chain은 Servlet Filter Chain 내부에서 실행됨
	 * [Spring Security 필터로 등록 이유]
	 *  - JWT 필터는 인증 처리를 수행하므로, 보안 처리 흐름 안에서 일관되게 관리하는 것이 자연스러움
	 * [@Bean 메소드]
	 *  - Spring Boot가 서블릿 컨테이너 초기화 과정에서 @Bean 메소드가 반환한 FilterRegistrationBean을 인식해 설정에 따라 필터 등록 처리
	 */
	@Bean
	public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
		FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false); // 서블릿 컨테이너 자동 등록 비활성
		return registration;
	}

	/** Spring Security의 HTTP 보안 정책 구성
	 * - CORS 및 CSRF 설정
	 * - 세션 미사용(STATELESS)
	 * - JWT 인증 필터 (Auth 도메인) 등록
	 * - URL별 인증 및 인가 규칙 설정 (authorizeHttpRequests 설정 없이는 기본값으로 모든 API가 401)
	 * - 인증 실패 처리
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter,
			AuthEntryPoint authEntryPoint) throws Exception {
		// http 객체에 보안 정책 정의
		http
			// CORS지원 기능 활성화 (webConfig에서 등록한 corsConfigurationSource 빈으로 CORS 필터 적용)
			.cors(Customizer.withDefaults())
			// CSRF 비활성화 (쿠키기반 JWT 인증의 CSRF 위험을 줄이기 위해 SameSite=Lax 적용)
			.csrf(csrf -> csrf.disable())
			// HTTP 세션 미사용 (JWT 기반 Stateless 인증)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			// authenticationEntryPoint 구현체 지정 (인증 실패 시 AuthEntryPoint.commence 호출)
			.exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint))
			// jwt 인증 필터 등록 및 순서 설정
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			// URL별 접그 권한 설정 (swagger관련 api 외 접근은 로그인 필요)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
				.anyRequest().authenticated());

		// 보안 정책이 적용된 SecurityFilterChain 객체 생성
		return http.build();
	}
}