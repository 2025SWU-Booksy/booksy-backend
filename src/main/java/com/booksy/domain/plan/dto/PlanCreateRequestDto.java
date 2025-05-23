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

  // 도서 및 기본 설정
  private String bookIsbn;
  private LocalDate startDate;
  private Integer periodDays;
  private Boolean isFreePlan;

  // 제외 날짜/요일
  private List<LocalDate> excludeDates;
  private List<Integer> excludeWeekdays; // 0:일요일 ~ 6:토요일

  // 미리보기 결과 복사용 필드
  private List<LocalDate> readingDates;
  private Integer dailyMinutes;
  private Integer dailyPages;

  // 추천 일정 사용 여부 및 추천 값
  private Boolean useRecommendedPlan;
  private Integer recommendedPeriodDays;
}
