package com.booksy.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReadingStatisticsItemDto {

  private String label;        // 날짜 or 주 or 월 (예: "2025-05-01", "2025-05", "2025-05-05 ~ 05-11")
  private String averageMinutes; // 평균 시간 hh:mm
  private String formattedTime; // 총 시간 hh:mm
}