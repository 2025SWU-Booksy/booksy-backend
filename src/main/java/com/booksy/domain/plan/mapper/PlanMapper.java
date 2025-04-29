package com.booksy.domain.plan.mapper;

import com.booksy.domain.book.entity.Book;
import com.booksy.domain.plan.dto.PlanPreviewResponseDto;
import com.booksy.domain.plan.dto.PlanResponseDto;
import com.booksy.domain.plan.entity.Plan;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Plan 엔티티와 DTO 간 변환을 담당하는 매퍼 클래스
 */
@Component
public class PlanMapper {

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
      List<LocalDate> readingDates) {
    return PlanPreviewResponseDto.builder()
        .title(book.getTitle())
        .author(book.getAuthor())
        .publisher(book.getPublisher())
        .publishedDate(book.getPublishedDate())
        .totalPage(book.getTotalPage())
        .dailyRecommendedPages(dailyPages)
        .dailyRecommendedMinutes(dailyMinutes)
        .totalDurationDays(totalDays)
        .readingDates(readingDates)
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
}
