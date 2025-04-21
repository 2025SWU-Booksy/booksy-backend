package com.booksy.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 회원가입 처리 결과 dto
 */
@Getter
@AllArgsConstructor
public class SignupResponse {

  private int code;
  private String result;
  private String message;
}