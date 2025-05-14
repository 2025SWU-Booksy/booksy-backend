package com.booksy.domain.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 도서관 위치 정보를 클라이언트에 응답할 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class LibraryLocationResponseDto {

  private String libCode;
  private String libraryName;
  private double latitude;
  private double longitude;
}
