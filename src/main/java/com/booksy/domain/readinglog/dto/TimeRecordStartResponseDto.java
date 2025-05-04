package com.booksy.domain.readinglog.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TimeRecordStartResponseDto {

  private Long timeRecordId;
  private LocalDateTime startTime;
}