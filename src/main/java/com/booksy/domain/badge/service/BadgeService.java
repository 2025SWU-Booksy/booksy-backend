// BadgeService.java
package com.booksy.domain.badge.service;

import com.booksy.domain.badge.entity.Badge;
import com.booksy.domain.badge.entity.UserBadge;
import com.booksy.domain.badge.repository.BadgeRepository;
import com.booksy.domain.badge.repository.UserBadgeRepository;
import com.booksy.domain.badge.type.BadgeType;
import com.booksy.domain.plan.repository.PlanRepository;
import com.booksy.domain.plan.type.PlanStatus;
import com.booksy.domain.readinglog.repository.ReadingLogRepository;
import com.booksy.domain.readinglog.repository.TimeRecordRepository;
import com.booksy.domain.readinglog.type.ContentType;
import com.booksy.domain.user.entity.User;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 뱃지 획득 조건 평가 클래스
 */
@Service
@RequiredArgsConstructor
public class BadgeService {

  private final BadgeRepository badgeRepository;
  private final UserBadgeRepository userBadgeRepository;
  private final PlanRepository planRepository;
  private final ReadingLogRepository readingLogRepository;
  private final TimeRecordRepository timeRecordRepository;

  /**
   * 배지를 2개 획득할 때마다 1레벨씩 레벨업
   */
  private void updateUserLevel(User user) {
    int badgeCount = userBadgeRepository.countByUserId(user.getId());
    int newLevel = 1 + (badgeCount / 2);

    if (user.getLevel() != newLevel) {
      user.updateLevel(newLevel);
    }
  }

  /**
   * 플랜 완료 시 호출 → CATEGORY_COUNT, PLAN_COUNT 평가
   */
  @Transactional
  public List<Badge> evaluatePlanBadges(User user) {
    List<Badge> result = new ArrayList<>();
    List<Badge> badges = badgeRepository.findAll();

    for (Badge badge : badges) {
      if (badge.getType() != BadgeType.CATEGORY_COUNT &&
          badge.getType() != BadgeType.PLAN_COUNT) {
        continue;
      }

      if (userBadgeRepository.existsByUserIdAndBadgeId(user.getId(), badge.getId())) {
        continue;
      }

      boolean satisfied = switch (badge.getType()) {
        case CATEGORY_COUNT -> evaluateCategory(user, badge);
        case PLAN_COUNT -> evaluatePlan(user, badge);
        default -> false;
      };

      if (satisfied) {
        userBadgeRepository.save(UserBadge.builder()
            .user(user)
            .badge(badge)
            .acquiredAt(LocalDateTime.now())
            .build());
        updateUserLevel(user);
        result.add(badge);
      }
    }

    return result;
  }

  /**
   * 리뷰/스크랩 등록 시 호출 → READING_LOG_COUNT 평가
   */
  @Transactional
  public List<Badge> evaluateReadingLogBadges(User user, ContentType type) {
    List<Badge> result = new ArrayList<>();
    List<Badge> badges = badgeRepository.findAllByType(BadgeType.READING_LOG_COUNT);

    for (Badge badge : badges) {
      if (!badge.getTarget().equals(type.name())) {
        continue;
      }

      if (userBadgeRepository.existsByUserIdAndBadgeId(user.getId(), badge.getId())) {
        continue;
      }

      boolean satisfied = evaluateReadingLog(user, badge);

      if (satisfied) {
        userBadgeRepository.save(UserBadge.builder()
            .user(user)
            .badge(badge)
            .acquiredAt(LocalDateTime.now())
            .build());
        updateUserLevel(user);
        result.add(badge);
      }
    }

    return result;
  }

  /**
   * 타이머 완료 시 호출 → TIME_COUNT 평가
   */
  @Transactional
  public List<Badge> evaluateTimeBadges(User user) {
    List<Badge> result = new ArrayList<>();
    List<Badge> badges = badgeRepository.findAllByType(BadgeType.TIME_COUNT);

    for (Badge badge : badges) {
      if (userBadgeRepository.existsByUserIdAndBadgeId(user.getId(), badge.getId())) {
        continue;
      }

      boolean satisfied = evaluateTime(user, badge);

      if (satisfied) {
        userBadgeRepository.save(UserBadge.builder()
            .user(user)
            .badge(badge)
            .acquiredAt(LocalDateTime.now())
            .build());
        updateUserLevel(user);
        result.add(badge);
      }
    }

    return result;
  }

  // -------------------- 평가 조건 메서드 --------------------

  private boolean evaluateCategory(User user, Badge badge) {
    Long categoryId = Long.parseLong(badge.getTarget());
    int count = planRepository.countCompletedBooksByCategory(user.getId(), categoryId);
    return count >= badge.getGoal();
  }

  private boolean evaluatePlan(User user, Badge badge) {
    PlanStatus status = PlanStatus.valueOf(badge.getTarget());
    int count = planRepository.countByUserIdAndStatus(user.getId(), status);
    return count >= badge.getGoal();
  }

  private boolean evaluateReadingLog(User user, Badge badge) {
    ContentType type = ContentType.valueOf(badge.getTarget());

    int count = readingLogRepository.countByUserIdAndContentType(user.getId(), type);
    return count >= badge.getGoal();
  }

  private boolean evaluateTime(User user, Badge badge) {
    int totalMinutes = timeRecordRepository.getTotalReadingTime(user.getId());
    return totalMinutes >= badge.getGoal(); // goal = 분 단위
  }
}
