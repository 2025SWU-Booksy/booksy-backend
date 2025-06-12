package com.booksy.domain.user.repository;

import com.booksy.domain.user.entity.Provider;
import com.booksy.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

  /**
   * 이메일로 유저 찾기 (회원가입 시 중복 확인, 로그인 시 인증 용도)
   */
  Optional<User> findByEmail(String email);

  /**
   * 소셜 로그인 시 provider + providerUserId로 유저 찾기
   */
  Optional<User> findByProviderAndProviderUserId(Provider provider, String providerUserId);

  // 푸시알림 설정한 유저 찾기
  List<User> findByIsPushEnabledTrue();

}