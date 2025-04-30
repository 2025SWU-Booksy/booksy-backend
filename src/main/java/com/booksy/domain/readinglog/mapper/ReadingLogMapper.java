package com.booksy.domain.readinglog.mapper;

import com.booksy.domain.plan.entity.Plan;
import com.booksy.domain.readinglog.dto.ReadingLogRequestDto;
import com.booksy.domain.readinglog.entity.ReadingLog;
import com.booksy.domain.readinglog.type.ContentType;
import com.booksy.domain.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * 리뷰/스크랩 생성 요청 DTO를 실제 DB에 저장할 ReadingLog 엔티티로 변환
 */
@Component
public class ReadingLogMapper {

  public ReadingLog toEntity(ReadingLogRequestDto dto, User user, Plan plan) {
    ReadingLog.ReadingLogBuilder builder = ReadingLog.builder()
        .user(user)
        .plan(plan)
        .contentType(dto.getContentType())
        .content(dto.getContent());

    // 스크랩일 경우 → Book의 imageUrl 복사
    if (dto.getContentType() == ContentType.SCRAP) {
      builder.imageUrl(plan.getBook().getImageUrl());
    }

    return builder.build();
  }
}
