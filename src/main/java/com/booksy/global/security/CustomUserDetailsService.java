package com.booksy.global.security;

import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.entity.UserStatus;
import com.booksy.domain.user.repository.UserRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * JWT 인증 시 userId로 사용자 정보를 로드하는 서비스
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService {

  private final UserRepository userRepository;

  /**
   * JWT 토큰에서 userId를 추출해서 사용자 인증 정보를 반환
   */
  public UserDetails loadUserById(Integer userId) throws UsernameNotFoundException {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

    if (user.getStatus() != UserStatus.ACTIVE) {
      throw new UsernameNotFoundException("비활성화된 계정입니다");
    }

    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getId().toString())
        .password(user.getPassword())
        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        .build();
  }
}
