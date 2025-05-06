package com.booksy.domain.book.external.type;

import com.booksy.global.error.ErrorCode;
import com.booksy.global.error.exception.ApiException;

public enum AladinSortType {

  ACCURACY("accuracy", "Accuracy"),
  POPULAR("popular", "SalesPoint");

  private final String input;       // 사용자 요청용
  private final String aladinValue; // 알라딘 API에 넘길 값

  AladinSortType(String input, String aladinValue) {
    this.input = input;
    this.aladinValue = aladinValue;
  }

  public String getAladinValue() {
    return aladinValue;
  }

  public static String fromInput(String input) {
    for (AladinSortType type : values()) {
      if (type.input.equalsIgnoreCase(input)) {
        return type.getAladinValue();
      }
    }
    throw new ApiException(ErrorCode.INVALID_SORT_TYPE);
  }
}
