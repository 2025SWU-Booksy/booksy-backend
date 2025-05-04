package com.booksy.domain.readinglog.controller;

import com.booksy.domain.readinglog.dto.ReadingLogRequestDto;
import com.booksy.domain.readinglog.dto.ReadingLogResponseDto;
import com.booksy.domain.readinglog.service.ReadingLogService;
import com.booksy.domain.readinglog.type.ContentType;
import java.util.List;
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

  /**
   * 특정 플랜에 속한 리뷰 or 스크랩 로그 리스트를 조회하는 API
   *
   * @param planId      조회 대상 플랜 ID
   * @param contentType 필터링할 로그 타입 (REVIEW or SCRAP)
   * @return 필터링된 로그 목록 리스트
   */
  @GetMapping
  public ResponseEntity<List<ReadingLogResponseDto>> getLogsByType(
      @PathVariable Long planId,
      @RequestParam("type") ContentType contentType,
      Authentication authentication) {

    List<ReadingLogResponseDto> response = readingLogService.getLogsByPlanAndType(planId,
        contentType, authentication);
    return ResponseEntity.ok(response);
  }
}