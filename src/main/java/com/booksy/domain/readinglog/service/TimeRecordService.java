package com.booksy.domain.readinglog.service;

import com.booksy.domain.plan.entity.Plan;
import com.booksy.domain.plan.repository.PlanRepository;
import com.booksy.domain.readinglog.dto.TimeRecordStartRequestDto;
import com.booksy.domain.readinglog.dto.TimeRecordStartResponseDto;
import com.booksy.domain.readinglog.dto.TimeRecordStopRequestDto;
import com.booksy.domain.readinglog.dto.TimeRecordStopResponseDto;
import com.booksy.domain.readinglog.entity.TimeRecord;
import com.booksy.domain.readinglog.repository.TimeRecordRepository;
import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.service.UserService;
import com.booksy.global.error.ErrorCode;
import com.booksy.global.error.exception.ApiException;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimeRecordService {

  private final TimeRecordRepository timeRecordRepository;
  private final PlanRepository planRepository;
  private final UserService userService;

  /**
   * 타이머 시작 처리
   */
  public TimeRecordStartResponseDto startTimer(TimeRecordStartRequestDto requestDto,
      Authentication authentication) {
    // 현재 로그인한 사용자 조회
    User user = userService.getCurrentUser(authentication);

    // 요청한 플랜 존재 여부 확인
    Plan plan = planRepository.findById(requestDto.getPlanId())
        .orElseThrow(() -> new ApiException(ErrorCode.ENTITY_NOT_FOUND));

    // 현재 로그인한 사용자의 플랜인지 확인
    if (!plan.getUser().getId().equals(user.getId())) {
      throw new ApiException(ErrorCode.UNAUTHORIZED_ACCESS);
    }

    // 이미 시작된 타이머가 존재하는지 확인
    timeRecordRepository.findFirstByUserIdAndEndTimeIsNullOrderByStartTimeDesc(user.getId())
        .ifPresent(record -> {
          throw new ApiException(ErrorCode.ILLEGAL_STATE); // 중복 시작 방지
        });

    // 타이머 기록 저장
    TimeRecord timeRecord = TimeRecord.builder()
        .user(user)
        .plan(plan)
        .startTime(LocalDateTime.now())
        .endTime(null)
        .duration(0)
        .build();

    timeRecordRepository.save(timeRecord);

    return new TimeRecordStartResponseDto(timeRecord.getId(), timeRecord.getStartTime());
  }

  /**
   * 타이머 종료 처리
   */
  public TimeRecordStopResponseDto stopTimer(TimeRecordStopRequestDto requestDto,
      Authentication authentication) {
    User user = userService.getCurrentUser(authentication);

    // 진행 중인 타이머 찾기
    TimeRecord timeRecord = timeRecordRepository
        .findFirstByUserIdAndEndTimeIsNullOrderByStartTimeDesc(user.getId())
        .orElseThrow(() -> new ApiException(ErrorCode.ENTITY_NOT_FOUND));

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startTime = timeRecord.getStartTime();

    // duration 계산 (분 단위)
    long minutes = Duration.between(startTime, now).toMinutes();

    // 종료 처리
    timeRecord.setEndTime(now);
    timeRecord.setDuration((int) minutes);
    timeRecordRepository.save(timeRecord);

    // 페이지 갱신
    Plan plan = timeRecord.getPlan();

    // 현재 페이지가 이전까지 읽은 페이지보다 줄어들 수 없음
    if (requestDto.getCurrentPage() < plan.getCurrentPage()) {
      throw new ApiException(ErrorCode.ILLEGAL_STATE);
    }

    plan.setCurrentPage(requestDto.getCurrentPage());
    planRepository.save(plan);

    // 응답 DTO 반환
    return new TimeRecordStopResponseDto(
        startTime,
        now,
        (int) minutes,
        plan.getCurrentPage()
    );
  }
}
