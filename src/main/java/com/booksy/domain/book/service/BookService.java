package com.booksy.domain.book.service;

import com.booksy.domain.book.dto.BookResponseDto;
import com.booksy.domain.book.entity.Book;
import com.booksy.domain.book.external.BookExternalClient;
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
   *
   * 1. 내부 DB(Book 테이블)에서 ISBN으로 조회
   * 2. 존재하지 않으면 → 알라딘 API 호출하여 책 정보를 가져옴
   * 3. 가져온 정보를 Book 엔티티로 변환하여 DB에 저장
   *
   * @param isbn 조회할 도서의 ISBN
   * @return Book 엔티티 (기존 또는 새로 저장된 값)
   * @exception ApiException BOOK_NOT_FOUND_EXTERNAL (알라딘 API에 결과 없을 때)
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

}

