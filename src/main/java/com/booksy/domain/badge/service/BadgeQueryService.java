// BadgeQueryService.java
package com.booksy.domain.badge.service;

import com.booksy.domain.badge.dto.BadgeResponseDto;
import com.booksy.domain.badge.entity.Badge;
import com.booksy.domain.badge.entity.UserBadge;
import com.booksy.domain.badge.repository.BadgeRepository;
import com.booksy.domain.badge.repository.UserBadgeRepository;
import com.booksy.domain.badge.type.BadgeType;
import com.booksy.domain.user.entity.User;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 뱃지 조회 클래스
 */
@Service
@RequiredArgsConstructor
public class BadgeQueryService {

  private final BadgeRepository badgeRepository;
  private final UserBadgeRepository userBadgeRepository;

  /**
   * 전체 뱃지를 조회하며, 사용자가 이미 획득한 뱃지인지 여부를 함께 표시한다.
   *
   * @param user 로그인한 사용자
   * @return 전체 뱃지 목록 (isAcquired 포함)
   */
  public List<BadgeResponseDto> getAllBadgesWithStatus(User user) {
    List<Badge> allBadges = badgeRepository.findAll();
    List<UserBadge> userBadges = userBadgeRepository.findAllByUser(user);

    Set<Long> acquiredIds = userBadges.stream()
        .map(ub -> ub.getBadge().getId())
        .collect(Collectors.toSet());

    return allBadges.stream()
        .map(badge -> BadgeResponseDto.from(badge, acquiredIds.contains(badge.getId())))
        .collect(Collectors.toList());
  }

  /**
   * 사용자가 획득한 뱃지를 조회한다.
   *
   * @param user 현재 로그인한 사용자
   * @return 획득한 뱃지 목록 (isAcquired = true)
   */
  public List<BadgeResponseDto> getMyBadges(User user) {
    List<UserBadge> userBadges = userBadgeRepository.findAllByUser(user);

    return userBadges.stream()
        .map(ub -> BadgeResponseDto.from(ub.getBadge(), true))
        .collect(Collectors.toList());
  }

  /**
   * 특정 타입의 뱃지만 필터링해서 조회하고, 사용자의 획득 여부 정보를 포함해 반환한다.
   *
   * @param user 로그인 사용자
   * @param type 조회할 뱃지 타입 (예: PLAN_COUNT, CATEGORY_COUNT)
   * @return 필터링된 뱃지 목록 (isAcquired 포함)
   */
  public List<BadgeResponseDto> getBadgesByTypeWithStatus(User user, BadgeType type) {
    List<Badge> filteredBadges = badgeRepository.findAllByType(type);
    List<UserBadge> userBadges = userBadgeRepository.findAllByUser(user);

    Set<Long> acquiredIds = userBadges.stream()
        .map(ub -> ub.getBadge().getId())
        .collect(Collectors.toSet());

    return filteredBadges.stream()
        .map(badge -> BadgeResponseDto.from(badge, acquiredIds.contains(badge.getId())))
        .collect(Collectors.toList());
  }
}
