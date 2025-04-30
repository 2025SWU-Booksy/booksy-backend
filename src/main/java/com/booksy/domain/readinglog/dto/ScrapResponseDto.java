package com.booksy.domain.readinglog.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 전체 스크랩 + 특정 플랜 스크랩 조회 결과 dto
 */
@Getter
@Builder
public class ScrapResponseDto {

  private Long id;
  private String content;
  private String bookTitle;
  private String author;

}