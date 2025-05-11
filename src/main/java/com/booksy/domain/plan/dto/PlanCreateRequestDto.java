package com.booksy.domain.plan.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 플랜 생성 요청 DTO
 */
@Getter
@Setter
public class PlanCreateRequestDto {

  private String bookIsbn;
  private LocalDate startDate;
  private Integer periodDays;        // 목표 기간 (자유플랜이 아니라면 필수)
  private Boolean isFreePlan = false;
  private List<LocalDate> excludeDates;
  private List<Integer> excludeWeekdays; // 0:일요일 ~ 6:토요일
  private String description;

  // preview 결과 복사용 필드
  private List<LocalDate> readingDates;
  private Integer dailyMinutes;
  private Integer dailyPages;
}
