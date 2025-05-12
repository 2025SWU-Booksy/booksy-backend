package com.booksy.domain.oauth.dto;

import com.booksy.domain.user.entity.Provider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 로그인에서 얻은 사용자 정보
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuthUserInfo {

  private String email;          // 이메일
  private String providerUserId; // 소셜 제공자의 고유 ID
  private Provider provider;     // 제공자 타입 (GOOGLE, NAVER, KAKAO)
}