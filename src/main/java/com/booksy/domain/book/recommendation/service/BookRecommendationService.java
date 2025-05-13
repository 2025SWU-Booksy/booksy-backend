package com.booksy.domain.book.recommendation.service;

import com.booksy.domain.book.dto.BookResponseDto;
import com.booksy.domain.book.external.BookExternalClient;
import com.booksy.domain.book.recommendation.dto.BookGptResponseDto;
import com.booksy.domain.book.recommendation.dto.BookRecommendationResponseDto;
import com.booksy.domain.category.entity.UserCategory;
import com.booksy.domain.user.entity.User;
import com.booksy.global.ai.OpenAiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookRecommendationService {

  private final BookExternalClient bookExternalClient;
  private final OpenAiClient openAiClient;
  private final ObjectMapper objectMapper;

  public BookRecommendationResponseDto recommendBooks(User user) {

    // 1. 베스트셀러 (전체)
    List<BookResponseDto> bestsellers = bookExternalClient.searchBooksByCategory("0", 5, "popular");

    // 2. 주목할 만한 신간 (전체)
    List<BookResponseDto> newReleases = bookExternalClient.searchBooksByCategory("0", 5,
        "recentspecial");

    // 3. 관심 장르 기반 추천 도서 리스트
    Map<String, List<BookResponseDto>> genreRecommendations = new LinkedHashMap<>();
    if (user.getFavoriteGenres() != null) {
      user.getFavoriteGenres().stream()
          .map(UserCategory::getCategory)
          .filter(Objects::nonNull)
          .limit(5)
          .forEach(category -> {
            String categoryId = String.valueOf(category.getId());
            List<BookResponseDto> books = bookExternalClient.searchBooksByCategory(categoryId, 5,
                "editor");
            genreRecommendations.put(category.getName(), books);
          });
    }

    // 4. GPT 기반 개인화(나이+성별) 추천 도서 리스트
    List<BookResponseDto> personalized = recommendByGpt(user);

    return BookRecommendationResponseDto.builder()
        .bestsellers(bestsellers)
        .newReleases(newReleases)
        .genreRecommendations(genreRecommendations)
        .personalized(personalized)
        .build();
  }

  private List<BookResponseDto> recommendByGpt(User user) {
    try {
      String gptResponse = openAiClient.askRecommendation(user.getAge(), user.getGender().name());

      List<BookGptResponseDto> gptBooks = objectMapper.readValue(
          gptResponse,
          objectMapper.getTypeFactory()
              .constructCollectionType(List.class, BookGptResponseDto.class)
      );

      return gptBooks.stream()
          .map(book -> {
            try {
              return bookExternalClient.getBookByIsbnFromAladin(book.getIsbn());
            } catch (Exception e) {
              return null; // 실패한 ISBN은 무시
            }
          })
          .filter(Objects::nonNull)
          .collect(Collectors.toList());

    } catch (Exception e) {
      // GPT 응답 오류 → 빈 리스트 반환 (서비스 전체 실패 방지)
      return Collections.emptyList();
    }
  }
}

