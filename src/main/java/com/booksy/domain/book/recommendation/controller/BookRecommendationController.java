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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books/recommendations")
public class BookRecommendationController {

  private final UserService userService;
  private final BookRecommendationService bookRecommendationService;

  @GetMapping
  public ResponseEntity<BookRecommendationResponseDto> getRecommendations(
      Authentication authentication) {
    User user = userService.getCurrentUser(authentication);
    BookRecommendationResponseDto responseDto = bookRecommendationService.recommendBooks(user);
    return ResponseEntity.ok(responseDto);
  }
}
