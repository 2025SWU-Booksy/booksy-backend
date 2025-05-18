package com.booksy.domain.badge.type;

public enum BadgeType {
  CATEGORY_COUNT,      // 장르별 책 수 (target = categoryId)
  PLAN_COUNT,          // 특정 상태 플랜 수 (target = COMPLETED 등)
  READING_LOG_COUNT,   // 리뷰/스크랩/연속기록 수 (target = REVIEW, SCRAP, CONSECUTIVE)
  TIME_COUNT           // 타이머 누적 시간 (target = null)
}