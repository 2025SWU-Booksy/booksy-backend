package com.booksy.domain.book.external;

import com.booksy.domain.book.dto.BookResponseDto;
import com.booksy.domain.book.external.dto.AladinItemResultDto;
import com.booksy.domain.book.external.type.AladinListType;
import com.booksy.domain.book.external.type.AladinSortType;
import com.booksy.domain.book.mapper.BookMapper;
import com.booksy.domain.plan.repository.PlanRepository;
import com.booksy.domain.plan.type.PlanStatus;
import com.booksy.global.error.ErrorCode;
import com.booksy.global.error.exception.ApiException;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 알라딘 외부 API와 통신하여 도서 정보를 가져오는 클라이언트
 * 도서 DB 제공 : 알라딘 인터넷서점( www.aladin.co.kr)
 */
@Component
@RequiredArgsConstructor
public class BookExternalClient {

  private final RestTemplate restTemplate = new RestTemplate();
  private final BookMapper bookMapper;
  private final PlanRepository planRepository;

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
   * 알라딘 API에 ISBN 기반 단일 도서 상세 조회 요청을 보낸다. (AI 가공용)
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

  /**
   * 알라딘 API에 ISBN 기반 단일 도서 상세 조회 요청을 보낸다. (조회용)
   *
   * @param isbn   조회할 도서의 ISBN
   * @param userId 로그인한 유저
   * @return BookResponseDto (정제된 도서 정보)
   * @exception ApiException 알라딘 API로부터 응답이 없거나 결과가 비어있을 경우
   */
  public BookResponseDto getBookByIsbnFromAladin(String isbn, Integer userId) {
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

    boolean isWishlisted = planRepository.existsByUserIdAndBookIsbnAndStatus(userId, isbn,
      PlanStatus.WISHLIST);

    return bookMapper.toDto(response.getItem().get(0), isWishlisted);
  }

  /**
   * 알라딘 API를 호출하여 카테고리 ID 및 정렬 기준에 따라 도서 목록을 조회한다.
   * 정렬 기준에 따라 내부적으로 적절한 QueryType을 자동 지정한다.
   *
   * @param categoryId 알라딘 카테고리 ID (예: "336"은 자기계발)
   * @param maxResults 최대 결과 개수 (예: 10, 20)
   * @param sort       정렬 기준 (popular, recent, editor, blog 중 하나)
   * @return BookResponseDto 리스트 (조회된 도서 목록)
   * @exception ApiException 정렬 값이 잘못된 경우 또는 내부 처리 중 예외 발생 시
   */
  @Cacheable(value = "bookList", key = "#categoryId + '_' + #sort")
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
      .queryParam("SearchTarget", "Book")
      .toUriString();

    AladinItemResultDto response = restTemplate.getForObject(url, AladinItemResultDto.class);

    if (response == null || response.getItem() == null || response.getItem().isEmpty()) {
      return Collections.emptyList();
    }

    return bookMapper.toDtoListFromAladin(response.getItem());
  }
}
