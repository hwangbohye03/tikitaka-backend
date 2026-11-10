package com.tikitaka.global.error;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tikitaka.global.common.ApiResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 컨트롤러 밖으로 나온 예외를 ApiResponse 실패 응답으로 변환한다.
 * HTTP 상태는 ResponseEntity에 담고, 본문은 ApiResponse로 만든다.
**/
@Slf4j
@RestControllerAdvice // 필터를 통과한 뒤, 스프링 MVC가 컨트롤러를 호출하는 과정에서 나온 예외를 처리
public class GlobalExceptionHandler {

	/** 서비스가 던진 BusinessException을 실패 응답으로 변환 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
		return toResponse(e.getErrorCode());
	}

	/** @Valid 검증 실패를 INVALID_INPUT 응답으로 변환 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
		ErrorCode errorCode = ErrorCode.INVALID_INPUT;
		String message = errorCode.getMessage();

		// 필드 오류 문장이 있으면, INVALID_INPUT의 기본 message를 대체
			// getBindingResult() : 요청을 DTO에 넣고 검사한 결과
			// getFieldError() : 결과에서 특정 필드의 첫번째 오류 (제목 누락 시 "제목 필드 오류", 오류 없으면 null)
			// getDefaultMessage() : 오류의 문장 (@NotBlank(message = "제목은 필수입니다")처럼 적었다면 그 문장)
		if (e.getBindingResult().getFieldError() != null
				&& e.getBindingResult().getFieldError().getDefaultMessage() != null) {
			message = e.getBindingResult().getFieldError().getDefaultMessage();
		}
		// 대체 메세지일 가능성 존재하므로, toResponse대신 직접 응답 생성 반환
		return ResponseEntity
				.status(errorCode.getStatus())
				.body(ApiResponse.fail(errorCode.getCode(), message));
	}

	/** 컨트롤러까지 들어온 인증 실패만 401로 변환 (컨트롤러로 들어가기 전 필터에서 거절된 요청은 여기 오지 않음) */
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
		return toResponse(ErrorCode.UNAUTHORIZED);
	}

	/** 컨트롤러까지 들어온 권한 실패만 403으로 변환 (컨트롤러로 들어가기 전 필터에서 거절된 요청은 여기 오지 않음) */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
		return toResponse(ErrorCode.FORBIDDEN);
	}

	/** 그 외 처리하지 못한 예외 고통 500으로 고정 변환 (원인은 서버 로그에만 남김) */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
		log.error("처리하지 못한 예외", e); // log 변수는 @Slf4j가 만들어준 것
		return toResponse(ErrorCode.INTERNAL_SERVER_ERROR);
	}

	/** ErrorCode(HTTP 상태, 코드, 메시지)로 실패 응답 생성 */
	private ResponseEntity<ApiResponse<Void>> toResponse(ErrorCode errorCode) {
		return ResponseEntity
				.status(errorCode.getStatus())
				.body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
	}
}
