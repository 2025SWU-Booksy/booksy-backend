package com.booksy.domain.book.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 책 정보를 조회할 때 클라이언트에게 전달되는 응답 DTO
 */
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
  private String imageUrl;
  private String description;
}
