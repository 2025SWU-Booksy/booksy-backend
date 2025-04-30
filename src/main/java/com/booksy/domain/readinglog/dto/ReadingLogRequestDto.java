package com.booksy.domain.readinglog.dto;

import com.booksy.domain.readinglog.type.ContentType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadingLogRequestDto {

  private Long planId;
  private ContentType contentType; // REVIEW or SCRAP
  private String content;
}