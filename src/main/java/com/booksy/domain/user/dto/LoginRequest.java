package com.booksy.domain.user.dto;

import lombok.Getter;

/**
 * 로그인 정보 요청 dto
 */
@Getter
public class LoginRequest {

  private String email;
  private String password;
}
