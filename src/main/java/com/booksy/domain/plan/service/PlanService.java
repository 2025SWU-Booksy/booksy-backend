package com.booksy.domain.plan.service;

import com.booksy.domain.book.entity.Book;
import com.booksy.domain.book.service.BookService;
import com.booksy.domain.plan.dto.PlanCreateRequestDto;
import com.booksy.domain.plan.dto.PlanDetailResponseDto;
import com.booksy.domain.plan.dto.PlanListResponseDto;
import com.booksy.domain.plan.dto.PlanPreviewResponseDto;
import com.booksy.domain.plan.dto.PlanResponseDto;
import com.booksy.domain.plan.dto.PlanSummaryResponseDto;
import com.booksy.domain.plan.entity.Plan;
import com.booksy.domain.plan.mapper.PlanMapper;
import com.booksy.domain.plan.repository.PlanRepository;
import com.booksy.domain.plan.type.PlanStatus;
import com.booksy.domain.readinglog.repository.ReadingLogRepository;
import com.booksy.domain.readinglog.type.ContentType;
import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.service.UserService;
import com.booksy.global.ai.OpenAiClient;
import com.booksy.global.error.ErrorCode;
import com.booksy.global.error.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
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

  private final OpenAiClient openAiClient;
  private final ReadingLogRepository readingLogRepository;

  /**
   * 플랜 미리보기를 위한 계산 (DB 저장 없이 결과만 반환)
   *
   * @param requestDto 플랜 생성 요청 정보
   * @return PlanPreviewResponseDto (책 정보 + 계산된 독서 일정)
   */
  @Transactional(readOnly = true)
  public PlanPreviewResponseDto previewPlan(PlanCreateRequestDto requestDto) {
    // 1. 도서 조회 or 저장
    Book book = bookService.findOrCreateBookByIsbn(requestDto.getBookIsbn());

    // 2. 읽을 날짜 계산
    List<LocalDate> readingDates = calculateReadingDates(
      requestDto.getStartDate(),
      requestDto.getPeriodDays(),
      requestDto.getExcludeDates(),
      requestDto.getExcludeWeekdays()
    );

    // 3. 난이도 판단 (기본값: 초급)
    String level = "초급";

    if (book.getDescription() != null && !book.getDescription().isBlank()) {
      if (book.getDifficultyLevel() != null && !book.getDifficultyLevel().isBlank()) {
        level = book.getDifficultyLevel();
      } else {
        // GPT 호출
        String gptResultJson = openAiClient.askDifficulty(book.getTitle(),
          book.getDescription());

        System.out.println("🎯 GPT 응답: " + gptResultJson);
        level = parseLevelFromJson(gptResultJson);

        // 결과 캐싱 (트랜잭션 안이므로 자동 감지되어 저장됨)
        book.setDifficultyLevel(level);
      }
    }

    // 난이도에 따른 읽기 속도 설정
    int speed = switch (level) {
      case "초급" -> 2; // 1p = 2분
      case "중급" -> 3;
      case "고급" -> 4;
      default -> 2;
    };

    int dailyPages = book.getTotalPage() / readingDates.size();
    int dailyMinutes = dailyPages * speed;
    int totalDays = readingDates.size();

    // 하루 권장 시간이 90분을 초과하는 경우 기간 재설정 유도
    boolean tooLong = dailyMinutes > 90;
    int recommendedDays = tooLong
      ? (int) Math.ceil((double) (book.getTotalPage() * speed) / 90)
      : totalDays;

    return planMapper.toPreviewDto(book, dailyPages, dailyMinutes, totalDays, readingDates,
      tooLong, recommendedDays);
  }

  /**
   * GPT 응답 문자열에서 난이도(level) 값을 파싱하여 반환
   *
   * GPT가 반환한 JSON 예시:
   * {
   * "level": "중급",
   * "reason": "의학적 내용과 철학적 고찰이 포함되어 있음"
   * }
   *
   * 해당 JSON에서 "level" 값을 추출하여 반환한다.
   * 파싱 실패 시 기본값인 "초급"을 반환한다.
   *
   * @param json GPT 응답 문자열 (JSON 형식)
   * @return 난이도 문자열 ("초급", "중급", "고급" 중 하나, 또는 fallback 값)
   */
  private String parseLevelFromJson(String json) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(json);
      return root.get("level").asText(); // "초급", "중급", "고급" 중 하나 기대
    } catch (Exception e) {
      // 파싱 실패시 기본값
      return "초급";
    }
  }

  /**
   * 사용자의 요청 정보를 바탕으로 새로운 독서 플랜을 생성하고 저장한다.
   *
   * 추천 일정을 사용할 경우, 제외 날짜 및 요일 정보를 반영하여 읽을 날짜를 계산한다.
   * 그렇지 않은 경우 preview에서 전달받은 readingDates를 그대로 사용한다.
   *
   * @param dto 독서 플랜 생성 요청 DTO
   * @return 생성된 독서 플랜에 대한 응답 DTO
   */
  @Transactional
  public PlanResponseDto createPlan(PlanCreateRequestDto dto) {
    // 1. 인증된 사용자 조회
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(authentication);

    // 2. 도서 조회
    Book book = bookService.findOrCreateBookByIsbn(dto.getBookIsbn());

    // 3. 독서 날짜 계산
    List<LocalDate> readingDates;

    if (Boolean.TRUE.equals(dto.getUseRecommendedPlan())) {
      // 추천 일정 사용 시, recommendedPeriodDays로 새로 계산
      readingDates = calculateReadingDates(
        dto.getStartDate(),
        dto.getRecommendedPeriodDays(),
        dto.getExcludeDates(),
        dto.getExcludeWeekdays()
      );
    } else {
      // preview에서 받은 readingDates 그대로 사용
      readingDates = dto.getReadingDates();
    }

    // 4. Plan 생성 (계산된 readingDates 사용)
    Plan plan = planMapper.toEntity(user, book, dto, readingDates);
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
   * 현재 로그인한 사용자의 플랜 목록을 모두 조회
   *
   * @return 전체 플랜 목록
   */
  @Transactional(readOnly = true)
  public List<PlanResponseDto> getAllPlans() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(authentication);

    List<Plan> plans = planRepository.findAllByUser(user);
    return plans.stream()
      .map(planMapper::toResponseDto)
      .collect(Collectors.toList());
  }

  /**
   * 현재 로그인한 사용자의 플랜 목록을 상태별로 조회
   *
   * @param status 조회할 플랜 상태 (예: READING, COMPLETED)
   * @return 해당 상태에 해당하는 플랜 목록
   */
  @Transactional(readOnly = true)
  public List<PlanListResponseDto> getPlansByStatus(PlanStatus status) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(authentication);

    List<Plan> plans = planRepository.findAllByUserAndStatus(user, status);

    return plans.stream()
      .map(plan -> {
        int scrapCount = (status == PlanStatus.COMPLETED)
          ? readingLogRepository.countByPlanIdAndContentType(plan.getId(), ContentType.SCRAP)
          : 0;
        return planMapper.toListDto(plan, status, scrapCount);
      })
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
  public List<PlanResponseDto> getReadingPlanSummaries() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(auth);
    LocalDate today = LocalDate.now();

    List<Plan> plans =
      planRepository.findByUserAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        user, PlanStatus.READING, today, today);

    return plans.stream()
      .map(planMapper::toResponseDto)
      .collect(Collectors.toList());
  }

  /**
   * 오늘 날짜가 포함된 플랜 목록 조회
   *
   * 조건:
   * - 로그인 사용자 기준
   * - PlanStatus가 READING
   * - readingDates에 오늘 날짜가 포함되어 있는 경우만
   *
   * @return List<PlanSummaryResponseDto> 오늘 읽을 책 리스트
   */
  @Transactional(readOnly = true)
  public List<PlanSummaryResponseDto> getPlansForToday() {
    LocalDate today = LocalDate.now();
    String todayStr = today.toString();

    // 1. 사용자 조회
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(auth);

    // 2. 사용자의 READING 플랜 조회
    List<Plan> plans = planRepository.findAllByUserAndStatus(user, PlanStatus.READING);

    // 3. 오늘 날짜가 포함된 플랜만 필터링
    List<Plan> todaysPlans = plans.stream()
      .filter(plan -> plan.getReadingDates().contains(todayStr))
      .toList();

    // 4. DTO로 변환 후 반환
    return todaysPlans.stream()
      .map(planMapper::toSummaryDto)
      .toList();
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

  /**
   * 플랜 단일 삭제
   *
   * @param planId 삭제할 플랜 ID
   * @exception ApiException PLAN_NOT_FOUND (존재하지 않거나 다른 사용자의 플랜일 경우)
   */
  @Transactional
  public void deletePlan(Long planId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(authentication);

    Plan plan = planRepository.findByIdAndUser(planId, user)
      .orElseThrow(() -> new ApiException(ErrorCode.PLAN_NOT_FOUND));

    planRepository.delete(plan);
  }

  /**
   * 플랜 다중 삭제
   *
   * @param planIds 삭제할 플랜 ID 리스트
   */
  @Transactional
  public void deletePlans(List<Long> planIds) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(authentication);

    planRepository.deleteByIdsAndUser(planIds, user);
  }

  /**
   * 도서를 위시리스트에 추가한다.
   *
   * 이미 위시리스트에 존재하는 경우 중복 추가를 방지하며, 존재하지 않을 경우 빈 Plan(PlanStatus = WISHLIST)을 생성한다.
   *
   * @param bookIsbn 추가할 도서의 ISBN13
   */
  @Transactional
  public void addToWishlist(String bookIsbn) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(authentication);

    // 중복 추가 방지
    if (planRepository.existsByUserIdAndBookIsbnAndStatus(user.getId(), bookIsbn,
      PlanStatus.WISHLIST)) {
      return;
    }

    // 책 정보 조회 또는 생성
    Book book = bookService.findOrCreateBookByIsbn(bookIsbn);

    // 빈 플랜 생성
    Plan wishlistPlan = planMapper.toWishlistEntity(user, book);

    planRepository.save(wishlistPlan);
  }

  /**
   * 도서를 위시리스트에서 제거한다.
   *
   * 해당 도서가 위시리스트에 존재할 경우에만 삭제를 수행하며,
   * 존재하지 않으면 아무 작업도 하지 않는다.
   *
   * @param bookIsbn 제거할 도서의 ISBN13
   */
  @Transactional
  public void removeFromWishlist(String bookIsbn) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(authentication);

    planRepository.findByUserAndBookIsbnAndStatus(user, bookIsbn, PlanStatus.WISHLIST)
      .ifPresent(planRepository::delete);
  }

}
