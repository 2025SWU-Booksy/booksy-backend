package com.booksy.domain.plan.service;

import com.booksy.domain.book.entity.Book;
import com.booksy.domain.book.service.BookService;
import com.booksy.domain.plan.dto.PlanCreateRequestDto;
import com.booksy.domain.plan.dto.PlanDetailResponseDto;
import com.booksy.domain.plan.dto.PlanPreviewResponseDto;
import com.booksy.domain.plan.dto.PlanResponseDto;
import com.booksy.domain.plan.dto.PlanSummaryResponseDto;
import com.booksy.domain.plan.entity.Plan;
import com.booksy.domain.plan.mapper.PlanMapper;
import com.booksy.domain.plan.repository.PlanRepository;
import com.booksy.domain.plan.type.PlanStatus;
import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.service.UserService;
import com.booksy.global.error.ErrorCode;
import com.booksy.global.error.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 플랜 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class PlanService {

  private final PlanRepository planRepository;
  private final UserService userService;
  private final BookService bookService;
  private final PlanMapper planMapper;
  private final ObjectMapper objectMapper;

  /**
   * 플랜 미리보기를 위한 계산 (DB 저장 없이 결과만 반환)
   *
   * @param requestDto 플랜 생성 요청 정보
   * @return PlanPreviewResponseDto (책 정보 + 계산된 독서 일정)
   */
  @Transactional(readOnly = true)
  public PlanPreviewResponseDto previewPlan(PlanCreateRequestDto requestDto) {
    Book book = bookService.findOrCreateBookByIsbn(requestDto.getBookIsbn());

    List<LocalDate> readingDates = calculateReadingDates(
        requestDto.getStartDate(),
        requestDto.getPeriodDays(),
        requestDto.getExcludeDates(),
        requestDto.getExcludeWeekdays()
    );

    int dailyPages = book.getTotalPage() / readingDates.size();
    int dailyMinutes = dailyPages; // 1p = 1분 기준
    int totalDays = readingDates.size();

    return planMapper.toPreviewDto(book, dailyPages, dailyMinutes, totalDays, readingDates);
  }

  /**
   * 플랜 최종 저장 (DB에 저장)
   *
   * @param requestDto 플랜 생성 요청 정보
   * @return PlanResponseDto (저장된 플랜 요약 정보)
   */
  @Transactional
  public PlanResponseDto createPlan(PlanCreateRequestDto requestDto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    User user = userService.getCurrentUser(authentication);

    Book book = bookService.findOrCreateBookByIsbn(requestDto.getBookIsbn());

    List<LocalDate> readingDates = requestDto.getReadingDates() != null
        ? requestDto.getReadingDates()
        : calculateReadingDates(
            requestDto.getStartDate(),
            requestDto.getPeriodDays(),
            requestDto.getExcludeDates(),
            requestDto.getExcludeWeekdays()
        );

    Boolean isFreePlan = requestDto.getIsFreePlan();

    Plan plan = new Plan();
    plan.setUser(user);
    plan.setBook(book);
    plan.setStatus(PlanStatus.READING);
    plan.setStartDate(
        requestDto.getStartDate() != null
            ? requestDto.getStartDate()
            : readingDates.get(0)
    );
    plan.setIsFreePlan(requestDto.getIsFreePlan());
    plan.setCurrentPage(0);
    plan.setEndDate(
        Boolean.TRUE.equals(isFreePlan) ? null : readingDates.get(readingDates.size() - 1));
    plan.setReadingDates(convertListToJson(readingDates));
    plan.setDailyPages(requestDto.getDailyPages());
    plan.setDailyMinutes(requestDto.getDailyMinutes());

    Plan savedPlan = planRepository.save(plan);
    return planMapper.toResponseDto(savedPlan);
  }

  /**
   * 시작 날짜와 기간, 제외 조건을 기반으로 독서 예정 날짜 리스트 계산
   *
   * @param startDate       시작 날짜
   * @param periodDays      목표 기간 (읽을 날 수)
   * @param excludeDates    제외할 특정 날짜 리스트
   * @param excludeWeekdays 제외할 요일 리스트 (0: 일요일 ~ 6: 토요일)
   * @return 읽을 날짜 리스트
   */
  private List<LocalDate> calculateReadingDates(LocalDate startDate, Integer periodDays,
      List<LocalDate> excludeDates, List<Integer> excludeWeekdays) {
    if (Boolean.TRUE.equals(periodDays == null || startDate == null)) {
      return new ArrayList<>();
    }

    List<LocalDate> result = new ArrayList<>();
    LocalDate current = startDate;

    while (result.size() < periodDays) {
      boolean isExcluded = false;

      if (excludeDates != null && excludeDates.contains(current)) {
        isExcluded = true;
      }
      if (excludeWeekdays != null && excludeWeekdays.contains(
          current.getDayOfWeek().getValue() % 7)) {
        isExcluded = true;
      }

      if (!isExcluded) {
        result.add(current);
      }
      current = current.plusDays(1);
    }

    return result;
  }

  /**
   * 리스트(Object)를 JSON 문자열로 변환
   *
   * @param list 변환할 리스트
   * @return JSON 문자열
   */
  private String convertListToJson(Object list) {
    if (list == null) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(list);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("JSON 변환 실패", e);
    }
  }

  /**
   * 현재 로그인한 사용자의 플랜 목록을 상태별로 조회
   *
   * @param status 조회할 플랜 상태 (예: READING, COMPLETED)
   * @return 해당 상태에 해당하는 플랜 목록
   */
  @Transactional(readOnly = true)
  public List<PlanResponseDto> getPlansByStatus(PlanStatus status) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(authentication);

    List<Plan> plans = planRepository.findAllByUserAndStatus(user, status);
    return plans.stream()
        .map(planMapper::toResponseDto)
        .collect(Collectors.toList());
  }

  /**
   * 오늘 날짜 기준으로 진행 중인 플랜 목록 요약 조회
   *
   * 조건:
   * - 로그인 사용자 기준
   * - PlanStatus가 READING
   * - startDate ≤ 오늘 ≤ endDate
   *
   * @return List<PlanSummaryResponseDto> 오늘 읽을 책 리스트
   */
  @Transactional(readOnly = true)
  public List<PlanSummaryResponseDto> getTodayPlanSummaries() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(auth);
    LocalDate today = LocalDate.now();

    List<Plan> plans =
        planRepository.findByUserAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            user, PlanStatus.READING, today, today);

    return plans.stream()
        .map(planMapper::toSummaryDto)
        .collect(Collectors.toList());
  }

  /**
   * 플랜 상세 정보 조회
   *
   * @param planId 플랜 ID
   * @return PlanDetailResponseDto 상세 정보 응답
   */
  @Transactional(readOnly = true)
  public PlanDetailResponseDto getPlanDetail(Long planId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(authentication);

    Plan plan = planRepository.findByIdAndUser(planId, user)
        .orElseThrow(() -> new ApiException(ErrorCode.PLAN_NOT_FOUND));

    return planMapper.toDetailDto(plan);
  }

  /**
   * 해당 월에 진행 중인 모든 플랜 목록 조회 (캘린더 표시용)
   *
   * 주어진 연도와 월 기준으로, 그 달에 시작되었거나 진행 중인 모든 플랜을 반환한다.
   * 제외 날짜는 고려하지 않고, 플랜의 시작일과 종료일 범위를 기준으로 조회한다.
   *
   * @param year  조회할 연도 (예: 2025)
   * @param month 조회할 월 (1~12)
   * @return PlanSummaryResponseDto 리스트 (플랜 요약 정보)
   */
  @Transactional(readOnly = true)
  public List<PlanSummaryResponseDto> getPlansForCalendar(int year, int month) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(authentication);

    LocalDate startOfMonth = LocalDate.of(year, month, 1);
    LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

    List<Plan> plans = planRepository
        .findAllByUserAndStartDateLessThanEqualAndEndDateGreaterThanEqual(user, endOfMonth,
            startOfMonth);

    return plans.stream()
        .map(planMapper::toSummaryDto)
        .collect(Collectors.toList());
  }

  /**
   * 특정 날짜에 진행 중인 플랜 목록 조회
   *
   * @param date 조회할 날짜 (YYYY-MM-DD)
   * @return 해당 날짜에 진행 중인 플랜들의 요약 정보 목록
   */
  @Transactional(readOnly = true)
  public List<PlanSummaryResponseDto> getPlansByDate(LocalDate date) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(authentication);

    List<Plan> plans = planRepository
        .findAllByUserAndStartDateLessThanEqualAndEndDateGreaterThanEqual(user, date, date);

    return plans.stream()
        .map(planMapper::toSummaryDto)
        .collect(Collectors.toList());
  }

  /**
   * 플랜 상태를 중도 포기로 변경
   *
   * @param planId 플랜 ID
   */
  @Transactional
  public void abandonPlan(Long planId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(authentication);

    Plan plan = planRepository.findByIdAndUser(planId, user)
        .orElseThrow(() -> new ApiException(ErrorCode.PLAN_NOT_FOUND));

    if (!plan.getStatus().equals(PlanStatus.READING)) {
      throw new ApiException(ErrorCode.INVALID_PLAN_STATUS);
    }

    plan.setStatus(PlanStatus.ABANDONED);
  }

  /**
   * 플랜 종료일 연장
   *
   * @param planId     연장할 플랜의 ID
   * @param newEndDate 새로 설정할 종료일
   * @exception ApiException PLAN_NOT_FOUND: 플랜이 존재하지 않을 경우
   * @exception ApiException INVALID_PLAN_EXTENSION: 자유 플랜은 연장 불가
   */
  @Transactional
  public void extendPlan(Long planId, LocalDate newEndDate) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(authentication);

    Plan plan = planRepository.findByIdAndUser(planId, user)
        .orElseThrow(() -> new ApiException(ErrorCode.PLAN_NOT_FOUND));

    if (plan.getIsFreePlan() != null && plan.getIsFreePlan()) {
      throw new ApiException(ErrorCode.INVALID_PLAN_EXTENSION);
    }

    plan.setEndDate(newEndDate);
  }

}
