package com.tikitaka.global.common;

import lombok.Getter;

/**
 * 모든 API의 공통 응답 규격
 * ApiResponse = body = {success + code + message + data}
 * ResponseEntity = {헤더 + HTTP 상태 코드 + 바디 (ApiResponse)}
	 * HTTP 상태 코드는 ApiResponse에 넣지 않고 ResponseEntity로 따로 내려줌
**/

@Getter
public class ApiResponse<T> {

	private final boolean success; 	// 성공 여부 (true/false)
	private final String code; 		// 예외 코드 (응답 실패시)
	private final String message; 	// 메세지
	private final T data;			// 응답 데이터

	/** 접근제한을 통해, 바깥에서 new로 직접 생성 방지 */
	private ApiResponse(boolean success, String code, String message, T data) {
		this.success = success;
		this.code = code;
		this.message = message;
		this.data = data;
	}
	
	/** (데이터) 성공 응답 */
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, "OK", "성공", data);
	}

	/** (메시지 + 데이터) 성공 응답 */
	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>(true, "OK", message, data);
	}

	/** (본문이 없는) 성공 응답 */
	public static ApiResponse<Void> success() {
		return new ApiResponse<>(true, "OK", "성공", null);
	}

	/** (에러 코드) 실패 응답 */
	public static <T> ApiResponse<T> fail(String code, String message) {
		return new ApiResponse<>(false, code, message, null);
	}
}
