package com.booksy.domain.book.mapper;

import com.booksy.domain.book.dto.BookResponseDto;
import com.booksy.domain.book.entity.Book;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Book 엔티티와 BookResponseDto 간의 변환을 담당하는 매퍼 클래스
 */
@Component
public class BookMapper {

  /**
   * Book 엔티티 → BookResponseDto 변환
   *
   * @param book 변환할 Book 객체
   * @return BookResponseDto
   */
  public BookResponseDto toDto(Book book) {
    if (book == null) {
      return null;
    }

    return BookResponseDto.builder()
        .isbn(book.getIsbn())
        .title(book.getTitle())
        .author(book.getAuthor())
        .publisher(book.getPublisher())
        .publishedDate(book.getPublishedDate())
        .totalPage(book.getTotalPage())
        .build();
  }

  /**
   * Book 리스트 → BookResponseDto 리스트 변환
   *
   * @param books Book 객체 리스트
   * @return BookResponseDto 리스트
   */
  public List<BookResponseDto> toDtoList(List<Book> books) {
    return books.stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

}
