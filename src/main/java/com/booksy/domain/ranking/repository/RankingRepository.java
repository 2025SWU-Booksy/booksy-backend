package com.booksy.domain.ranking.repository;

import com.booksy.domain.ranking.dto.RankingResponseDto;
import com.booksy.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RankingRepository extends JpaRepository<User, Long> {

  // ==============================
  // 📌 상위 50명 조회용 쿼리
  // ==============================

  /**
   * [독서 시간 기준] 상위 50명 조회 - TimeRecord.duration (분 단위) 누적합 기준 - startTime이 지정일 이후인 것만 집계 - 반환값:
   * "10시간 30분", valueType: "time"
   */
  @Query("""
          SELECT new com.booksy.domain.ranking.dto.RankingResponseDto(
              u.id,
              0,
              u.nickname,
              u.profileImage,
              CONCAT(FLOOR(SUM(COALESCE(t.duration, 0)) / 60), '시간 ', MOD(SUM(COALESCE(t.duration, 0)), 60), '분'),
              'time'
          )
          FROM User u
          JOIN TimeRecord t ON t.user = u
          WHERE t.startTime >= :start
          GROUP BY u.id, u.nickname, u.profileImage
          ORDER BY SUM(t.duration) DESC
      """)
  List<RankingResponseDto> getTop50ByReadingTime(@Param("start") LocalDateTime start,
      Pageable pageable);

  /**
   * [완료한 플랜 수 기준] 상위 50명 조회 - Plan.status == COMPLETED - updatedAt이 지정일 이후인 것만 포함 - 반환값: "35권",
   * valueType: "count"
   */
  @Query("""
          SELECT new com.booksy.domain.ranking.dto.RankingResponseDto(
              u.id,
              0,
              u.nickname,
              u.profileImage,
              CONCAT(COUNT(p), '권'),
              'count'
          )
          FROM User u
          JOIN Plan p ON p.user = u
          WHERE p.status = com.booksy.domain.plan.type.PlanStatus.COMPLETED
            AND p.updatedAt >= :start
          GROUP BY u.id, u.nickname, u.profileImage
          ORDER BY COUNT(p) DESC
      """)
  List<RankingResponseDto> getTop50ByCompletedPlans(@Param("start") LocalDateTime start,
      Pageable pageable);

  /**
   * [획득한 뱃지 수 기준] 상위 50명 조회 - UserBadge.acquiredAt 기준으로 필터링 - 반환값: "12개", valueType: "badge"
   */
  @Query("""
          SELECT new com.booksy.domain.ranking.dto.RankingResponseDto(
              u.id,
              0,
              u.nickname,
              u.profileImage,
              CONCAT(COUNT(ub), '개'),
              'badge'
          )
          FROM User u
          JOIN UserBadge ub ON ub.user = u
          WHERE ub.acquiredAt >= :start
          GROUP BY u.id, u.nickname, u.profileImage
          ORDER BY COUNT(ub) DESC
      """)
  List<RankingResponseDto> getTop50ByBadgeCount(@Param("start") LocalDateTime start,
      Pageable pageable);

  // ==============================
  // 📌 전체 사용자 기준 랭킹 조회 (내 랭킹 확인용)
  // ==============================

  /**
   * [전체 사용자] 독서 시간 기준 랭킹 조회 - 반환값에 userId 포함
   */
  @Query("""
          SELECT new com.booksy.domain.ranking.dto.RankingResponseDto(
              u.id,
              0,
              u.nickname,
              u.profileImage,
              CONCAT(FLOOR(SUM(t.duration) / 60), '시간 ', MOD(SUM(t.duration), 60), '분'),
              'time'
          )
          FROM User u
          JOIN TimeRecord t ON t.user = u
          WHERE t.startTime >= :start
          GROUP BY u.id, u.nickname, u.profileImage
          ORDER BY SUM(t.duration) DESC
      """)
  List<RankingResponseDto> getAllByReadingTime(@Param("start") LocalDateTime start);

  /**
   * [전체 사용자] 플랜 완료 수 기준 랭킹 조회
   */
  @Query("""
          SELECT new com.booksy.domain.ranking.dto.RankingResponseDto(
              u.id,
              0,
              u.nickname,
              u.profileImage,
              CONCAT(COUNT(p), '권'),
              'count'
          )
          FROM User u
          JOIN Plan p ON p.user = u
          WHERE p.status = com.booksy.domain.plan.type.PlanStatus.COMPLETED
            AND p.updatedAt >= :start
          GROUP BY u.id, u.nickname, u.profileImage
          ORDER BY COUNT(p) DESC
      """)
  List<RankingResponseDto> getAllByCompletedPlans(@Param("start") LocalDateTime start);

  /**
   * [전체 사용자] 획득 뱃지 수 기준 랭킹 조회
   */
  @Query("""
          SELECT new com.booksy.domain.ranking.dto.RankingResponseDto(
              u.id,
              0,
              u.nickname,
              u.profileImage,
              CONCAT(COUNT(ub), '개'),
              'badge'
          )
          FROM User u
          JOIN UserBadge ub ON ub.user = u
          WHERE ub.acquiredAt >= :start
          GROUP BY u.id, u.nickname, u.profileImage
          ORDER BY COUNT(ub) DESC
      """)
  List<RankingResponseDto> getAllByBadgeCount(@Param("start") LocalDateTime start);
}
