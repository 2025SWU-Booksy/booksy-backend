package com.booksy.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 마이페이지 내 정보 요약 조회
 */
@Getter
@Builder
@AllArgsConstructor
public class MyPageResponse {

  private String nickname;
  private String email;
  private String profileImage;
  private int level;
  private int badgeCount;
}