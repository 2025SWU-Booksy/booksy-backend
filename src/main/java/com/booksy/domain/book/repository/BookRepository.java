package com.booksy.domain.book.repository;

import com.booksy.domain.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 도서 정보를 관리하는 JPA 레포지토리 인터페이스
 */
public interface BookRepository extends JpaRepository<Book, String> {
  // ISBN(String)을 기본 키로 사용

}
