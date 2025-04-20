package com.booksy.domain.user.dto;

import com.booksy.domain.user.entity.Gender;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

  private String email;
  private String password;

  private Integer age;  //nullable
  private Gender gender;  //nullable

  private String nickname;  // nullable → email로 대체
  private String profileImage;  // nullable

  private List<Integer> preferredCategoryIds;  // nullable
}