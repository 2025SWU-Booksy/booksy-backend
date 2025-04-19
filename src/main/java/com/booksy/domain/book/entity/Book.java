package com.booksy.domain.book.entity;

import com.booksy.global.common.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
