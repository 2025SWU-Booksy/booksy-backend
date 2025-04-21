package com.booksy.domain.book.mapper;

import com.booksy.domain.book.dto.BookResponseDto;
import com.booksy.domain.book.entity.Book;
import com.booksy.domain.book.external.dto.AladinItemDto;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
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
        .imageUrl(book.getImageUrl())
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


  /**
   * AladinItemDto → BookResponseDto 변환
   */
  public BookResponseDto toDto(AladinItemDto item) {
    if (item == null) {
      return null;
    }

    return BookResponseDto.builder()
        .isbn(item.getIsbn13())
        .title(item.getTitle())
        .author(item.getAuthor())
        .publisher(item.getPublisher())
        .publishedDate(parseDate(item.getPubDate()))
        .totalPage(item.getSubInfo() != null ? item.getSubInfo().getItemPage() : 0)
        .imageUrl(item.getCover())
        .build();
  }

  /**
   * AladinItemDto 리스트 → BookResponseDto 리스트 변환
   */
  public List<BookResponseDto> toDtoListFromAladin(List<AladinItemDto> items) {
    if (items == null) {
      return Collections.emptyList();
    }
    return items.stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  /**
   * 문자열 형태의 pubDate("yyyy-MM-dd") → LocalDate 변환
   */
  private LocalDate parseDate(String pubDate) {
    try {
      return LocalDate.parse(pubDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    } catch (Exception e) {
      return null;
    }
  }
}
