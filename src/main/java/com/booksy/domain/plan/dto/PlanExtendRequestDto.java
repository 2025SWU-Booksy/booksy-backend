package com.booksy.domain.plan.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * 플랜 기간 연장 요청 DTO
 */
@Getter
@Setter
public class PlanExtendRequestDto {

  @NotNull(message = "새 종료일은 필수입니다.")
  private LocalDate newEndDate;
}
