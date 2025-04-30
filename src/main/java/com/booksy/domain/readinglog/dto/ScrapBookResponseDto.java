package com.booksy.domain.readinglog.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 도서별 스크랩 조회 결과 dto
 */
@Getter
@Builder
public class ScrapBookResponseDto {

  private String bookTitle;
  private String author;
  private String imageUrl;
  private int scrapCount; //한 도서의 총 스크랩 수
}
