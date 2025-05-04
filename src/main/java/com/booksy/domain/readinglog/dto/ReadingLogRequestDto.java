package com.booksy.domain.readinglog.dto;

import com.booksy.domain.readinglog.type.ContentType;
import lombok.Getter;
import lombok.Setter;

/**
 * 독서로그 생성 요청 dto
 */
@Getter
@Setter
public class ReadingLogRequestDto {

  private ContentType contentType; // REVIEW or SCRAP
  private String content;
}