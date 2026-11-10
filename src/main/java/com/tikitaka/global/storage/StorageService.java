package com.tikitaka.global.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 저장소 인터페이스 (업로드/삭제/조회)
 *
 * 파일 업로드 시, 저장 경로를 반환함
 * 파일 삭제/조회 시, 저장 경로를 사용
**/
public interface StorageService {

	/** 파일 업로드 - 기본 저장소 루트의 하위 디렉터리에 파일을 저장하고 저장 경로를 반환 */
	String upload(MultipartFile file, String directory); // (파일, 저장소 루트의 하위 디렉터리)

	/** 파일 삭제 - 저장 경로에 해당하는 파일을 삭제 (없는 경로여도 실패로 보지 않음) */
	void delete(String storedPath);

	/** URL 조회 - 저장 경로로 브라우저가 접근할 URL을 만듦 (파일 존재 여부는 확인하지 않음) */
	String getUrl(String storedPath);
}
