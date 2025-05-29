package com.booksy.domain.plan.dto;

import com.booksy.domain.plan.type.PlanStatus;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanDetailResponseDto {

  private Long planId;

  // 책 정보
  private String bookTitle;
  private String author;
  private String publisher;
  private String publishedDate;
  private String imageUrl;
  private int totalPage;

  // 플랜 정보
  private LocalDate startDate;
  private LocalDate endDate;
  private int currentPage;
  private int dailyPages;
  private int dailyMinutes;
  private int progressRate;
  private PlanStatus status;
  private String totalReadingTime;
  private String todayReadingTime;

  private List<LocalDate> readingDates;
}
