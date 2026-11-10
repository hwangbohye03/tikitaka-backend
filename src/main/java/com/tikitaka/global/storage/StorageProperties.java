package com.tikitaka.global.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * 저장소 관련 (파일 저장/조회)설정값을 하나의 클래스로 그룹화
 * 자주 사용하는 설정값을 Properties 클래스로 분리하여 관리 효율 및 재사용성 확보
 *
 * local : (디스크 경로 + 공개 URL) 사용
 * prod : (Firebase 버킷) 사용
**/
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.storage") // 설정 파일의 app.storage 설정값을 필드에 자동 바인딩
public class StorageProperties {

	/** local 프로필 */
	private String location; // 로컬 환경에서 파일을 저장할 경로
	private String publicBaseUrl; // 저장 파일에 접근할 기본 URL

	/** prod 프로필용 */
	private String bucket; // 파일을 저장할 Firebase Storage 버킷 이름
}
