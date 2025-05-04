package com.booksy.domain.user.dto;

import com.booksy.domain.user.entity.Gender;
import lombok.Getter;
import lombok.Setter;

/**
 * 내 정보 수정 요청 DTO
 */
@Getter
@Setter
public class UpdateUserRequest {

  private String nickname;            // 닉네임
  private String currentPassword;     // 현재 비밀번호
  private String newPassword;         // 새 비밀번호
  private Integer age;                // 나이
  private Gender gender;              // 성별
  // private List<Integer> preferredGenres; // 선호 장르
}
