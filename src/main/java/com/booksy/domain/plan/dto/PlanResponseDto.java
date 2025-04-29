package com.booksy.domain.plan.dto;

import com.booksy.domain.plan.type.PlanStatus;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

/**
 * 플랜 저장 후 요약 응답 DTO
 */
@Data
@Builder
public class PlanResponseDto {

  private Long id;
  private String bookTitle;
  private String imageUrl;
  private PlanStatus status;
  private LocalDate startDate;
  private LocalDate endDate;
}
