package com.booksy.domain.user.repository;

import com.booksy.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

  /**
   * 이메일로 유저 찾기 (회원가입 시 중복 확인, 로그인 시 인증 용도)
   */
  Optional<User> findByEmail(String email);

  /**
   * 소셜 로그인 시 provider + providerUserId로 유저 찾기 (선택사항)
   */
  Optional<User> findByProviderAndProviderUserId(String provider, String providerUserId);
}