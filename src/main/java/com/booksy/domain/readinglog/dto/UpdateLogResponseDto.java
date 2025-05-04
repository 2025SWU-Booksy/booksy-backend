package com.booksy.domain.readinglog.dto;

import com.booksy.domain.readinglog.type.ContentType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateLogResponseDto {

  private Long id;
  private Long planId;
  private String content;
  private ContentType contentType;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}