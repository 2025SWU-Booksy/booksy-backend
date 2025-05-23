package com.booksy.domain.plan.mapper;

import com.booksy.domain.book.entity.Book;
import com.booksy.domain.plan.dto.PlanCreateRequestDto;
import com.booksy.domain.plan.dto.PlanDetailResponseDto;
import com.booksy.domain.plan.dto.PlanPreviewResponseDto;
import com.booksy.domain.plan.dto.PlanResponseDto;
import com.booksy.domain.plan.dto.PlanSummaryResponseDto;
import com.booksy.domain.plan.entity.Plan;
import com.booksy.domain.plan.type.PlanStatus;
import com.booksy.domain.user.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Plan 엔티티와 DTO 간 변환을 담당하는 매퍼 클래스
 */
@Component
public class PlanMapper {

  private final ObjectMapper objectMapper;

  public PlanMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
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
      .build();
  }

  /**
   * Plan 엔티티를 상세 응답 DTO로 변환
   *
   * @param plan Plan 엔티티
   * @return PlanDetailResponseDto (책 + 플랜 상세 정보)
   */
  public PlanDetailResponseDto toDetailDto(Plan plan) {
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
