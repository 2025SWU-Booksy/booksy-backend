package com.booksy.domain.book.recommendation.dto;

import com.booksy.domain.book.dto.BookResponseDto;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookRecommendationResponseDto {

  private List<BookResponseDto> bestsellers;
  private List<BookResponseDto> newReleases;
  private Map<String, List<BookResponseDto>> genreRecommendations;
  private List<BookResponseDto> personalized;
}
