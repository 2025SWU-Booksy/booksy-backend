package com.booksy.domain.readinglog.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TimeRecordDetailResponseDto {

  // 개별 기록 리스트
  private List<TimeRecordItemDto> records;

  // 해당 날짜 총 소요 시간 (분)
  private int totalDuration;

  @Getter
  @AllArgsConstructor
  public static class TimeRecordItemDto {

    private String startTime;  // ex: "10:00"
    private String endTime;    // ex: "10:45"
    private int duration;      // 45
  }
}