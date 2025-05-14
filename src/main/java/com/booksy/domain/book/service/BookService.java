package com.booksy.domain.book.service;

import com.booksy.domain.book.dto.BookResponseDto;
import com.booksy.domain.book.dto.LibraryLocationResponseDto;
import com.booksy.domain.book.entity.Book;
import com.booksy.domain.book.external.BookExternalClient;
import com.booksy.domain.book.external.LibraryExternalClient;
import com.booksy.domain.book.external.dto.BookAvailability;
import com.booksy.domain.book.external.dto.LibraryInfo;
import com.booksy.domain.book.mapper.BookMapper;
import com.booksy.domain.book.repository.BookRepository;
import com.booksy.global.error.ErrorCode;
import com.booksy.global.error.exception.ApiException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 도서 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class BookService {

  private final BookRepository bookRepository;
  private final BookMapper bookMapper;
  private final BookExternalClient bookExternalClient;
  private final LibraryExternalClient libraryExternalClient;

  /**
   * ISBN을 기반으로 내부 DB에 저장된 도서 정보를 조회
   *
   * @param isbn 조회할 도서의 ISBN
   * @return BookResponseDto
   */
  @Transactional(readOnly = true)
  public BookResponseDto getBookByIsbn(String isbn) {
    Book book = bookRepository.findById(isbn)
        .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND_INTERNAL));
    return bookMapper.toDto(book);
  }


  /**
   * 알라딘 API를 통해 키워드 기반 도서 목록을 검색
   *
   * @param keyword 검색 키워드
   * @param limit   최대 검색 결과 수
   * @return BookResponseDto 리스트
   */
  @Transactional(readOnly = true)
  public List<BookResponseDto> searchBooksByKeyword(String keyword, int limit, String sort) {
    return bookExternalClient.searchBooksByKeyword(keyword, limit, sort);
  }

  /**
   * 알라딘 API를 통해 ISBN 기반 도서 상세 정보를 조회
   *
   * @param isbn 조회할 도서의 ISBN
   * @return BookResponseDto
   */
  @Transactional(readOnly = true)
  public BookResponseDto getBookDetailFromAladin(String isbn) {
    return bookExternalClient.getBookByIsbnFromAladin(isbn);
  }

  /**
   * ISBN으로 책 정보를 조회하고, 없으면 알라딘 API에서 가져와 저장
   * <p>
   * 1. 내부 DB(Book 테이블)에서 ISBN으로 조회 2. 존재하지 않으면 → 알라딘 API 호출하여 책 정보를 가져옴 3. 가져온 정보를 Book 엔티티로 변환하여
   * DB에 저장
   *
   * @param isbn 조회할 도서의 ISBN
   * @return Book 엔티티 (기존 또는 새로 저장된 값)
   * @throws ApiException BOOK_NOT_FOUND_EXTERNAL (알라딘 API에 결과 없을 때)
   */
  @Transactional
  public Book findOrCreateBookByIsbn(String isbn) {
    return bookRepository.findById(isbn)
        .orElseGet(() -> {
          // 알라딘 API 호출
          BookResponseDto externalBook = bookExternalClient.getBookByIsbnFromAladin(isbn);

          if (externalBook == null) {
            throw new ApiException(ErrorCode.BOOK_NOT_FOUND_EXTERNAL);
          }

          Book newBook = bookMapper.toEntity(externalBook);

          return bookRepository.save(newBook);
        });
  }

  /**
   * 알라딘 API를 통해 카테고리 ID 기반 도서 목록을 조회
   *
   * @param categoryId 알라딘 카테고리 ID
   * @param limit      최대 검색 결과 수
   * @param sort       정렬 기준 (e.g., SalesPoint, PublishTime 등)
   * @return BookResponseDto 리스트
   */
  @Transactional(readOnly = true)
  public List<BookResponseDto> getBooksByCategory(String categoryId, int limit, String sort) {
    return bookExternalClient.searchBooksByCategory(categoryId, limit, sort);
  }

  /**
   * ISBN과 현재 위치를 기반으로 인근 도서관 정보를 조회하는 서비스 메서드
   *
   * @param isbn   ISBN-13
   * @param lat    사용자 위도
   * @param lng    사용자 경도
   * @param radius 검색 반경 (단위: km)
   * @return 도서관 위치 정보 목록
   */
  @Transactional(readOnly = true)
  public List<LibraryLocationResponseDto> getNearbyLibrariesWithBook(
      String isbn, double lat, double lng, double radius
  ) {
    // 책이 존재하는지 검증 (내부 또는 외부로 확인)
    findOrCreateBookByIsbn(isbn);

    // 도서관 위치 목록 조회
    List<LibraryInfo> libraries = libraryExternalClient.getNearbyLibraries(lat, lng, radius);

    // 응답 DTO로 변환
    return libraries.stream()
        .map(lib -> LibraryLocationResponseDto.builder()
            .libCode(lib.getLibCode())
            .libraryName(lib.getLibraryName())
            .latitude(lib.getLatitude())
            .longitude(lib.getLongitude())
            .build())
        .toList();
  }

  /**
   * 특정 도서관에서 ISBN 도서의 소장 여부 및 대출 가능 여부를 반환한다.
   *
   * @param isbn    ISBN-13
   * @param libCode 도서관 코드
   * @return BookAvailability 응답
   */
  @Transactional(readOnly = true)
  public BookAvailability getBookAvailability(String isbn, String libCode) {
    findOrCreateBookByIsbn(isbn);
    return libraryExternalClient.getBookAvailability(libCode, isbn);
  }

}

