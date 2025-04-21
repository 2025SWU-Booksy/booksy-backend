package com.booksy.domain.book.external.dto;

import lombok.Data;

@Data
public class AladinItemDto {

  private String title;
  private String author;
  private String publisher;
  private String pubDate;
  private String isbn13;
  private SubInfoDto subInfo;  // 응답 구조가 중첩 JSON
  private String cover;

}
