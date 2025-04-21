package com.booksy.domain.book.controller;

import com.booksy.domain.book.dto.BookResponseDto;
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
   * @return 검색된 도서 목록
   */
  @GetMapping("/search")
  public ResponseEntity<List<BookResponseDto>> searchBooks(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "10") int limit
  ) {
    List<BookResponseDto> result = bookService.searchBooksByKeyword(keyword, limit);
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
}
