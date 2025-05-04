package com.booksy.domain.readinglog.controller;

import com.booksy.domain.readinglog.dto.TimeRecordStartRequestDto;
import com.booksy.domain.readinglog.dto.TimeRecordStartResponseDto;
import com.booksy.domain.readinglog.dto.TimeRecordStopRequestDto;
import com.booksy.domain.readinglog.dto.TimeRecordStopResponseDto;
import com.booksy.domain.readinglog.service.TimeRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/timers")
@RequiredArgsConstructor
public class TimeRecordController {

  private final TimeRecordService timeRecordService;

  /**
   * 타이머 시작 API
   */
  @PostMapping("/start")
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
  @PostMapping("/stop")
  public ResponseEntity<TimeRecordStopResponseDto> stopTimer(
      @RequestBody TimeRecordStopRequestDto requestDto,
      Authentication authentication
  ) {
    TimeRecordStopResponseDto response = timeRecordService.stopTimer(requestDto, authentication);
    return ResponseEntity.ok(response);
  }
}
