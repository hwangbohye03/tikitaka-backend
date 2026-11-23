package com.tikitaka.domain.user.entity;

import com.tikitaka.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "user")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    // 이메일 (로그인 아이디용 / 중복 저장 불가)
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    // 비밀번호 (BCrypt 암호화 저장, 소셜 가입 시 NULL)
    @Column(name = "password", length = 255)
    private String password;

    // 닉네임
    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    // 회원 프로필 이미지
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    // 회원 권한
    @Builder.Default // 빌더 기반 생성 시에도 기본값 유지
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role = UserRole.ROLE_USER;

    // 소셜 로그인 유형 (자체 이메일 가입은 NONE)
    @Builder.Default // 빌더 기반 생성 시에도 기본값 유지
    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", nullable = false, length = 20)
    private SocialProvider socialProvider = SocialProvider.NONE;

    // 회원 상태 (서비스 탈퇴 여부)
    @Builder.Default // 빌더 기반 생성 시에도 기본값 유지
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;
}
