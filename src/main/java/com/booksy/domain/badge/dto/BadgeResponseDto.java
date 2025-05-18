package com.booksy.domain.badge.dto;

import com.booksy.domain.badge.entity.Badge;
import com.booksy.domain.badge.type.BadgeType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BadgeResponseDto {

  private Long badgeId;
  private String name;
  private BadgeType type;
  private String target;
  private int goal;
  private String imageUrl;
  private String description;
  private boolean isAcquired; //획득 여부

  public static BadgeResponseDto from(Badge badge, boolean isAcquired) {
    return BadgeResponseDto.builder()
        .badgeId(badge.getId())
        .name(badge.getName())
        .type(badge.getType())
        .target(badge.getTarget())
        .goal(badge.getGoal())
        .imageUrl(badge.getImageUrl())
        .description(badge.getDescription())
        .isAcquired(isAcquired)
        .build();
  }
}