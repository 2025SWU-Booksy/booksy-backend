package com.booksy.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 로그인 처리 결과 dto
 */
@Getter
@AllArgsConstructor
public class LoginResponse {

  private int code;
  private String result;
  private String message;
  private String token; //로그인 성공하면 JWT 반환
}
