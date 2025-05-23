package com.booksy.domain.book.controller;

import com.booksy.domain.book.dto.BookResponseDto;
import com.booksy.domain.book.dto.LibraryLocationResponseDto;
import com.booksy.domain.book.external.dto.BookAvailability;
import com.booksy.domain.book.service.BookService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {

  private final BookService bookService;

  /**
   * ISBN으로 도서 정보를 조회하는 API (내부 DB)
   *
   * @param isbn 조회할 도서의 ISBN
   * @return 도서 정보를 담은 BookResponseDto
   */
  @GetMapping("/{isbn}")
  public ResponseEntity<BookResponseDto> getBook(@PathVariable String isbn) {
    BookResponseDto response = bookService.getBookByIsbn(isbn);
    return ResponseEntity.ok(response);
  }

  /**
   * 키워드 기반으로 알라딘 API를 통해 도서 목록을 검색하는 API
   *
   * @param keyword 검색 키워드
   * @param limit   결과 개수 제한 (기본값 10)
   * @param sort    검색 정렬 (기본값 정확도순)
   * @return 검색된 도서 목록
   */
  @GetMapping("/search")
  public ResponseEntity<List<BookResponseDto>> searchBooks(
    @RequestParam String keyword,
    @RequestParam(defaultValue = "10") int limit,
    @RequestParam(defaultValue = "accuracy") String sort
  ) {
    List<BookResponseDto> result = bookService.searchBooksByKeyword(keyword, limit, sort);
    return ResponseEntity.ok(result);
  }

  /**
   * ISBN을 기반으로 알라딘 API를 통해 도서 상세 정보를 조회하는 API
   *
   * @param isbn 조회할 도서의 ISBN
   * @return 도서 상세 정보
   */
  @GetMapping("/aladin/{isbn}")
  public ResponseEntity<BookResponseDto> getBookDetailFromAladin(@PathVariable String isbn) {
    BookResponseDto result = bookService.getBookDetailFromAladin(isbn);
    return ResponseEntity.ok(result);
  }

  /**
   * 카테고리 기반 도서 목록 조회 API
   *
   * @param categoryId 알라딘 카테고리 ID
   * @param limit      결과 수 (기본값 10)
   * @param sort       정렬 기준 (기본값 "SalesPoint")
   * @return BookResponseDto 리스트
   */
  @GetMapping("/category")
  public List<BookResponseDto> getBooksByCategory(
    @RequestParam String categoryId,
    @RequestParam(defaultValue = "10") int limit,
    @RequestParam(defaultValue = "popular") String sort
  ) {
    return bookService.getBooksByCategory(categoryId, limit, sort);
  }

  /**
   * ISBN + 사용자 위치(lat/lng)로 근처 도서관 위치 정보 조회
   *
   * @param isbn   도서 ISBN
   * @param lat    사용자 위도
   * @param lng    사용자 경도
   * @param radius 반경 (단위: km), 기본 2km
   * @return 도서관 마커 정보 리스트
   */
  @GetMapping("/{isbn}/libraries/nearby")
  public ResponseEntity<List<LibraryLocationResponseDto>> getNearbyLibrariesWithBook(
    @PathVariable String isbn,
    @RequestParam double lat,
    @RequestParam double lng,
    @RequestParam(defaultValue = "2.0") double radius
  ) {
    List<LibraryLocationResponseDto> response = bookService.getNearbyLibrariesWithBook(isbn, lat,
      lng, radius);
    return ResponseEntity.ok(response);
  }

  /**
   * 도서관 소장 여부 및 대출 가능 여부를 조회하는 API
   *
   * @param isbn    ISBN-13
   * @param libCode 도서관 코드
   * @return BookAvailability 응답 DTO
   */
  @GetMapping("/{isbn}/libraries/{libCode}/availability")
  public ResponseEntity<BookAvailability> getBookAvailability(
    @PathVariable String isbn,
    @PathVariable String libCode
  ) {
    BookAvailability result = bookService.getBookAvailability(isbn, libCode);
    return ResponseEntity.ok(result);
  }
}
