package com.booksy.domain.book.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

/**
 * data4library의 bookExist API 응답 구조
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookExistResponse {

  private BookExistResponseBody response;

  @Getter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class BookExistResponseBody {

    private BookExistResult result;
  }
}
