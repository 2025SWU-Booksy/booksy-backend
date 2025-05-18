package com.booksy.domain.readinglog.repository;

import com.booksy.domain.readinglog.entity.TimeRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeRecordRepository extends JpaRepository<TimeRecord, Long> {

  /**
   * 종료하지 않은 타이머가 존재하는지 확인
   */
  Optional<TimeRecord> findFirstByUserIdAndEndTimeIsNullOrderByStartTimeDesc(Integer userId);

  /**
   * 플랜에 해당하는 전체 기록 가져오기
   */
  List<TimeRecord> findByPlanId(Long planId);

  /**
   * 오늘 날짜의 기록만 가져오기
   */
  @Query("""
          SELECT r FROM TimeRecord r
          WHERE r.plan.id = :planId AND FUNCTION('DATE', r.startTime) = CURRENT_DATE
      """)
  List<TimeRecord> findTodayRecordsByPlanId(@Param("planId") Long planId);

  /**
   * 특정 날짜의 타이머 전체 기록 가져오기
   */
  @Query("""
          SELECT r FROM TimeRecord r
          WHERE r.plan.id = :planId AND FUNCTION('DATE', r.startTime) = :date
          ORDER BY r.startTime ASC
      """)
  List<TimeRecord> findByPlanIdAndDate(
      @Param("planId") Long planId,
      @Param("date") LocalDate date
  );

  /**
   * 타이머 완료 시 독서 시간 뱃지 조건 검사
   */
  @Query("""
        SELECT COALESCE(SUM(tr.duration), 0)
        FROM TimeRecord tr
        WHERE tr.user.id = :userId
          AND tr.duration > 0
      """)
  int getTotalReadingTime(@Param("userId") Integer userId);
}