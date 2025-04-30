package com.booksy.domain.readinglog.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 독후감 조회 결과 dto
 */
@Getter
@Builder
public class ReviewResponseDto {

  private Long id;
  private String content;
}
