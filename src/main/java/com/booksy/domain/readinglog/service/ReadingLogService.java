package com.booksy.domain.readinglog.service;

import com.booksy.domain.plan.entity.Plan;
import com.booksy.domain.plan.repository.PlanRepository;
import com.booksy.domain.readinglog.dto.ReadingLogRequestDto;
import com.booksy.domain.readinglog.entity.ReadingLog;
import com.booksy.domain.readinglog.mapper.ReadingLogMapper;
import com.booksy.domain.readinglog.repository.ReadingLogRepository;
import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
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

  /**
   * 독서로그 생성
   *
   * @param planId 플랜Id
   * @param dto    ContentType(ENUM), content
   * @param auth   토큰으로 사용자 인증
   */
  public void createReadingLog(Long planId, ReadingLogRequestDto dto, Authentication auth) {
    if (dto.getContent() == null || dto.getContent().isBlank()) {
      throw new IllegalArgumentException("내용이 비어있습니다.");
    }

    Plan plan = getPlan(planId);
    User user = userService.getCurrentUser(auth);

    ReadingLog log = readingLogMapper.toEntity(dto, user, plan);
    readingLogRepository.save(log);
  }

  /**
   * 플랜 존재 여부 확인
   */
  private Plan getPlan(Long planId) {
    return planRepository.findById(planId)
        .orElseThrow(() -> new EntityNotFoundException("해당 플랜이 존재하지 않습니다. ID: " + planId));
  }

  /**
   * @param logId      독서로그 ID
   * @param newContent 변경할 내용
   * @param auth       토큰으로 사용자 인증
   */
  @Transactional
  public void updateReadingLog(Long logId, String newContent, Authentication auth) {
    User user = userService.getCurrentUser(auth);
    ReadingLog log = readingLogRepository.findById(logId)
        .orElseThrow(() -> new EntityNotFoundException("해당 로그가 없습니다."));

    if (newContent == null || newContent.isBlank()) {
      throw new IllegalArgumentException("내용이 비어 있습니다.");
    }

    log.setContent(newContent);
  }

  @Transactional
  public void deleteReadingLog(Long logId, Authentication auth) {
    User user = userService.getCurrentUser(auth);
    ReadingLog log = readingLogRepository.findById(logId)
        .orElseThrow(() -> new EntityNotFoundException("해당 로그가 없습니다."));

    readingLogRepository.delete(log);
  }
}
