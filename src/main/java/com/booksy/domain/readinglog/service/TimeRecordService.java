package com.booksy.domain.readinglog.service;

import com.booksy.domain.badge.service.BadgeService;
import com.booksy.domain.plan.entity.Plan;
import com.booksy.domain.plan.repository.PlanRepository;
import com.booksy.domain.readinglog.dto.*;
import com.booksy.domain.readinglog.entity.TimeRecord;
import com.booksy.domain.readinglog.repository.TimeRecordRepository;
import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.service.UserService;
import com.booksy.global.error.ErrorCode;
import com.booksy.global.error.exception.ApiException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimeRecordService {

  private final TimeRecordRepository timeRecordRepository;
  private final PlanRepository planRepository;
  private final UserService userService;
  private final BadgeService badgeService;

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

    // 뱃지 획득 가능 여부 검사
    badgeService.evaluateTimeBadges(user);

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

  /**
   * 특정 플랜의 총 독서 시간 + 오늘 독서 시간을 계산하여 hh:mm:ss 포맷으로 반환
   */
  public TimeRecordResponseDto getTimeStat(Long planId, Authentication authentication) {
    User user = userService.getCurrentUser(authentication);

    // 플랜 소유 여부 확인
    Plan plan = planRepository.findById(planId)
        .orElseThrow(() -> new ApiException(ErrorCode.ENTITY_NOT_FOUND));
    if (!plan.getUser().getId().equals(user.getId())) {
      throw new ApiException(ErrorCode.UNAUTHORIZED_ACCESS);
    }

    // 전체 기록, 오늘 기록 각각 조회
    List<TimeRecord> all = timeRecordRepository.findByPlanId(planId);
    List<TimeRecord> today = timeRecordRepository.findTodayRecordsByPlanId(planId);

    // 전체 초 계산
    int totalSeconds = calculateTotalSeconds(all);
    int todaySeconds = calculateTotalSeconds(today);

    // 초 → hh:mm:ss 포맷 변환
    String totalFormatted = formatSecondsToHhmmss(totalSeconds);
    String todayFormatted = formatSecondsToHhmmss(todaySeconds);

    return new TimeRecordResponseDto(totalFormatted, todayFormatted);
  }

  /**
   * 타임레코드 리스트로부터 총 시간(초 단위) 계산 - startTime, endTime이 null이 아닌 경우만 포함
   */
  private int calculateTotalSeconds(List<TimeRecord> records) {
    return records.stream()
        .filter(r -> r.getStartTime() != null && r.getEndTime() != null)
        .mapToInt(r -> (int) Duration.between(r.getStartTime(), r.getEndTime()).getSeconds())
        .sum();
  }

  /**
   * 초(int)을 hh:mm:ss 형식의 문자열로 변환
   */
  private String formatSecondsToHhmmss(int seconds) {
    int hours = seconds / 3600;
    int remaining = seconds % 3600;
    int minutes = remaining / 60;
    int secs = remaining % 60;
    return String.format("%02d:%02d:%02d", hours, minutes, secs);
  }

  /**
   * 특정 날짜에 해당하는 타이머 기록들을 조회하고 시간 범위 및 duration(분)을 가공하여 리스트로 반환
   */
  public TimeRecordDetailResponseDto getTimerDetails(Long planId, LocalDate date,
      Authentication authentication) {
    User user = userService.getCurrentUser(authentication);

    // 플랜 소유 확인
    Plan plan = planRepository.findById(planId)
        .orElseThrow(() -> new ApiException(ErrorCode.ENTITY_NOT_FOUND));
    if (!plan.getUser().getId().equals(user.getId())) {
      throw new ApiException(ErrorCode.UNAUTHORIZED_ACCESS);
    }

    List<TimeRecord> records = timeRecordRepository.findByPlanIdAndDate(planId, date);

    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    List<TimeRecordDetailResponseDto.TimeRecordItemDto> items = records.stream()
        .filter(r -> r.getStartTime() != null && r.getEndTime() != null)
        .map(r -> new TimeRecordDetailResponseDto.TimeRecordItemDto(
            r.getStartTime().format(timeFormatter),
            r.getEndTime().format(timeFormatter),
            r.getDuration()
        ))
        .toList();

    int totalDuration = items.stream()
        .mapToInt(TimeRecordDetailResponseDto.TimeRecordItemDto::getDuration)
        .sum();

    return new TimeRecordDetailResponseDto(items, totalDuration);
  }

}
