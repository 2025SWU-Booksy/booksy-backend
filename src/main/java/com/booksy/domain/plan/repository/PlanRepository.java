package com.booksy.domain.plan.repository;

import com.booksy.domain.plan.entity.Plan;
import com.booksy.domain.plan.type.PlanStatus;
import com.booksy.domain.user.entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 플랜 정보를 관리하는 JPA 레포지토리 인터페이스
 */
public interface PlanRepository extends JpaRepository<Plan, Long> {

  // 상태별 플랜 조회
  List<Plan> findAllByUserAndStatus(User user, PlanStatus status);

  // 오늘 날짜에 해당하는 READING 상태 플랜 조회 (오늘 읽을 책)
  List<Plan> findByUserAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
      User user, PlanStatus status, LocalDate today1, LocalDate today2);

  // 플랜 상세 조회
  Optional<Plan> findByIdAndUser(Long planId, User user);

  // 특정 날짜가 플랜 진행 기간에 포함되는 경우 조회 (user 기준)
  List<Plan> findAllByUserAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
      User user, LocalDate date1, LocalDate date2);

  // 특정 플랜 삭제
  void deleteByIdAndUser(Long id, User user);

  // 다중 플랜 삭제
  @Modifying
  @Query("DELETE FROM Plan p WHERE p.id IN :ids AND p.user = :user")
  void deleteByIdsAndUser(@Param("ids") List<Long> ids, @Param("user") User user);

}
