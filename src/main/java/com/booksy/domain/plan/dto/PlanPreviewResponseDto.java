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

  private String title;
  private String author;
  private String publisher;
  private LocalDate publishedDate;
  private int totalPage;

  private int dailyRecommendedPages;
  private int dailyRecommendedMinutes;
  private int totalDurationDays;

  private List<LocalDate> readingDates;
}
