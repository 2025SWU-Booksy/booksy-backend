package com.booksy.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 랭킹 페이지에서 타 회원 정보 조회 dto
 */
@Getter
@Builder
@AllArgsConstructor
public class UserProfileResponse {

  private String nickname;
  private String profileImage;
  private int level;
  private String yesterdayReadingTime;  // 어제 독서 시간 (hh:mm:ss)
  private int totalCompletedBooks;      // 완독한 책 수 (COMPLETED 플랜 수)
}