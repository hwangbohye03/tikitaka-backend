package com.tikitaka.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * BaseEntity의 생성 시각, 수정 시각을 자동으로 채우는 감사 기능 활성화 설정
 * 해당 설정이 없으면 @CreatedDate, @LastModifiedDate 필드에 값이 들어가지 않음
**/
@Configuration // 설정 클래스로 등록
@EnableJpaAuditing // 저장·수정 시 시각을 채우는 감사 기능 활성화(on)
public class JpaAuditingConfig { }
