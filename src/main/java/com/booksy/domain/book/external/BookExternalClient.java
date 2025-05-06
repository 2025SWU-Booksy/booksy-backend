package com.booksy.domain.book.external;

import com.booksy.domain.book.dto.BookResponseDto;
import com.booksy.domain.book.external.dto.AladinItemResultDto;
import com.booksy.domain.book.external.type.AladinListType;
import com.booksy.domain.book.external.type.AladinSortType;
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
   * 알라딘 API에 키워드 검색 요청을 보내 도서 리스트를 가져온다.
   *
   * @param keyword    검색 키워드
   * @param maxResults 최대 검색 결과 수
   * @return BookResponseDto 리스트 (정제된 형태)
   */
  public List<BookResponseDto> searchBooksByKeyword(String keyword, int maxResults, String sort)
      throws ApiException {
    String sortOption = AladinSortType.fromInput(sort);

    String url = UriComponentsBuilder.fromHttpUrl(
            "https://www.aladin.co.kr/ttb/api/ItemSearch.aspx")
        .queryParam("ttbkey", apiKey)
        .queryParam("Query", keyword)
        .queryParam("QueryType", "Keyword")
        .queryParam("SearchTarget", "Book")
        .queryParam("MaxResults", maxResults)
        .queryParam("Sort", sortOption)
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

  /**
   * 알라딘 API에 ISBN 기반 단일 도서 상세 조회 요청을 보낸다.
   *
   * @param isbn 조회할 도서의 ISBN
   * @return BookResponseDto (정제된 도서 정보)
   * @exception ApiException 알라딘 API로부터 응답이 없거나 결과가 비어있을 경우
   */
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

  public List<BookResponseDto> searchBooksByCategory(String categoryId, int maxResults,
      String sort) throws ApiException {
    String queryType = AladinListType.fromSortInput(sort);

    String url = UriComponentsBuilder.fromHttpUrl("https://www.aladin.co.kr/ttb/api/ItemList.aspx")
        .queryParam("ttbkey", apiKey)
        .queryParam("QueryType", queryType)
        .queryParam("CategoryId", categoryId)
        .queryParam("MaxResults", maxResults)
        .queryParam("output", "js")
        .queryParam("Version", "20131101")
        .toUriString();

    AladinItemResultDto response = restTemplate.getForObject(url, AladinItemResultDto.class);

    if (response == null || response.getItem() == null || response.getItem().isEmpty()) {
      return Collections.emptyList();
    }

    return bookMapper.toDtoListFromAladin(response.getItem());
  }

}
