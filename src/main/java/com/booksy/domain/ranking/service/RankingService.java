package com.booksy.domain.ranking.service;

import com.booksy.domain.ranking.dto.MyRankingResponseDto;
import com.booksy.domain.ranking.dto.RankingResponseDto;
import com.booksy.domain.ranking.repository.RankingRepository;
import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.repository.UserRepository;
import com.booksy.global.error.ErrorCode;
import com.booksy.global.error.exception.ApiException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingService {

  private final RankingRepository rankingRepository;
  private final UserRepository userRepository;

  /**
   * 정렬 기준과 범위에 따라 랭킹 리스트 조회 (최대 50명)
   *
   * @param sort  정렬 기준 ("time", "count", "badge")
   * @param scope 조회 범위 ("month", "year")
   * @return 랭킹 응답 DTO 리스트 (rank 포함)
   */
  public List<RankingResponseDto> getRankingList(String sort, String scope) {
    validateSortAndScope(sort, scope); // 유효성 검사
    LocalDateTime start = calculateStartDate(scope); // 범위 시작 시점 계산
    PageRequest pageRequest = PageRequest.of(0, 50); // 상위 50명 제한

    List<RankingResponseDto> result;

    // 정렬 기준별로 데이터 조회
    switch (sort) {
      case "time" -> result = rankingRepository.getTop50ByReadingTime(start, pageRequest);
      case "count" -> result = rankingRepository.getTop50ByCompletedPlans(start, pageRequest);
      case "badge" -> result = rankingRepository.getTop50ByBadgeCount(start, pageRequest);
      default -> throw new ApiException(ErrorCode.INVALID_SORT_TYPE);
    }

    // 순위(rank) 부여 (1위부터 시작)
    for (int i = 0; i < result.size(); i++) {
      result.get(i).setRank(i + 1);
    }

    return result;
  }

  /**
   * 정렬 기준과 범위 값의 유효성 검사
   *
   * @param sort  정렬 기준 ("time", "count", "badge")
   * @param scope 범위 기준 ("month", "year")
   */
  private void validateSortAndScope(String sort, String scope) {
    if (!List.of("time", "count", "badge").contains(sort)) {
      throw new ApiException(ErrorCode.INVALID_SORT_TYPE);
    }
    if (!List.of("month", "year").contains(scope)) {
      throw new ApiException(ErrorCode.INVALID_SCOPE_TYPE);
    }
  }

  /**
   * 범위(scope)에 따라 시작일 계산
   *
   * @param scope "month" 또는 "year"
   * @return 해당 범위의 시작 시각 (LocalDateTime)
   */
  private LocalDateTime calculateStartDate(String scope) {
    LocalDate today = LocalDate.now();

    if (scope.equals("month")) {
      return today.withDayOfMonth(1).atStartOfDay(); // 이번 달 1일 00:00
    } else {
      return today.withDayOfYear(1).atStartOfDay(); // 올해 1월 1일 00:00
    }
  }

  public MyRankingResponseDto getMyRanking(String sort, String scope, Integer userId) {
    // 1. sort, scope 값 유효성 검사
    validateSortAndScope(sort, scope);

    // 2. 범위 시작일 계산 (이번 달 1일 or 올해 1월 1일)
    LocalDateTime start = calculateStartDate(scope);

    // 3. 조건에 맞는 전체 사용자 랭킹 데이터 조회 (userId 포함됨)
    List<RankingResponseDto> allRanking = switch (sort) {
      case "time" -> rankingRepository.getAllByReadingTime(start);
      case "count" -> rankingRepository.getAllByCompletedPlans(start);
      case "badge" -> rankingRepository.getAllByBadgeCount(start);
      default -> throw new ApiException(ErrorCode.INVALID_SORT_TYPE);
    };

    // 4. rank 값을 1부터 부여
    for (int i = 0; i < allRanking.size(); i++) {
      allRanking.get(i).setRank(i + 1);
    }

    // 5. 내 랭킹 정보 찾기
    RankingResponseDto me = allRanking.stream()
        .filter(r -> r.getUserId().equals(userId))
        .findFirst()
        .orElse(null);

    // 6. 기록 없는 사용자 처리
    if (me == null) {
      return new MyRankingResponseDto(
          -1,             // rank
          "",             // nickname
          "",             // profileImage
          0,              // level
          "-",            // value
          sort,           // valueType 그대로 전달
          100.0           // 상위 100%로 처리
      );
    }

    // 7. User 엔티티에서 level 가져오기
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ApiException(ErrorCode.ENTITY_NOT_FOUND));

    // 8. 상위 퍼센트 계산
    double percentile = roundToOneDecimal((double) me.getRank() / allRanking.size() * 100);

    // 9. 최종 DTO 응답 생성
    return new MyRankingResponseDto(
        me.getRank(),
        me.getNickname(),
        me.getProfileImage(),
        user.getLevel(),
        me.getValue(),
        me.getValueType(),
        percentile
    );
  }

  /**
   * 소수점 1자리로 반올림
   */
  private double roundToOneDecimal(double val) {
    return Math.round(val * 10) / 10.0;
  }
}
