package com.booksy.domain.plan.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

/**
 * 메인 화면의 "오늘 읽을 책" 플랜 요약 정보 반환 dto
 */
@Data
@Builder
public class PlanSummaryResponseDto {

  private Long planId;
  private String bookTitle;
  private String author;
  private String imageUrl;
  private LocalDate startDate;
  private LocalDate endDate;
  private int currentPage;
  private int totalPage;
  private int progressRate; // 0~100 정수
  private String totalReadingTime; // 나중에 추가
}
