package com.booksy.domain.book.entity;

import com.booksy.domain.category.entity.Category;
import com.booksy.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

/**
 * Book 엔티티 클래스 DB의 'book' 테이블과 매핑되며, 도서 정보를 표현한다.
 */
@Entity
@Table(name = "book")
@Getter
@Setter
@Builder
@AllArgsConstructor
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

  @Column(columnDefinition = "TEXT")
  private String fullDescription;

  @Column(name = "difficulty_level")
  private String difficultyLevel;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

}
