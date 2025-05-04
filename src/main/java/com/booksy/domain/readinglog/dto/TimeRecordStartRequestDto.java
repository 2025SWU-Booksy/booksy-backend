package com.booksy.domain.readinglog.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 타이머 생성 요청 DTO
 */
@Getter
@Setter
public class TimeRecordStartRequestDto {

  private Long planId;
}