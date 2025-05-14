package com.booksy.domain.book.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

/**
 * bookExist API에서 반환되는 도서 소장/대출 가능 정보 DTO
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookExistResult {

  private boolean hasBook;
  private boolean loanAvailable;
  private int loanCount;
}
