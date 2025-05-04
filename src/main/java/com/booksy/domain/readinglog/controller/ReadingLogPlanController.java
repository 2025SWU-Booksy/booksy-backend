package com.booksy.domain.readinglog.controller;

import com.booksy.domain.readinglog.dto.ReadingLogRequestDto;
import com.booksy.domain.readinglog.dto.ReadingLogResponseDto;
import com.booksy.domain.readinglog.service.ReadingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/plans/{planId}/logs")
public class ReadingLogPlanController {

  private final ReadingLogService readingLogService;

  /**
   * 독서로그를 생성하는 API (리뷰 or 스크랩)
   *
   * @param planId         생성 대상 플랜의 ID (경로 변수)
   * @param dto            사용자 입력 데이터 (content, contentType 등)
   * @param authentication 사용자 정보 (JWT 토큰 기반)
   * @return 등록 메시지
   */
  @PostMapping
  public ResponseEntity<ReadingLogResponseDto> createReadingLog(@PathVariable Long planId,
      @RequestBody ReadingLogRequestDto dto,
      Authentication authentication) {

    ReadingLogResponseDto response = readingLogService.createReadingLog(planId, dto,
        authentication);
    return ResponseEntity.ok(response);
  }
}