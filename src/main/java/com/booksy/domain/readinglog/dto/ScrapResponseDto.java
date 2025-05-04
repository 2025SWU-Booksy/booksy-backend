package com.booksy.domain.readinglog.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 스크랩 조회 결과 dto
 */
@Getter
@Builder
public class ScrapResponseDto {

  private Long id;                      // 로그 ID
  private String content;              // 스크랩 내용
  private String bookTitle;            // 책 제목
  private String author;               // 책 저자
  private LocalDateTime readingDate;   // 생성일 (스크랩한 날짜)

}