package com.tikitaka.global.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.tikitaka.global.error.BusinessException;
import com.tikitaka.global.error.ErrorCode;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * local 프로필용 로컬 저장소 구현체
 * - StorageService 인터페이스 : 파일 저장·삭제·URL 조회 기능
 * - WebMvcConfigurer 인터페이스 : Spring MVC 설정 확장 기능
 *
 * StorageProperties기반 파일 저장과 url 접근 설정
 *
 * 파일 저장 상대경로 : directory/file.png
 * 파일 저장 절대경로 : root경로/directory/file.png
 *
**/
@Slf4j
@Service
@Profile("local") // local 프로필이 활성화된 환경에서만 빈 등록
@RequiredArgsConstructor
public class LocalStorageService implements StorageService, WebMvcConfigurer {

	private final StorageProperties properties; // 저장소 설정값 관리 객체
	private Path root; // 저장소 루트 절대경로
	private String baseUrl; // 브라우저 접근 기본 주소

	/** 초기화 - 애플리케이션 시작 시, 저장 경로 절대 경로화 및 해당 디렉터리 없으면 생성 */
	@PostConstruct // 의존성 주입 완료 이후 메소드 자동 실행
	void init() {
		// 설정값 유효성 검사 (저장 경로 + 접근 url)
		if (isBlank(properties.getLocation()) || isBlank(properties.getPublicBaseUrl())) {
			throw new IllegalStateException(
					"local 프로필에는 app.storage.location과 app.storage.public-base-url이 필요합니다.");
		}

		// 브라우저 접근 주소 양식 통일
		String publicBaseUrl = properties.getPublicBaseUrl();
		if (publicBaseUrl.endsWith("/")) { // 주소 끝 (/) 제거
			publicBaseUrl = publicBaseUrl.substring(0, publicBaseUrl.length()-1);
		}
		baseUrl = publicBaseUrl;

		// 저장 루트 경로 절대경로화 및 디렉터리 생성
		try {
			root = Path.of(properties.getLocation()).toAbsolutePath().normalize(); // 절대경로화 + 경로 정규화 + Path 객체 변환
			Files.createDirectories(root); // root 경로 디렉토리 생성
		} catch (IOException e) {
			throw new IllegalStateException(
					"로컬 저장 디렉터리를 만들 수 없다. location=" + properties.getLocation(), e);
		}
	}


	/** 저장/삭제/조회 메소드 구현 - StorageService 인터페이스 메소드 오버라이딩 */
	@Override
	public String upload(MultipartFile file, String directory) {
		// 같은 저장 경로에 이미 파일 존재하면 저장 경로 재생성
		int maxAttempts = 3;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			String newStoredRelativePath = StoragePathRules.createStoredPath(file, directory); // 파일 저장 상대경로 (생성)
			Path newStoredAbsolutePath = resolveUnderRoot(newStoredRelativePath);  // 파일 저장 절대경로

			// 파일 저장
			try {
				// 파일 저장 디렉터리 생성 (존재하지 않는 상위 디렉터리까지 재귀적 생성)
				Files.createDirectories(newStoredAbsolutePath.getParent());
				// 파일 입력 스트림 생성 및 파일 저장
				try (InputStream input = file.getInputStream()) {
					Files.copy(input, newStoredAbsolutePath); // 입력스트림으로 파일을 읽어, 절대 경로에 저장
				}
				// 저장 파일 상대 경로 반환
				return newStoredRelativePath;

			} catch (FileAlreadyExistsException e) { // copy() 처리 시, 해당 경로 이미 파일 존재하면 발생
				log.warn("저장 경로가 이미 있음. path={}, attempt={}/{}", newStoredRelativePath, attempt, maxAttempts);
			} catch (IOException e) {
				log.error("파일 저장 실패. path={}", newStoredRelativePath, e);
				throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
			}
		}

		// 경로 재생성 시도 3회 초과 시, 예외 처리
		log.error("저장 경로 중복 {}회로 저장에 실패함", maxAttempts);
		throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
	}

	@Override
	public void delete(String storedPath) {
		// 저장된 파일의 절대경로 (상대경로 정규화 + 절대경로화)
		Path storedAbsolutePath = resolveUnderRoot(StoragePathRules.normalizeRelativePath(storedPath));

		// 파일 삭제
		try {
			Files.deleteIfExists(storedAbsolutePath); // (파일 부재시 오류 없음)
		} catch (IOException e) {
			log.error("파일 삭제 실패. path={}", storedPath, e);
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public String getUrl(String storedPath) {
		// 저장된 파일의 상대경로 (상대 경로 정규화)
		String StoredRelativePath = StoragePathRules.normalizeRelativePath(storedPath);
		// 파일 접근 url 생성
		return baseUrl + "/" + StoredRelativePath;
	}


	/** 브라우저 접근을 위한 정적 리소스 경로 등록 - WebMvcConfigurer 인터페이스 메소드 오버라이딩 */
	// 애플리케이션 초기화 시 정적 리소스 요청 경로(url)와 실제 저장 위치(로컬 저장소) 매핑
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) { // registry는 정적 리소스 요청 처리 매핑 규칙 등록 객체
		// baseUrl에서 경로 추출 (ex: http://localhost:8080/files → /files)
		String publicPath = URI.create(baseUrl).getPath();

		// 실제 저장소 루트를 URI로 변환 (ex: C:/.../uploads → file:///C:/.../uploads)
		String location = root.toUri().toString();
		// location 정규화
		if (!location.endsWith("/")) {
			location = location + "/";
		}

		// url요청을 로컬 저장소로 연결 (/files/** → /uploads/**)
		registry.addResourceHandler(publicPath + "/**").addResourceLocations(location);
	}


	/** 절대경로 변환 - 루트 저장소 기준 */
	private Path resolveUnderRoot(String storedPath) {
		// 저장 경로 비었는지 검사
		if (isBlank(storedPath)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		// 상대경로 정규화
		String storedRelativePath = storedPath.replace("\\", "/");
		if (storedRelativePath.startsWith("/") || storedRelativePath.contains("..")) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		// 절대경로화
		Path storedAbsolutePath = root.resolve(storedRelativePath).normalize();
		if (!storedAbsolutePath.startsWith(root) || storedAbsolutePath.equals(root)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		return storedAbsolutePath;
	}


	/** 빈 문자열 검사 - 문자열이 비었는지 확인 */
	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
