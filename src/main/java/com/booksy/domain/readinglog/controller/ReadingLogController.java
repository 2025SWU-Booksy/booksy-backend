package com.booksy.domain.readinglog.controller;

import com.booksy.domain.readinglog.service.ReadingLogService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/logs")
public class ReadingLogController {

  private final ReadingLogService readingLogService;

  /**
   * 독서로그를 수정하는 API
   *
   * @param logId          수정할 로그의 ID
   * @param request        변경할 content 를 담은 요청
   * @param authentication 사용자 정보
   * @return 수정 메시지
   */

  @PatchMapping("/{logId}")
  public ResponseEntity<String> updateLog(@PathVariable Long logId,
      @RequestBody Map<String, String> request,
      Authentication authentication) {
    readingLogService.updateReadingLog(logId, request.get("content"), authentication);
    return ResponseEntity.ok("수정되었습니다.");
  }

  /**
   * 독서로그를 삭제하는 API
   *
   * @param logId          삭제할 로그 ID
   * @param authentication 사용자 정보
   * @return 삭제 메시지
   */
  @DeleteMapping("/{logId}")
  public ResponseEntity<String> deleteLog(@PathVariable Long logId,
      Authentication authentication) {
    readingLogService.deleteReadingLog(logId, authentication);
    return ResponseEntity.ok("삭제되었습니다.");
  }
}