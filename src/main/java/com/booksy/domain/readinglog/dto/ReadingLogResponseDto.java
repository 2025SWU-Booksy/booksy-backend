package com.booksy.domain.readinglog.dto;

import com.booksy.domain.readinglog.type.ContentType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 리딩로그 생성 결과 응답 dto
 */
@Getter
@Builder
public class ReadingLogResponseDto {

  private Long id;
  private Long planId;
  private ContentType contentType;
  private String content;
  private LocalDateTime createdAt;
}