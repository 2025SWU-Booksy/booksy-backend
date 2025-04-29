package com.booksy.domain.plan.entity;

import com.booksy.domain.book.entity.Book;
import com.booksy.domain.plan.type.PlanStatus;
import com.booksy.domain.user.entity.User;
import com.booksy.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Plan 엔티티 클래스
 * DB의 'plan' 테이블과 매핑되며, 플랜 정보를 표현한다.
 */
@Entity
@Table(name = "plan")
@Getter
@Setter
@NoArgsConstructor
public class Plan extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "book_isbn", referencedColumnName = "isbn", nullable = false)
  private Book book;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PlanStatus status;

  private LocalDate startDate;
  private LocalDate endDate;

  private Integer currentPage;

  @Column(name = "is_free_plan")
  private Boolean isFreePlan;

  @Column(name = "excluded_dates", columnDefinition = "TEXT")
  private String excludedDates; // JSON string (e.g., ["2025-05-03", "2025-05-06"])

  @Column(name = "excluded_weekdays", columnDefinition = "TEXT")
  private String excludedWeekdays; // JSON string (e.g., [0,6])
}
