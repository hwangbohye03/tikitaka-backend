package com.tikitaka.global.storage;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

/**
 * prod 프로필용 Firebase 저장소 구현체
 *
 * Admin SDK와 서비스 계정 연동은 운영 환경에 연결할 때
 * 구현 예정 (그 전에 호출하면 실패)
 */
@Slf4j
@Service
@Profile("prod") // prod 프로필이 활성화된 환경에서만 빈 등록
public class FirebaseStorageService implements StorageService {

	/** 저장/삭제/조회 메소드 구현 - StorageService 인터페이스 메소드 오버라이딩 */
	@Override
	public String upload(MultipartFile file, String directory) {
		throw notReady(); // 구현 예정
	}

	@Override
	public void delete(String storedPath) {
		throw notReady(); // 구현 예정
	}

	@Override
	public String getUrl(String storedPath) {
		throw notReady(); // 구현 예정
	}


	/** 아직 지원/구현되지 않은 기능 호출 시 발생 예외 */
	private UnsupportedOperationException notReady() {
		return new UnsupportedOperationException("Firebase 저장소는 운영 연동 시 구현한다.");
	}
}
