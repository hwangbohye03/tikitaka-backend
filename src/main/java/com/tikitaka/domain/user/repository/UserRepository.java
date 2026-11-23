package com.tikitaka.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tikitaka.domain.user.entity.User;
import com.tikitaka.domain.user.entity.UserStatus;

public interface UserRepository extends JpaRepository<User, Long> {
    /** (이메일 + 상태)기반 회원 한 명 조회*/
    Optional<User> findByEmailAndStatus(String email, UserStatus status);
}
