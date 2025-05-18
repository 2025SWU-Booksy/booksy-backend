package com.booksy.domain.badge.controller;

import com.booksy.domain.badge.dto.BadgeResponseDto;
import com.booksy.domain.badge.service.BadgeQueryService;
import com.booksy.domain.badge.type.BadgeType;
import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/badges")
public class BadgeController {

  private final UserService userService;
  private final BadgeQueryService badgeQueryService;

  /**
   * [GET] /api/badges BadgeType에 따라 필터링된 뱃지를 조회하고, 내가 획득한 뱃지에는 isAcquired = true로 표시 type이 없으면 전체
   * 뱃지를 반환
   */
  @GetMapping
  public List<BadgeResponseDto> getBadges(
      @RequestParam(required = false) BadgeType type,
      Authentication authentication
  ) {
    User user = userService.getCurrentUser(authentication);

    if (type == null) {
      return badgeQueryService.getAllBadgesWithStatus(user);
    }

    return badgeQueryService.getBadgesByTypeWithStatus(user, type);
  }


  /**
   * [GET] /api/badges/me 사용자가 획득한 뱃지 목록을 조회
   */
  @GetMapping("/me")
  public List<BadgeResponseDto> getMyBadges(Authentication authentication) {
    User user = userService.getCurrentUser(authentication);
    return badgeQueryService.getMyBadges(user);
  }

}
