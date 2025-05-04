package com.booksy.domain.readinglog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TimeRecordResponseDto {

  // 총 독서 시간 (hh:mm:ss 형식 문자열)
  private String totalDuration;

  // 오늘 독서 시간 (hh:mm:ss 형식 문자열)
  private String todayDuration;
}
