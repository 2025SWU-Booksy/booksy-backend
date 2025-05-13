package com.booksy.domain.book.recommendation.controller;

import com.booksy.domain.book.recommendation.dto.BookRecommendationResponseDto;
import com.booksy.domain.book.recommendation.service.BookRecommendationService;
import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 맞춤 도서 추천 API 컨트롤러
 * - 로그인한 사용자의 정보(선호 장르, 성별, 나이 등)를 기반으로 도서를 추천
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books/recommendations")
public class BookRecommendationController {

  private final UserService userService;
  private final BookRecommendationService bookRecommendationService;

  /**
   * 도서 추천 API
   *
   * @param authentication 현재 로그인한 사용자의 인증 정보
   * @return 추천 도서 리스트 (베스트셀러, 신간, 장르별, GPT 기반 맞춤형)
   */
  @GetMapping
  public ResponseEntity<BookRecommendationResponseDto> getRecommendations(
      Authentication authentication) {
    User user = userService.getCurrentUser(authentication);
    BookRecommendationResponseDto responseDto = bookRecommendationService.recommendBooks(user);
    return ResponseEntity.ok(responseDto);
  }
}
