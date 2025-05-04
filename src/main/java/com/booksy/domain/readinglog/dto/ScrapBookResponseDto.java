package com.booksy.domain.readinglog.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 도서별 스크랩 조회 결과 dto
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScrapBookResponseDto {

  private Long planId;
  private String bookTitle;
  private String author;
  private String imageUrl;
  private long scrapCount;
  private LocalDateTime latestScrap;
}


