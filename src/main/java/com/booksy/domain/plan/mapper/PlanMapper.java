package com.booksy.domain.plan.mapper;

import com.booksy.domain.book.entity.Book;
import com.booksy.domain.plan.dto.PlanCreateRequestDto;
import com.booksy.domain.plan.dto.PlanDetailResponseDto;
import com.booksy.domain.plan.dto.PlanListResponseDto;
import com.booksy.domain.plan.dto.PlanPreviewResponseDto;
import com.booksy.domain.plan.dto.PlanResponseDto;
import com.booksy.domain.plan.dto.PlanSummaryResponseDto;
import com.booksy.domain.plan.entity.Plan;
import com.booksy.domain.plan.type.PlanStatus;
import com.booksy.domain.readinglog.dto.TimeRecordResponseDto;
import com.booksy.domain.readinglog.repository.ReadingLogRepository;
import com.booksy.domain.user.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Plan 엔티티와 DTO 간 변환을 담당하는 매퍼 클래스
 */
@Component
public class PlanMapper {

  private final ObjectMapper objectMapper;
  private final ReadingLogRepository readingLogRepository;

  public PlanMapper(ObjectMapper objectMapper, ReadingLogRepository readingLogRepository) {
    this.objectMapper = objectMapper;
    this.readingLogRepository = readingLogRepository;
  }

  /**
   * 도서(Book) + 계산 결과를 기반으로 플랜 미리보기 응답 DTO 생성
   *
   * @param book         책 정보
   * @param dailyPages   하루 추천 페이지 수
   * @param dailyMinutes 하루 추천 독서 시간 (분)
   * @param totalDays    플랜 전체 소요 기간
   * @param readingDates 실제 읽을 날짜 리스트
   * @return PlanPreviewResponseDto
   */
  public PlanPreviewResponseDto toPreviewDto(Book book,
    int dailyPages,
    int dailyMinutes,
    int totalDays,
    List<LocalDate> readingDates,
    boolean tooLong,
    int recommendedPeriodDays) {
    return PlanPreviewResponseDto.builder()
      .bookIsbn(book.getIsbn())
      .title(book.getTitle())
      .author(book.getAuthor())
      .publisher(book.getPublisher())
      .publishedDate(book.getPublishedDate())
      .totalPage(book.getTotalPage())
      .imageUrl(book.getImageUrl())
      .dailyPages(dailyPages)
      .dailyMinutes(dailyMinutes)
      .totalDurationDays(totalDays)
      .readingDates(readingDates)
      .tooLong(tooLong)
      .recommendedPeriodDays(recommendedPeriodDays)
      .build();
  }

  /**
   * 저장된 Plan 엔티티를 플랜 요약 응답 DTO로 변환
   *
   * @param plan 저장된 플랜 엔티티
   * @return PlanResponseDto
   */
  public PlanResponseDto toResponseDto(Plan plan) {
    return PlanResponseDto.builder()
      .id(plan.getId())
      .bookTitle(plan.getBook().getTitle())
      .imageUrl(plan.getBook().getImageUrl())
      .status(plan.getStatus())
      .startDate(plan.getStartDate())
      .endDate(plan.getEndDate())
      .build();
  }

  /**
   * Plan 엔티티를 메인 요약 응답 Dto로 변환
   *
   * @param plan Plan 엔티티
   * @return PlanSummaryResponseDto (오늘 읽을 책 정보 요약)
   */
  public PlanSummaryResponseDto toSummaryDto(Plan plan) {
    Book book = plan.getBook();
    int current = plan.getCurrentPage();
    int total = book.getTotalPage();
    int progress = (total == 0) ? 0 : (int) Math.round((double) current / total * 100);

    return PlanSummaryResponseDto.builder()
      .planId(plan.getId())
      .bookTitle(book.getTitle())
      .author(book.getAuthor())
      .imageUrl(book.getImageUrl())
      .startDate(plan.getStartDate())
      .endDate(plan.getEndDate())
      .currentPage(current)
      .totalPage(total)
      .progressRate(progress)
      .totalReadingTime(null)
      .dailyPages(plan.getDailyPages())
      .dailyMinutes(plan.getDailyMinutes())
      .build();
  }

  /**
   * Plan 엔티티를 상세 응답 DTO로 변환
   *
   * @param plan Plan 엔티티
   * @return PlanDetailResponseDto (책 + 플랜 상세 정보)
   */
  public PlanDetailResponseDto toDetailDto(Plan plan, TimeRecordResponseDto timeDto) {
    Book book = plan.getBook();

    int totalPage = book.getTotalPage();
    int currentPage = plan.getCurrentPage() != null ? plan.getCurrentPage() : 0;
    int progressRate = totalPage == 0 ? 0 : (int) (((double) currentPage / totalPage) * 100);

    return PlanDetailResponseDto.builder()
      .planId(plan.getId())
      .bookTitle(book.getTitle())
      .author(book.getAuthor())
      .publisher(book.getPublisher())
      .publishedDate(book.getPublishedDate() != null ? book.getPublishedDate().toString() : null)
      .imageUrl(book.getImageUrl())
      .totalPage(totalPage)

      .startDate(plan.getStartDate())
      .endDate(plan.getEndDate())
      .currentPage(currentPage)
      .dailyPages(plan.getDailyPages() != null ? plan.getDailyPages() : 0)
      .dailyMinutes(plan.getDailyMinutes() != null ? plan.getDailyMinutes() : 0)
      .progressRate(progressRate)
      .status(plan.getStatus())
      .todayReadingTime(timeDto.getTodayDuration())
      .totalReadingTime(timeDto.getTotalDuration())
      .readingDates(parseReadingDates(plan.getReadingDates()))
      .build();
  }

