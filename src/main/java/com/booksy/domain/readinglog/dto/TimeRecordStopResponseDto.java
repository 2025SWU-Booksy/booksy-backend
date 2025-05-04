package com.booksy.domain.readinglog.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TimeRecordStopResponseDto {

  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private int duration;
  private int updatedCurrentPage;
}