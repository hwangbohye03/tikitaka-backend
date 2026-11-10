package com.tikitaka.global.common;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * 해당 클래스를 상속한 엔티티 테이블은 생성 시각과 수정 시각 필드 자동 생성
 * 시각이 자동으로 채워지려면 JpaAuditingConfig의 @EnableJpaAuditing이 필요
 * - @EnableJpaAuditing : 감사 기능 활성화
 * - AuditingEntityListener : 감사 기능 처리
 *
 * BaseEntity는 독립 객체를 생성을 위한 클래스가 아니라,
 * 여러 자식 엔티티에서 공통으로 사용하는 필드와 기능을 제공하기 위한
 * 부모 클래스이므로 직접 객체를 생성하지 못하도록 추상 클래스로 선언
**/
@Getter
@MappedSuperclass // 부모 클래스의 필드를 자식 엔티티 테이블에 매핑 (부모 자체는 테이블 생성 X)
@EntityListeners(AuditingEntityListener.class)  // 엔티티 생성&수정 이벤트를 처리할 클래스 등록
public abstract class BaseEntity {

	/** 생성 시각 (수정 쿼리에 포함 X) */
	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	/** 수정 시각 (최초 생성부터 셋팅) */
	@LastModifiedDate
	@Column(nullable = false)
	private LocalDateTime updatedAt;
}
