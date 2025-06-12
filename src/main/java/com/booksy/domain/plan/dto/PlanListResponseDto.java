package com.booksy.domain.plan.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanListResponseDto {

  private Long planId;
  private String isbn;
  private String title;
  private String author;
  private String publisher;
  private String imageUrl;

  // READING / COMPLETED 공통
  private LocalDate startDate;
  private LocalDate endDate;

  // READING 전용
  private Integer todayIndex;      // null 가능
  private Integer progressPercent; // null 가능

  // COMPLETED 전용
  private Integer rating;          // null 가능
  private Integer scrapCount;      // null 가능
}