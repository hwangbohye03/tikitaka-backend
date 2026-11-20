package com.tikitaka.domain.user.entity;

/**
 * 회원 상태
 * - 탈퇴는 soft delete 처리 (데이터 삭제 대신 DELETED로 표시)
 */
public enum UserStatus {
    ACTIVE,
    DELETED
}
