package com.booksy.domain.plan.controller;

import com.booksy.domain.plan.dto.PlanCreateRequestDto;
import com.booksy.domain.plan.dto.PlanPreviewResponseDto;
import com.booksy.domain.plan.dto.PlanResponseDto;
import com.booksy.domain.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 플랜 관련 API를 제공하는 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plans")
public class PlanController {

  private final PlanService planService;

  /**
   * 플랜 미리보기 API
   *
   * 사용자가 입력한 조건을 바탕으로 플랜 예상 결과를 계산해 반환
   *
   * @param requestDto 플랜 생성 요청 정보
   * @return PlanPreviewResponseDto (계산된 추천 결과)
   */
  @PostMapping("/preview")
  public ResponseEntity<PlanPreviewResponseDto> previewPlan(
      @RequestBody PlanCreateRequestDto requestDto
  ) {
    PlanPreviewResponseDto preview = planService.previewPlan(requestDto);
    return ResponseEntity.ok(preview);
  }

  /**
   * 플랜 생성 API
   *
   * 사용자가 최종 저장 요청 시 플랜을 DB에 저장
   *
   * @param requestDto 플랜 생성 요청 정보
   * @return PlanResponseDto (저장된 플랜 요약 정보)
   */
  @PostMapping
  public ResponseEntity<PlanResponseDto> createPlan(
      @RequestBody PlanCreateRequestDto requestDto
  ) {
    PlanResponseDto savedPlan = planService.createPlan(requestDto);
    return ResponseEntity.ok(savedPlan);
  }

}
