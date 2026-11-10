package com.tikitaka.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Swagger UI에 표시할 API 문서 기본 정보
 * [문서 화면] http://localhost:8080/swagger-ui.html
 * [API 명세] http://localhost:8080//v3/api-docs
 *
 * 인증 기능 도입 시 위 url 접근 안되면,
 * SecurityConfig에서 Swagger 관련 경로만 인증 없이 접근하도록 허용
 *
 * OpenAPI : API명세 를 작성하기 위한 표준 규격
 * API 표준 규격 = URL + HTTP 메서드 + 요청 파라미터 + 응답 형식
**/
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("Tikitaka API")
				.description("크리에이터-팬덤 이벤트 관리 API")
				.version("v1"));
	}
}
