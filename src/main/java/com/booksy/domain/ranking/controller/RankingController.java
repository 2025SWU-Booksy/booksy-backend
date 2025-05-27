package com.booksy.domain.ranking.controller;

import com.booksy.domain.ranking.dto.MyRankingResponseDto;
import com.booksy.domain.ranking.dto.RankingResponseDto;
import com.booksy.domain.ranking.service.RankingService;
import com.booksy.domain.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

  private final RankingService rankingService;
  private final UserService userService;

  /**
   * 랭킹 목록 조회 API
   *
   * @param sort  정렬 기준 (time, count, badge)
   * @param scope 조회 범위 (month, year)
   * @return 랭킹 응답 DTO 리스트 (상위 50명, 랭크 포함)
   */
  @GetMapping
  public List<RankingResponseDto> getRankingList(
      @RequestParam String sort,
      @RequestParam String scope
  ) {
    return rankingService.getRankingList(sort, scope);
  }

  /**
   * 내 랭킹 정보 조회 API
   *
   * @param sort  정렬 기준 (time, count, badge)
   * @param scope 조회 범위 (month, year)
   * @return 나의 랭킹 정보 (랭크, 닉네임, 프로필, 레벨, 정렬 기준 값, 상위 퍼센트)
   */
  @GetMapping("/me")
  public MyRankingResponseDto getMyRanking(
      @RequestParam String sort,
      @RequestParam String scope,
      Authentication authentication
  ) {
    Integer userId = userService.getCurrentUser(authentication).getId(); // JWT 기반 userId 추출
    return rankingService.getMyRanking(sort, scope, userId);
  }
}