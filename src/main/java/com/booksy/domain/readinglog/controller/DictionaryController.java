package com.booksy.domain.readinglog.controller;

import com.booksy.domain.readinglog.dto.DictionaryResponseDto;
import com.booksy.domain.readinglog.service.DictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dictionary")
@RequiredArgsConstructor
public class DictionaryController {

  private final DictionaryService dictionaryService;

  /**
   * 단어 검색 API
   *
   * @param keyword 검색어 (쿼리 파라미터)
   * @return 단어와 정의 정보
   */
  @GetMapping
  public ResponseEntity<DictionaryResponseDto> search(
      @RequestParam String keyword
  ) {
    DictionaryResponseDto response = dictionaryService.searchWord(keyword);
    return ResponseEntity.ok(response);
  }
}
