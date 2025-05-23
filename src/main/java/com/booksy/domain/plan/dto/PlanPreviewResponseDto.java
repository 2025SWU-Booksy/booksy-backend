package com.booksy.domain.plan.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * 플랜 미리보기 결과 응답 DTO
 */
@Data
@Builder
public class PlanPreviewResponseDto {

  private String bookIsbn;
  private String title;
  private String author;
  private String publisher;
  private LocalDate publishedDate;
  private int totalPage;
  
  private Boolean isFreePlan;

  private int dailyPages;
  private int dailyMinutes;
  private int totalDurationDays;

  private List<LocalDate> readingDates;

  // 기간이 적절하지 않은 경우 플랜 기간 수정 유도
  private boolean tooLong;
  private int recommendedPeriodDays;
}
