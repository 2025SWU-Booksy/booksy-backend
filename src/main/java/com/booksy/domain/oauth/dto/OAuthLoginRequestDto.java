package com.booksy.domain.oauth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 로그인 요청 dto
 */
@Getter
@NoArgsConstructor
public class OAuthLoginRequestDto {

  private String accessToken;
}