package com.booksy.domain.readinglog.controller;

import com.booksy.domain.readinglog.dto.ReadingLogRequestDto;
import com.booksy.domain.readinglog.service.ReadingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plans/{planId}/logs")
public class ReadingLogController {

  private final ReadingLogService readingLogService;

  /**
   * ReadingLog 생성 API
   */
  @PostMapping
  public ResponseEntity<String> createReadingLog(@PathVariable Long planId,
      @RequestBody ReadingLogRequestDto dto,
      Authentication authentication) {
    readingLogService.createReadingLog(planId, dto, authentication);
    return ResponseEntity.ok("등록되었습니다.");
  }
}
