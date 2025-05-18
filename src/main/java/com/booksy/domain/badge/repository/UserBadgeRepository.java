// UserBadgeRepository.java
package com.booksy.domain.badge.repository;

import com.booksy.domain.badge.entity.UserBadge;
import com.booksy.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

  /**
   * 특정 유저가 획득한 모든 뱃지를 조회
   */
  List<UserBadge> findAllByUser(User user);

  /**
   * 유저 ID와 뱃지 ID로 획득 여부를 조회
   */
  Optional<UserBadge> findByUserIdAndBadgeId(Integer userId, Long badgeId);

  /**
   * 뱃지 획득 여부 확인
   */
  boolean existsByUserIdAndBadgeId(Integer userId, Long badgeId);
}
