package com.booksy.domain.readinglog.service;

import com.booksy.domain.badge.service.BadgeService;
import com.booksy.domain.plan.entity.Plan;
import com.booksy.domain.plan.repository.PlanRepository;
import com.booksy.domain.readinglog.dto.*;
import com.booksy.domain.readinglog.entity.ReadingLog;
import com.booksy.domain.readinglog.mapper.ReadingLogMapper;
import com.booksy.domain.readinglog.repository.ReadingLogRepository;
import com.booksy.domain.readinglog.type.ContentType;
import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.service.UserService;
import com.booksy.global.error.ErrorCode;
import com.booksy.global.error.exception.ApiException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReadingLogService {

  private final PlanRepository planRepository;
  private final ReadingLogRepository readingLogRepository;
  private final ReadingLogMapper readingLogMapper;
  private final UserService userService;
  private final BadgeService badgeService;

  /**
   * 독서로그 생성
   *
   * @param planId 플랜Id
   * @param dto    ContentType(ENUM), content
   * @param auth   토큰으로 사용자 인증
   */
  public ReadingLogResponseDto createReadingLog(Long planId, ReadingLogRequestDto dto,
      Authentication auth) {
    if (dto.getContent() == null || dto.getContent().isBlank()) {
      throw new ApiException(ErrorCode.FIELD_REQUIRED);
    }

    Plan plan = getPlan(planId);
    User user = userService.getCurrentUser(auth);

    ReadingLog log = readingLogMapper.toEntity(dto, user, plan);
    ReadingLog savedLog = readingLogRepository.save(log);

    badgeService.evaluateReadingLogBadges(user, dto.getContentType());

    return readingLogMapper.toDto(savedLog);
  }


  /**
   * 플랜 존재 여부 확인
   */
  private Plan getPlan(Long planId) {
    return planRepository.findById(planId)
        .orElseThrow(() -> new ApiException(ErrorCode.ENTITY_NOT_FOUND));
  }

  /**
   * 독서로그 수정
   *
   * @param logId      독서로그 ID
   * @param newContent 변경할 내용
   * @param auth       토큰으로 사용자 인증
   */
  @Transactional
  public UpdateLogResponseDto updateReadingLog(Long logId, String newContent, Authentication auth) {
    User user = userService.getCurrentUser(auth);
    ReadingLog log = readingLogRepository.findById(logId)
        .orElseThrow(() -> new ApiException(ErrorCode.ENTITY_NOT_FOUND));

    if (newContent == null || newContent.isBlank()) {
      throw new ApiException(ErrorCode.FIELD_REQUIRED);
    }

    log.setContent(newContent);
    return readingLogMapper.toUpdateDto(log);
  }

  /**
   * 독서로그 삭제
   *
   * @param logId 독서로그 id
   * @param auth  사용자 인증
   */
  @Transactional
  public void deleteReadingLog(Long logId, Authentication auth) {
    User user = userService.getCurrentUser(auth);
    ReadingLog log = readingLogRepository.findById(logId)
        .orElseThrow(() -> new ApiException(ErrorCode.ENTITY_NOT_FOUND));

    readingLogRepository.delete(log);
  }

  /**
   * 특정 플랜에 포함된 독서로그 중, 요청한 타입(SCRAP or REVIEW)만 필터링하여 반환
   *
   * @param planId      조회할 플랜 ID
   * @param contentType 조회할 로그 타입 (SCRAP or REVIEW)
   * @return 필터링된 독서로그 리스트 (DTO)
   * @throws ApiException 권한이 없는 사용자가 접근 시 예외 발생
   */
  @Transactional(readOnly = true)
  public List<ReadingLogResponseDto> getLogsByPlanAndType(Long planId, ContentType contentType,
      Authentication auth) {
    User currentUser = userService.getCurrentUser(auth);

    Plan plan = planRepository.findById(planId)
        .orElseThrow(() -> new ApiException(ErrorCode.ENTITY_NOT_FOUND));

    if (!plan.getUser().getId().equals(currentUser.getId())) {
      throw new ApiException(ErrorCode.UNAUTHORIZED_ACCESS);
    }

    List<ReadingLog> logs = readingLogRepository.findAllByPlanIdAndContentType(planId, contentType);
    return logs.stream()
        .map(readingLogMapper::toDto)
        .collect(Collectors.toList());
  }

  /**
   * 독서로그 단건 조회 - 로그 ID로 하나의 독서로그(리뷰 or 스크랩)를 조회
   *
   * @param logId 조회할 독서로그의 ID
   * @param auth  로그인한 사용자 인증 정보 (JWT)
   * @return 해당 독서로그의 응답 DTO
   * @throws ApiException 권한이 없는 사용자가 접근 시 예외 발생
   */
  @Transactional(readOnly = true)
  public ReadingLogResponseDto getLogById(Long logId, Authentication auth) {
    User currentUser = userService.getCurrentUser(auth);

    ReadingLog log = readingLogRepository.findById(logId)
        .orElseThrow(() -> new ApiException(ErrorCode.ENTITY_NOT_FOUND));

    if (!log.getUser().getId().equals(currentUser.getId())) {
      throw new ApiException(ErrorCode.UNAUTHORIZED_ACCESS);
    }

    return readingLogMapper.toDto(log);
  }

  /**
   * 전체 스크랩 로그 조회 - contentType = SCRAP 인 항목만 필터링 - 책 제목과 작가는 plan → book 관계 통해 조회
   *
   * @param auth 로그인 사용자 인증 정보
   * @return 스크랩 응답 DTO 리스트
   */
  @Transactional(readOnly = true)
  public List<ScrapResponseDto> getAllScraps(Authentication auth) {
    User currentUser = userService.getCurrentUser(auth);

    List<ReadingLog> logs = readingLogRepository
        .findAllByUserIdAndContentType(currentUser.getId(), ContentType.SCRAP);

    return logs.stream()
        .map(log -> ScrapResponseDto.builder()
            .id(log.getId())
            .content(log.getContent())
            .bookTitle(log.getPlan().getBook().getTitle())
            .author(log.getPlan().getBook().getAuthor())
            .readingDate(log.getCreatedAt())
            .build())
        .toList();
  }

  /**
   * 도서 기준으로 그룹화된 스크랩 요약 목록을 정렬하여 반환
   *
   * @param userId 로그인한 사용자 ID
   * @param sort   정렬 기준: latest(기본), oldest, count(스크랩 수)
   * @return ScrapBookResponseDto 리스트
   */
  public List<ScrapBookResponseDto> getScrapSummaryGroup(Integer userId, String sort) {
    List<ScrapBookResponseDto> list = readingLogRepository.findScrapGroupedByBook(userId);

    switch (sort.toLowerCase()) {
      case "count":
        // 스크랩 수 내림차순
        list.sort(Comparator.comparingLong(ScrapBookResponseDto::getScrapCount).reversed());
        break;
      case "oldest":
        // 최신 스크랩 시각 오름차순
        list.sort(Comparator.comparing(ScrapBookResponseDto::getLatestScrap));
        break;
      case "latest":
      default:
        // 최신 스크랩 시각 내림차순 (기본값)
        list.sort(Comparator.comparing(ScrapBookResponseDto::getLatestScrap).reversed());
        break;
    }

    return list;
  }

  /**
   * @param logIds 로그ID
   * @param auth   로그인 사용자 인증 정보
   */
  public void deleteMultipleLogs(List<Long> logIds, Authentication auth) {
    User user = userService.getCurrentUser(auth);

    for (Long logId : logIds) {
      ReadingLog log = readingLogRepository.findById(logId)
          .orElseThrow(() -> new ApiException(ErrorCode.ENTITY_NOT_FOUND));

      if (!log.getPlan().getUser().getId().equals(user.getId())) {
        throw new ApiException(ErrorCode.UNAUTHORIZED_ACCESS);
      }

      readingLogRepository.delete(log);
    }
  }
}


