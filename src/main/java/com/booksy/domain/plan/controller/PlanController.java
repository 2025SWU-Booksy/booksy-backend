package com.booksy.domain.plan.controller;

import com.booksy.domain.plan.dto.PlanCreateRequestDto;
import com.booksy.domain.plan.dto.PlanDetailResponseDto;
import com.booksy.domain.plan.dto.PlanExtendRequestDto;
import com.booksy.domain.plan.dto.PlanPreviewResponseDto;
import com.booksy.domain.plan.dto.PlanResponseDto;
import com.booksy.domain.plan.dto.PlanSummaryResponseDto;
import com.booksy.domain.plan.service.PlanService;
import com.booksy.domain.plan.type.PlanStatus;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  /**
   * 전체 플랜 목록 조회
   *
   * @return 로그인 사용자의 모든 플랜
   */
  @GetMapping("/all")
  public ResponseEntity<List<PlanResponseDto>> getAllPlans() {
    return ResponseEntity.ok(planService.getAllPlans());
  }

  /**
   * 플랜 목록 상태별 조회 API
   *
   * @param status 플랜 상태 (예: READING, COMPLETED)
   * @return 해당 상태에 해당하는 플랜 목록
   */
  @GetMapping
  public ResponseEntity<List<PlanResponseDto>> getPlansByStatus(
    @RequestParam PlanStatus status
  ) {
    List<PlanResponseDto> plans = planService.getPlansByStatus(status);
    return ResponseEntity.ok(plans);
  }

  /**
   * 오늘 읽을 책 요약 정보 조회 API
   *
   * @return 오늘 읽을 플랜 목록
   */
  @GetMapping("/summary")
  public ResponseEntity<List<PlanSummaryResponseDto>> getTodayPlans() {
    return ResponseEntity.ok(planService.getTodayPlanSummaries());
  }

  /**
   * 특정 플랜 상세 정보 조회
   *
   * @param planId 플랜 ID
   * @return 플랜 상세 응답 DTO
   */
  @GetMapping("/{planId}")
  public ResponseEntity<PlanDetailResponseDto> getPlanDetail(@PathVariable Long planId) {
    PlanDetailResponseDto responseDto = planService.getPlanDetail(planId);
    return ResponseEntity.ok(responseDto);
  }

  /**
   * 달력에 표시할 플랜 목록 조회
   *
   * @param year  연도
   * @param month 월 (1~12)
   * @return 해당 월 동안 진행되는 플랜 리스트
   */
  @GetMapping("/calendar")
  public ResponseEntity<List<PlanSummaryResponseDto>> getPlansForCalendar(
    @RequestParam int year,
    @RequestParam int month) {
    return ResponseEntity.ok(planService.getPlansForCalendar(year, month));
  }

  /**
   * 특정 날짜에 진행 중인 플랜 조회
   *
   * @param date 날짜 (형식: yyyy-MM-dd)
   * @return 해당 날짜에 진행 중인 플랜 리스트
   */
  @GetMapping("/calendar/{date}")
  public ResponseEntity<List<PlanSummaryResponseDto>> getPlansByDate(
    @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return ResponseEntity.ok(planService.getPlansByDate(date));
  }

  /**
   * 플랜 상태를 중도 포기로 변경
   *
   * @param planId 플랜 ID
   * @return 204 No Content
   */
  @PatchMapping("/{planId}/withdraw")
  public ResponseEntity<Void> abandonPlan(@PathVariable Long planId) {
    planService.abandonPlan(planId);
    return ResponseEntity.noContent().build();
  }

  /**
   * 플랜 종료일 연장 API
   *
   * @param planId     연장할 플랜 ID (PathVariable)
   * @param requestDto 새 종료일 정보가 담긴 요청 바디
   * @return 200 OK (성공 시 응답 본문 없음)
   */
  @PatchMapping("/{planId}/extend")
  public ResponseEntity<Void> extendPlan(
    @PathVariable Long planId,
    @RequestBody @Valid PlanExtendRequestDto requestDto) {
    planService.extendPlan(planId, requestDto.getNewEndDate());
    return ResponseEntity.ok().build();
  }

  /**
   * 플랜 단일 삭제 API
   *
   * @param id 삭제할 플랜 ID
   * @return 204 No Content
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
    planService.deletePlan(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * 플랜 다중 삭제 API
   *
   * @param ids 삭제할 플랜 ID 리스트 (예: ?ids=1,2,3)
   * @return 204 No Content
   */
  @DeleteMapping
  public ResponseEntity<Void> deletePlans(@RequestParam List<Long> ids) {
    planService.deletePlans(ids);
    return ResponseEntity.noContent().build();
  }


  @PostMapping("/wishlist/{isbn}")
  public ResponseEntity<Void> addToWishlist(@PathVariable String isbn) {
    planService.addToWishlist(isbn);
    return ResponseEntity.ok().build();
  }
  
  @DeleteMapping("/wishlist/{isbn}")
  public ResponseEntity<Void> removeFromWishlist(@PathVariable String isbn) {
    planService.removeFromWishlist(isbn);
    return ResponseEntity.noContent().build();
  }
}
