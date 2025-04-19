package com.booksy.domain.book.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BookResponseDto {

  private String isbn;
  private String title;
  private String author;
  private String publisher;
  private LocalDate publishedDate;
  private int totalPage;
  
}
