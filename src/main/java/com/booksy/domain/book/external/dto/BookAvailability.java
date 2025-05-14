package com.booksy.domain.book.external.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 외부 API 결과를 내부 서비스에서 사용할 정제된 형태로 감싼 DTO
 */
@Getter
@AllArgsConstructor
public class BookAvailability {

  private final boolean hasBook;
  private final boolean loanAvailable;
}
