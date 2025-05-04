package com.booksy.domain.readinglog.repository;

import com.booksy.domain.readinglog.entity.TimeRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeRecordRepository extends JpaRepository<TimeRecord, Long> {

  /**
   * 종료하지 않은 타이머가 존재하는지 확인
   */
  Optional<TimeRecord> findFirstByUserIdAndEndTimeIsNullOrderByStartTimeDesc(Integer userId);
}