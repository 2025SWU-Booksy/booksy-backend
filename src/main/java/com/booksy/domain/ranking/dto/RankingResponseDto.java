package com.booksy.domain.ranking.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingResponseDto {

  private Integer userId;
  private int rank;
  private String nickname;
  private String profileImage;
  private String value;      // 문자열 ("35권", "10시간 5분", "12개")
  private String valueType;  // "time", "count", "badge"
}