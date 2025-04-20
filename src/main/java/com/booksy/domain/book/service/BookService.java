package com.booksy.domain.book.service;

import com.booksy.domain.book.dto.BookResponseDto;
import com.booksy.domain.book.entity.Book;
import com.booksy.domain.book.mapper.BookMapper;
import com.booksy.domain.book.repository.BookRepository;
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

  /**
   * ISBN을 통해 도서 정보를 조회하고, DTO로 반환
   *
   * @param isbn 도서 ISBN
   * @return BookResponseDto
   */
  @Transactional(readOnly = true)
  public BookResponseDto getBookByIsbn(String isbn) {
    Book book = bookRepository.findById(isbn)
        .orElseThrow();
    return bookMapper.toDto(book);
  }

}
