package com.tikitaka.global.error;

import lombok.Getter;

/**
 * 비즈니스 규칙 위반 시 던지는 예외 (서비스 예외)
 * 서비스에서는 throw new BusinessException(ErrorCode.XXX)만 사용
 *
 * RuntimeException(unchecked 예외) 상속 이유
 * - 호출부 메서드마다 throws를 붙이지 않아도 됨
 * - 예외가 트랜잭션 메서드 밖으로 나가면 롤백됨
**/
@Getter
public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;

	/** 응답에 쓸 (상태, 코드, 메시지)는 errorCode에 들어 있음 */
	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
