package com.booksy.domain.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 내 랭킹 정보를 반환하는 DTO - 정렬 기준/범위에 따른 내 순위 및 상세 정보 - 퍼센트는 같은 조건 기준의 사용자 집합에서 상위 퍼센트
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyRankingResponseDto {

  private int rank;               // 내 랭킹 (1위부터, 없으면 -1)
  private String nickname;        // 닉네임
  private String profileImage;    // 프로필 이미지 URL
  private int level;              // 사용자 레벨
  private String value;           // 정렬 기준 값 (ex: "10시간 20분", "35권", "12개")
  private String valueType;       // 정렬 기준 타입 ("time", "count", "badge")
  private double percentile;      // 상위 퍼센트 (ex: 3.2 → 상위 3.2%)
}
