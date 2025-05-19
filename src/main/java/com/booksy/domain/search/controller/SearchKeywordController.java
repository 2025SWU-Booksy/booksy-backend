package com.booksy.domain.search.controller;

import com.booksy.domain.search.dto.SearchKeywordRequestDto;
import com.booksy.domain.search.service.SearchKeywordService;
import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/searches")
@RequiredArgsConstructor
public class SearchKeywordController {

  private final SearchKeywordService searchKeywordService;
  private final UserService userService;

  /**
   * 사용자의 검색 키워드를 저장합니다.
   *
   * @param requestDto 저장할 검색 키워드 정보
   * @return HTTP 200 OK 응답
   */
  @PostMapping
  public ResponseEntity<Void> saveSearchKeyword(
    @RequestBody @Valid SearchKeywordRequestDto requestDto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(authentication);

    searchKeywordService.saveSearchKeyword(requestDto.getKeyword(), user);
    return ResponseEntity.ok().build();
  }

  /**
   * 사용자의 최근 검색 키워드 목록을 조회합니다.
   *
   * @return 최근 검색 키워드 리스트
   */
  @GetMapping
  public ResponseEntity<List<String>> getRecentSearchKeywords() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(authentication);

    List<String> keywords = searchKeywordService.getRecentKeywords(user);
    return ResponseEntity.ok(keywords);
  }

  /**
   * 사용자의 특정 검색 키워드를 삭제합니다.
   *
   * @param keyword 삭제할 키워드
   * @return HTTP 204 No Content 응답
   */
  @DeleteMapping(params = "keyword")
  public ResponseEntity<Void> deleteSearchKeyword(@RequestParam String keyword) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(authentication);

    searchKeywordService.deleteKeyword(user, keyword);
    return ResponseEntity.noContent().build();
  }

  /**
   * 사용자의 모든 검색 키워드를 삭제합니다.
   *
   * @return HTTP 204 No Content 응답
   */
  @DeleteMapping
  public ResponseEntity<Void> deleteAllSearchKeywords() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(authentication);

    searchKeywordService.deleteAllKeywords(user);
    return ResponseEntity.noContent().build();
  }
}
