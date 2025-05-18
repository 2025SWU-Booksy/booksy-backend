package com.booksy.domain.badge.repository;

import com.booksy.domain.badge.entity.Badge;
import com.booksy.domain.badge.type.BadgeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

  /**
   * 특정 타입의 모든 뱃지를 조회
   */
  List<Badge> findAllByType(BadgeType type);
}