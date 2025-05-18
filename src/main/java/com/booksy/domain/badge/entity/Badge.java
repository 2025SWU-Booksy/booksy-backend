package com.booksy.domain.badge.entity;

import com.booksy.domain.badge.type.BadgeType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BadgeType type; // CATEGORY_COUNT, PLAN_COUNT, ...

  @Column(nullable = true)
  private String target; // categoryId, plan status, log type 등

  @Column(nullable = false)
  private int goal; // 목표 수치 (ex. 책 5권, 리뷰 30개 등)

  @Column(nullable = true)
  private String imageUrl;

  private String description;
}