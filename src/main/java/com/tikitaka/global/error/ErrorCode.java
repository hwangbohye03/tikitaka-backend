package com.tikitaka.global.error;

import org.springframework.http.HttpStatus;
import lombok.Getter;

/**
 * 실패 응답 목록 (HTTP 상태, 에러 코드, 메시지)
**/
@Getter
public enum ErrorCode {
	/** 공통 에러 enum 상수 선언 - 도메인 에러는 기능 구현 시 아래에 추가 */
		//	입력값 오류 400
		//	로그인 안 된 상태 401
		//	권한 없음 403
		//	대상 없음 404
		//	매진이나 중복처럼 요청이 현재 상태와 충돌하면 409
		//	서버 버그처럼 예상 밖 실패 500
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "입력값이 올바르지 않습니다."),
	UNSUPPORTED_FILE_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_FILE_TYPE", "지원하지 않는 파일 형식입니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "권한이 없습니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "대상을 찾을 수 없습니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");


	private final HttpStatus status; // ResponseEntity에 넣을 HTTP 상태
	private final String code;       // ApiResponse.code. 프론트 분기용
	private final String message;    // ApiResponse.message. 사용자에게 보여줄 문장

	/** enum 생성자는 private만 가능 (상수 선언 시 한 번만 호출됨) */
	private ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}
}
