package com.booksy.domain.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  private Provider provider;  // NAVER, GOOGLE, KAKAO

  @Column(name = "provider_user_id")
  private String providerUserId;

  @Column(nullable = false)
  private String nickname;

  private String profileImage;  // URL

  @Enumerated(EnumType.STRING)
  private Gender gender;  // M, F

  private Integer age;

  @Column(nullable = false)
  private Boolean isPushEnabled = true;

  @CreationTimestamp  // 생성 시간 자동 입력
  private LocalDateTime createdAt;

  @UpdateTimestamp  // 수정 시간 자동 갱신
  private LocalDateTime updatedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserStatus status;  // ACTIVE, INACTIVE, DELETED
}