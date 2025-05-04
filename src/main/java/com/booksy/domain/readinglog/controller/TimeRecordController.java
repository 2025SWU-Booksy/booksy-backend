package com.booksy.domain.readinglog.controller;

import com.booksy.domain.readinglog.dto.*;
import com.booksy.domain.readinglog.service.TimeRecordService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TimeRecordController {

  private final TimeRecordService timeRecordService;

  /**
   * 타이머 시작 API
   */
  @PostMapping("timers/start")
  public ResponseEntity<TimeRecordStartResponseDto> startTimer(
      @RequestBody TimeRecordStartRequestDto requestDto,
      Authentication authentication
  ) {
    TimeRecordStartResponseDto response = timeRecordService.startTimer(requestDto, authentication);
    return ResponseEntity.ok(response);
  }

  /**
   * 타이머 종료 API - 현재 실행 중인 타이머를 종료하고, plan의 현재 페이지를 갱신합니다.
   */
  @PostMapping("timers/stop")
  public ResponseEntity<TimeRecordStopResponseDto> stopTimer(
      @RequestBody TimeRecordStopRequestDto requestDto,
      Authentication authentication
  ) {
    TimeRecordStopResponseDto response = timeRecordService.stopTimer(requestDto, authentication);
    return ResponseEntity.ok(response);
  }

  /**
   * 플랜별 전체/오늘 독서 시간 조회 API - 응답은 hh:mm:ss 형식 문자열
   */
  @GetMapping("/plans/{planId}/timers")
  public ResponseEntity<TimeRecordResponseDto> getTimeStat(
      @PathVariable Long planId,
      Authentication authentication
  ) {
    TimeRecordResponseDto response = timeRecordService.getTimeStat(planId, authentication);
    return ResponseEntity.ok(response);
  }

  /**
   * 특정 날짜의 타이머 기록 조회 API - 시간 범위 (ex: 10:00 ~ 10:45) - duration (분 단위)
   */
  @GetMapping("/plans/{planId}/timers/detail")
  public ResponseEntity<TimeRecordDetailResponseDto> getTimerDetails(
      @PathVariable Long planId,
      @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      Authentication authentication
  ) {
    TimeRecordDetailResponseDto response = timeRecordService.getTimerDetails(planId, date,
        authentication);
    return ResponseEntity.ok(response);
  }
}
