package com.booksy.domain.book.external.type;

public enum AladinListType {
  POPULAR("popular", "Bestseller"),
  RECENT("recent", "ItemNewAll"),
  RECENTSPECIAL("recentspecial", "ItemNewSpecial");

  private final String input;
  private final String queryType;

  AladinListType(String input, String queryType) {
    this.input = input;
    this.queryType = queryType;
  }

  public String getQueryType() {
    return queryType;
  }

  public static String fromSortInput(String sort) {
    for (AladinListType type : values()) {
      if (type.input.equalsIgnoreCase(sort)) {
        return type.getQueryType();
      }
    }
    return POPULAR.getQueryType();
  }
}
