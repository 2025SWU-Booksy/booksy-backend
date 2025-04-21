package com.booksy.domain.book.entity;

import com.booksy.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Book 엔티티 클래스
 * DB의 'book' 테이블과 매핑되며, 도서 정보를 표현한다.
 */
@Entity
@Table(name = "book")
@Getter
@Setter
@NoArgsConstructor
public class Book extends BaseTimeEntity {

  @Id
  private String isbn;

  private String title;
  private String author;
  private String publisher;

  private LocalDate publishedDate;
  private int totalPage;

  @Column(name = "image_url")
  private String imageUrl;
}
