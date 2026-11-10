package com.tikitaka.global.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS설정 : 서로 다른 출처 간 브라우저의 API 요청 허용 설정
 * SecurityConfig에서 기본 cors를 활성화하면, corsConfigurationSource 이름의 빈을 찾아 cors 필터링을 처리하게 함
**/
@Configuration
public class WebConfig {

	@Bean
	public CorsConfigurationSource corsConfigurationSource(
			@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) { // 설정 파일에 명시한 값을 인자로 전달

		/** 허용 cors 규칙 정의 */
        CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(allowedOrigins); // 요청 허용할 주소 (자격증명 활성화 시 * 불가)
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*")); // 허용할 요청 헤더 (*는 Host, Content-Type등 헤더의 모든 항목 의미)
		config.setAllowCredentials(true); // 쿠키, 인증 헤더 등 자격증명 포함한 요청 허용
		config.setMaxAge(3600L); // cors 사전 요청(OPTIONS) 결과를 1시간 보관 (동일한 조건의 요청은 캐싱된 결과를 재사용하여 OPTIONS 요청 생략 가능)

		/** API 경로에 CORS 규칙 적용 */
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config); // 모든 API 경로에 같은 규칙 적용

        return source;
	}
}
