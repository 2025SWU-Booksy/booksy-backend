package com.booksy.domain.readinglog.entity;

import com.booksy.domain.plan.entity.Plan;
import com.booksy.domain.readinglog.type.ContentType;
import com.booksy.domain.user.entity.User;
import com.booksy.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reading_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingLog extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "plan_id", nullable = false)
  private Plan plan;

  @Enumerated(EnumType.STRING)
  @Column(name = "content_type", nullable = false)
  private ContentType contentType; // REVIEW or SCRAP

  @Column(columnDefinition = "TEXT")
  private String content; // 리뷰 본문 or 추출 텍스트

  @Column(name = "image_url")
  private String imageUrl; // 스크랩일 경우 이미지 경로

}