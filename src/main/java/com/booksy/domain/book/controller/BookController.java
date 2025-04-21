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
   * ISBN으로 도서 정보를 조회하는 API
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
   * 키워드로 도서 검색 결과 리스트 조회 (알라딘 API)
   */
  @GetMapping("/search")
  public ResponseEntity<List<BookResponseDto>> searchBooks(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "10") int limit
  ) {
    List<BookResponseDto> result = bookService.searchBooksByKeyword(keyword, limit);
    return ResponseEntity.ok(result);
  }
}
