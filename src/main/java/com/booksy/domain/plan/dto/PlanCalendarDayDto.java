package com.booksy.domain.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlanCalendarDayDto {

  private int day;        // 해당 월의 날짜 (1~31)
  private boolean hasPlan; // 해당 날짜에 플랜이 있는지
}
