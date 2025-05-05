package com.booksy.domain.plan.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanCalendarResponseDto {

  private List<PlanCalendarDayDto> days;
}
