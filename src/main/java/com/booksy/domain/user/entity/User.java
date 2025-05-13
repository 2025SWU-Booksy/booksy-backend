package com.booksy.domain.user.entity;

import com.booksy.domain.category.entity.UserCategory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * user 엔티티 클래스
 */
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

  private String email;

  @Column(name = "password_hash") // 소셜 로그인 회원은 null
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

  @Builder.Default // 초기값 고정
  @Column(nullable = false)
  private Boolean isPushEnabled = true;

  @CreationTimestamp  // 생성 시간 자동 입력
  private LocalDateTime createdAt;

  @UpdateTimestamp  // 수정 시간 자동 갱신
  private LocalDateTime updatedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserStatus status;  // ACTIVE, INACTIVE, DELETED

  // 선호 장르
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<UserCategory> favoriteGenres = new ArrayList<>();

  /**
   * 정보 수정용
   */
  public void updatePassword(String newPassword) {
    this.password = newPassword;
  }

  public void updateAge(Integer age) {
    this.age = age;
  }

  public void updateGender(Gender gender) {
    this.gender = gender;
  }

  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }

  /**
   * 사용자 상태
   */
  public void updateStatus(UserStatus status) {
    this.status = status;
  }
}