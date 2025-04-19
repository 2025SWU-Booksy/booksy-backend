package com.booksy.domain.book.service;

import com.booksy.domain.book.dto.BookResponseDto;
import com.booksy.domain.book.entity.Book;
import com.booksy.domain.book.mapper.BookMapper;
import com.booksy.domain.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {

  private final BookRepository bookRepository;
  private final BookMapper bookMapper;

  @Transactional(readOnly = true)
  public BookResponseDto getBookByIsbn(String isbn) {
    Book book = bookRepository.findById(isbn)
        .orElseThrow();
    return bookMapper.toDto(book);
  }

}
