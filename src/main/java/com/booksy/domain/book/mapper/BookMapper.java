package com.booksy.domain.book.mapper;

import com.booksy.domain.book.dto.BookResponseDto;
import com.booksy.domain.book.entity.Book;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

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

  public List<BookResponseDto> toDtoList(List<Book> books) {
    return books.stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

}
