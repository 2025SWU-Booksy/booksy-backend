package com.booksy.domain.readinglog.controller;

import com.booksy.domain.readinglog.dto.*;
import com.booksy.domain.readinglog.service.ReadingLogService;
import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.service.UserService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/logs")
public class ReadingLogController {

  private final ReadingLogService readingLogService;
  private final UserService userService;

  /**
   * 독서로그를 수정하는 API
   *
   * @param logId          수정할 로그의 ID
   * @param request        변경할 content 를 담은 요청
   * @param authentication 사용자 정보
   * @return 수정 메시지
   */

  @PatchMapping("/{logId}")
  public ResponseEntity<UpdateLogResponseDto> updateLog(@PathVariable Long logId,
      @RequestBody Map<String, String> request,
      Authentication authentication) {
    UpdateLogResponseDto response = readingLogService.updateReadingLog(logId,
        request.get("content"), authentication);
    return ResponseEntity.ok(response);
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

  /**
   * 특정 독서로그 단건 조회 API
   *
   * @param logId          조회할 로그 ID
   * @param authentication 현재 로그인 사용자 정보
   * @return 해당 로그의 상세 정보
   */
  @GetMapping("/{logId}")
  public ResponseEntity<ReadingLogResponseDto> getLogById(
      @PathVariable Long logId,
      Authentication authentication) {

    ReadingLogResponseDto response = readingLogService.getLogById(logId, authentication);
    return ResponseEntity.ok(response);
  }

  /**
   * 전체 스크랩 목록 조회 API
   *
   * @param authentication JWT 토큰 기반 로그인 사용자 정보
   * @return ScrapResponseDto 리스트 (id, content, 책 제목, 작가, 생성일)
   */
  @GetMapping("/scraps")
  public ResponseEntity<Slice<ScrapResponseDto>> getAllScraps(
      Authentication authentication,
      @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

    Slice<ScrapResponseDto> response = readingLogService.getAllScraps(authentication, pageable);
    return ResponseEntity.ok(response);
  }

  /**
   * 도서 기준으로 그룹화된 스크랩 요약 목록을 조회하는 API
   *
   * @param sort           정렬 기준 (latest, oldest, count)
   * @param authentication JWT 인증 정보 (userId 추출용)
   * @return 도서별 스크랩 요약 리스트
   */
  @GetMapping("/scraps/group")
  public ResponseEntity<List<ScrapBookResponseDto>> getScrapSummaryGroup(
      @RequestParam(defaultValue = "latest") String sort,
      Authentication authentication) {

    User user = userService.getCurrentUser(authentication);
    List<ScrapBookResponseDto> result = readingLogService.getScrapSummaryGroup(user.getId(), sort);

    return ResponseEntity.ok(result);
  }

  /**
   * 독서로그 선택 삭제 API
   *
   * @param request        삭제할 로그 id 리스트
   * @param authentication JWT 인증 정보 (userId 추출용)
   * @return
   */
  @DeleteMapping
  public ResponseEntity<String> deleteMultipleLogs(
      @RequestBody LogDeleteRequestDto request,
      Authentication authentication
  ) {
    readingLogService.deleteMultipleLogs(request.getLogIds(), authentication);
    return ResponseEntity.ok("삭제되었습니다");
  }
}

