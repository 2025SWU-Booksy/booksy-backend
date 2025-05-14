package com.booksy.domain.book.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Getter;

/**
 * data4library의 libSrch API 응답 최상위 구조
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class LibraryResponse {

  private LibraryResponseBody response;

  @Getter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class LibraryResponseBody {

    private List<LibraryDto> libs;
  }

  @Getter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class LibraryDto {

    private String libCode;
    private String libName;
    private String latitude;
    private String longitude;
  }
}
