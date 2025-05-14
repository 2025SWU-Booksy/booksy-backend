package com.booksy.domain.book.external.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 도서관 마커용 응답에 사용되는 단일 도서관 정보 DTO
 */
@Getter
@AllArgsConstructor
public class LibraryInfo {

  private final String libCode;
  private final String libraryName;
  private final double latitude;
  private final double longitude;
}
