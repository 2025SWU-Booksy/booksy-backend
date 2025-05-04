package com.booksy.domain.readinglog.mapper;

import com.booksy.domain.plan.entity.Plan;
import com.booksy.domain.readinglog.dto.ReadingLogRequestDto;
import com.booksy.domain.readinglog.dto.ReadingLogResponseDto;
import com.booksy.domain.readinglog.dto.UpdateLogResponseDto;
import com.booksy.domain.readinglog.entity.ReadingLog;
import com.booksy.domain.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * 리뷰/스크랩 생성 요청 DTO를 실제 DB에 저장할 ReadingLog 엔티티로 변환
 */
@Component
public class ReadingLogMapper {

  /**
   * 요청 DTO + 유저 + 플랜 → 엔티티 변환
   */
  public ReadingLog toEntity(ReadingLogRequestDto dto, User user, Plan plan) {
    return ReadingLog.builder()
        .user(user)
        .plan(plan)
        .contentType(dto.getContentType())
        .content(dto.getContent())
        .build();
  }

  /**
   * 엔티티 → 리딩로그 생성 응답 DTO 변환
   */
  public ReadingLogResponseDto toDto(ReadingLog log) {
    return ReadingLogResponseDto.builder()
        .id(log.getId())
        .planId(log.getPlan().getId())
        .contentType(log.getContentType())
        .content(log.getContent())
        .createdAt(log.getCreatedAt())
        .build();
  }

  /**
   * 리딩로그 수정 응답 dto 변환
   */
  public UpdateLogResponseDto toUpdateDto(ReadingLog log) {
    return UpdateLogResponseDto.builder()
        .id(log.getId())
        .planId(log.getPlan().getId())
        .content(log.getContent())
        .contentType(log.getContentType())
        .createdAt(log.getCreatedAt())
        .updatedAt(log.getUpdatedAt())
        .build();
  }
}

