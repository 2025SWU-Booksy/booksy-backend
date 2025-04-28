package com.booksy.domain.user.dto;

import com.booksy.domain.user.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 내 정보 조회 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class InfoResponse {

  private String email;
  private String nickname;
  private Integer age;
  private Gender gender;
  private String profileImage;

  // 선호 장르
  // private List<String> preferredGenres;
}
