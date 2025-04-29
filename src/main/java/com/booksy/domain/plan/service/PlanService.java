package com.booksy.domain.plan.service;

import com.booksy.domain.book.entity.Book;
import com.booksy.domain.book.service.BookService;
import com.booksy.domain.plan.dto.PlanCreateRequestDto;
import com.booksy.domain.plan.dto.PlanPreviewResponseDto;
import com.booksy.domain.plan.dto.PlanResponseDto;
import com.booksy.domain.plan.entity.Plan;
import com.booksy.domain.plan.mapper.PlanMapper;
import com.booksy.domain.plan.repository.PlanRepository;
import com.booksy.domain.plan.type.PlanStatus;
import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.repository.UserRepository;
import com.booksy.domain.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
  private final UserRepository userRepository;

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

    // 🔥 (임시) userId=1인 사용자로 연결
    User user = userRepository.findById(1)
        .orElseThrow(() -> new UsernameNotFoundException("기본 사용자 없음"));
//    User user = userService.getCurrentUser()
//        .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHENTICATED));

    Book book = bookService.findOrCreateBookByIsbn(requestDto.getBookIsbn());

    List<LocalDate> readingDates = calculateReadingDates(
        requestDto.getStartDate(),
        requestDto.getPeriodDays(),
        requestDto.getExcludeDates(),
        requestDto.getExcludeWeekdays()
    );

    Plan plan = new Plan();
    plan.setUser(user);
    plan.setBook(book);
    plan.setStatus(PlanStatus.READING);
    plan.setStartDate(requestDto.getStartDate());
    plan.setIsFreePlan(requestDto.getIsFreePlan());
    plan.setCurrentPage(0);
    plan.setEndDate(requestDto.getIsFreePlan() ? null : readingDates.get(readingDates.size() - 1));
    plan.setExcludedDates(convertListToJson(requestDto.getExcludeDates()));
    plan.setExcludedWeekdays(convertListToJson(requestDto.getExcludeWeekdays()));

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
}