  /**
   * PlanCreateRequestDto와 기타 필요한 정보를 바탕으로 Plan 엔티티를 생성합니다.
   *
   * @param user         플랜을 생성할 사용자
   * @param book         독서 플랜 대상 도서
   * @param dto          사용자로부터 전달받은 플랜 생성 요청 DTO
   * @param readingDates 책을 읽을 날짜 리스트 (예: 추천된 날짜 목록)
   * @return Plan 생성에 필요한 필드가 포함된 Plan 엔티티 객체
   */
  public Plan toEntity(User user, Book book, PlanCreateRequestDto dto,
    List<LocalDate> readingDates) {
    boolean isFree = Boolean.TRUE.equals(dto.getIsFreePlan());
    LocalDate start = dto.getStartDate() != null ? dto.getStartDate() : readingDates.get(0);

    return Plan.builder()
      .user(user)
      .book(book)
      .status(PlanStatus.READING)
      .startDate(start)
      .endDate(isFree ? null : readingDates.get(readingDates.size() - 1))
      .isFreePlan(isFree)
      .currentPage(0)
      .readingDates(convertListToJson(readingDates))
      .dailyPages(dto.getDailyPages())
      .dailyMinutes(dto.getDailyMinutes())
      .build();
  }

  /**
   * 플랜 엔티티와 상태값, 스크랩 수치를 받아 통합 응답 DTO로 변환한다.
   *
   * 상태값에 따라 필요한 필드를 조건적으로 포함한다:
   * - WISHLIST, ABANDONED: 이미지, 제목, 저자, 출판사
   * - READING: 시작/종료 날짜, 오늘 인덱스, 진행률
   * - COMPLETED: 시작/종료 날짜, 별점, 스크랩 개수
   *
   * @param plan       변환할 플랜 엔티티
   * @param status     현재 플랜 상태 (PlanStatus)
   * @param scrapCount COMPLETED 상태에서 사용되는 스크랩 개수 (기타 상태에서는 0으로 전달)
   * @return 상태에 맞게 구성된 PlanListResponseDto
   */
  public PlanListResponseDto toListDto(Plan plan, PlanStatus status, int scrapCount) {
    PlanListResponseDto.PlanListResponseDtoBuilder builder = PlanListResponseDto.builder()
      .planId(plan.getId())
      .isbn(plan.getBook().getIsbn())
      .title(plan.getBook().getTitle())
      .author(plan.getBook().getAuthor())
      .publisher(plan.getBook().getPublisher())
      .imageUrl(plan.getBook().getImageUrl());

    if (status == PlanStatus.READING || status == PlanStatus.COMPLETED) {
      builder.startDate(plan.getStartDate())
        .endDate(plan.getEndDate());
    }

    if (status == PlanStatus.READING) {
      builder.todayIndex(calculateTodayIndex(plan))
        .progressPercent(calculateProgress(plan));
    }

    if (status == PlanStatus.COMPLETED) {
      builder
//        .rating(plan.getRating())
        .scrapCount(scrapCount);
    }

    return builder.build();
  }

  /**
   * readingDates(JSON 문자열)에서 오늘 날짜가 몇 번째인지 계산
   */
  private int calculateTodayIndex(Plan plan) {
    String readingDatesJson = plan.getReadingDates();
    if (readingDatesJson == null || readingDatesJson.isBlank()) {
      return 0;
    }

    try {
      List<LocalDate> dates = objectMapper.readValue(
        readingDatesJson,
        new TypeReference<>() {
        }
      );

      LocalDate today = LocalDate.now();
      for (int i = 0; i < dates.size(); i++) {
        if (dates.get(i).equals(today)) {
          return i + 1;
        }
      }

    } catch (Exception e) {
//      log.warn("readingDates 파싱 실패 - planId: {}, 원본: {}", plan.getId(), readingDatesJson, e);
    }

    return 0;
  }

  /**
   * 전체 페이지 대비 현재 페이지를 기준으로 진행률(%) 계산
   */
  private int calculateProgress(Plan plan) {
    Integer total = plan.getBook().getTotalPage();
    Integer current = plan.getCurrentPage();

    if (total == null || total <= 0 || current == null || current <= 0) {
      return 0;
    }

    return Math.min(100, (int) ((double) current / total * 100));
  }


  /**
   * 리스트(Object)를 JSON 문자열로 변환
   *
   * @param list 변환할 리스트
   * @return JSON 문자열
   */
  public String convertListToJson(Object list) {
    if (list == null) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(list);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("JSON 변환 실패", e);
    }
  }


  public List<String> parseReadingDates(String json) {
    try {
      return objectMapper.readValue(json, new TypeReference<>() {
      });
    } catch (JsonProcessingException e) {
      return Collections.emptyList();
    }
  }

  /**
   * 위시리스트용 빈 플랜 엔티티 생성
   *
   * @param user 사용자
   * @param book 도서 정보
   * @return Plan (PlanStatus = WISHLIST)
   */
  public Plan toWishlistEntity(User user, Book book) {
    return Plan.builder()
      .user(user)
      .book(book)
      .status(PlanStatus.WISHLIST)
      .currentPage(0)
      .build();
  }

}
