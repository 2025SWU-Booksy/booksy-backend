package com.booksy.domain.book.external;

import com.booksy.domain.book.dto.BookResponseDto;
import com.booksy.domain.book.external.dto.AladinItemResultDto;
import com.booksy.domain.book.mapper.BookMapper;
import com.booksy.global.error.ErrorCode;
import com.booksy.global.error.exception.ApiException;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 알라딘 외부 API와 통신하여 도서 정보를 가져오는 클라이언트
 */
@Component
@RequiredArgsConstructor
public class BookExternalClient {

  private final RestTemplate restTemplate = new RestTemplate();
  private final BookMapper bookMapper;

  @Value("${external.aladin.api-key}")
  private String apiKey;

  /**
   * 키워드로 책 리스트를 알라딘에서 조회
   *
   * @param keyword    검색할 키워드
   * @param maxResults 최대 결과 수
   * @return BookResponseDto 리스트
   */
  public List<BookResponseDto> searchBooksByKeyword(String keyword, int maxResults) {
    String url = UriComponentsBuilder.fromHttpUrl(
            "https://www.aladin.co.kr/ttb/api/ItemSearch.aspx")
        .queryParam("ttbkey", apiKey)
        .queryParam("Query", keyword)
        .queryParam("QueryType", "Keyword")
        .queryParam("SearchTarget", "Book")
        .queryParam("MaxResults", maxResults)
        .queryParam("output", "js")
        .queryParam("Version", "20131101")
        .toUriString();

    AladinItemResultDto response =
        restTemplate.getForObject(url, AladinItemResultDto.class);

    if (response == null || response.getItem() == null || response.getItem().isEmpty()) {
      return Collections.emptyList();
    }

    return bookMapper.toDtoListFromAladin(response.getItem());
  }

  public BookResponseDto getBookByIsbnFromAladin(String isbn) {
    String url = UriComponentsBuilder.fromHttpUrl(
            "https://www.aladin.co.kr/ttb/api/ItemLookUp.aspx")
        .queryParam("ttbkey", apiKey)
        .queryParam("itemIdType", "ISBN13")
        .queryParam("ItemId", isbn)
        .queryParam("output", "js")
        .queryParam("Version", "20131101")
        .toUriString();

    AladinItemResultDto response = restTemplate.getForObject(url,
        AladinItemResultDto.class);

    if (response == null || response.getItem() == null || response.getItem().isEmpty()) {
      throw new ApiException(ErrorCode.BOOK_NOT_FOUND_EXTERNAL);
    }

    return bookMapper.toDto(response.getItem().get(0));
  }
}
