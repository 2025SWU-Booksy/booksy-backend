package com.booksy.domain.readinglog.repository;

import com.booksy.domain.readinglog.entity.ReadingLog;
import com.booksy.domain.readinglog.type.ContentType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingLogRepository extends JpaRepository<ReadingLog, Long> {

  /**
   * 특정 플랜의 리뷰 or 스크랩 전체 조회용
   */
  List<ReadingLog> findAllByPlanIdAndContentType(Long planId, ContentType contentType);

  /**
   * 특정 사용자(userId)의 전체 스크랩 목록 조회 (플랜 상관x)
   */
  List<ReadingLog> findAllByUserIdAndContentType(Integer userId, ContentType contentType);
}