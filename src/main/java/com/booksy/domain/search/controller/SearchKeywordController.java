package com.booksy.domain.search.controller;

import com.booksy.domain.search.dto.SearchKeywordRequestDto;
import com.booksy.domain.search.service.SearchKeywordService;
import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/searches")
@RequiredArgsConstructor
public class SearchKeywordController {

  private final SearchKeywordService searchKeywordService;
  private final UserService userService;

  @PostMapping
  public ResponseEntity<Void> saveSearchKeyword(
    @RequestBody @Valid SearchKeywordRequestDto requestDto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(authentication);

    searchKeywordService.saveSearchKeyword(requestDto.getKeyword(), user);
    return ResponseEntity.ok().build();
  }
}
