package com.booksy.domain.book.recommendation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookGptResponseDto {

  private String title;
  private String author;
  private String isbn;
}
