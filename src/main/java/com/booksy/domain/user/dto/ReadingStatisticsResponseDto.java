package com.booksy.domain.user.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReadingStatisticsResponseDto {

  private String scope;  // "day", "week", "month"
  private List<ReadingStatisticsItemDto> data;
}
