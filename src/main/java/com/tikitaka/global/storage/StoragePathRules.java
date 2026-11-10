package com.tikitaka.global.storage;

import java.util.Set;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.tikitaka.global.error.BusinessException;
import com.tikitaka.global.error.ErrorCode;

/**
 * StorageService 구현체가 공통으로 사용하는 저장 경로 규칙 도구
 * 클라이언트가 보낸 파일명과 .. 경로는 저장 경로로 쓰지 않음
 * - 저장 경로 생성 (상대경로 기준)
 * - 상대 경로 정규화
 * - 확장자 추출
**/
final class StoragePathRules {
	// 허용 확장자 목록
	private static final Set<String> IMAGE_EXTENSIONS
			= Set.of("jpg", "jpeg", "png", "gif", "webp");

	// 규칙 모음이라 객체를 만들지 않는다
	private StoragePathRules() {
	}

	/** 저장 경로 생성 (상대경로 기준) */
	static String createStoredPath(MultipartFile file, String directory) {
		// 업로드 파일 비었는지 검사
		if (file == null || file.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		// 업로드 파일의 확장자 유효성 검증
		String extension = extensionOf(file.getOriginalFilename());
		// 저장 경로 생성(정규화 저장경로 + uuid)
		String newStoredPath = normalizeRelativePath(directory) + "/" + UUID.randomUUID() + "." + extension;
		return newStoredPath;
	}

	/** 상대 경로 정규화 */
	static String normalizeRelativePath(String storedPath) {
		// 경로 비었는지 검사
		if (storedPath == null || storedPath.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		// 정규화 검사를 위한 경로 순회
		String[] parts = storedPath.replace("\\", "/").split("/");
		StringBuilder normalizedRelativePath = new StringBuilder();
		for (String part : parts) {
			// 유효 경로 검사 (. ..을 통한 경로 이동 방지)
			if (part.isEmpty() || part.equals(".") || part.equals("..")) {
				throw new BusinessException(ErrorCode.INVALID_INPUT);
			}
			// 검사 통과 부분 경로를 상위 경로와 연결
			if (normalizedRelativePath.length() > 0) {
				normalizedRelativePath.append('/');
			}
			normalizedRelativePath.append(part);
		}
		// 검증 및 정규화된 상대 경로 반환
		return normalizedRelativePath.toString();
	}

	/** 확장자 반환 */
	private static String extensionOf(String originalFilename) {
		// 확장자가 있는지 검사
		if (originalFilename == null || !originalFilename.contains(".")) {
			throw new BusinessException(ErrorCode.UNSUPPORTED_FILE_TYPE);
		}
		// 확장자 소문자로 추출
		String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
		// 확장자 유효성 여부 검증
		if (!IMAGE_EXTENSIONS.contains(extension)) {
			throw new BusinessException(ErrorCode.UNSUPPORTED_FILE_TYPE);
		}
		return extension;
	}
}
