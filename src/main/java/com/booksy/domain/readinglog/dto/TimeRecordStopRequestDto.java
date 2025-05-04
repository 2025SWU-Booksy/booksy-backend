package com.booksy.domain.readinglog.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 타이머 종료 요청 dto
 */
@Getter
@Setter
public class TimeRecordStopRequestDto {

  private int currentPage; // 사용자가 입력한 현재 페이지
}