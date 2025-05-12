package com.booksy.domain.oauth.dto;

import com.booksy.domain.user.entity.Gender;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 회원가입 요청 dto accessToken + 추가 사용자 정보
 */
@Getter
@NoArgsConstructor
public class OAuthSignupRequestDto {

  private String accessToken;
  private String nickname;
  private Gender gender;
  private Integer age;
  private String profileImage;
  private List<Integer> preferredCategoryIds;
}