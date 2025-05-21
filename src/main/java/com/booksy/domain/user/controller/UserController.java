package com.booksy.domain.user.controller;

import com.booksy.domain.user.dto.*;
import com.booksy.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  /**
   * 회원가입 API
   *
   * @param request 로그인 요청 dto
   * @return 로그인 처리 결과
   */
  @PostMapping("/signup")
  public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest request) {
    SignupResponse response = userService.signup(request);
    return ResponseEntity.status(response.getCode()).body(response);
  }

  /**
   * 로그인 API
   *
   * @param request 로그인 요청 DTO
   * @return 로그인 결과 응답(JWT 토큰 포함)
   */
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    LoginResponse response = userService.login(request);
    return ResponseEntity.status(response.getCode()).body(response);
  }

  /**
   * 내 정보 조회 API
   *
   * @return 사용자 정보 반환
   */
  @GetMapping("/users/me")
  public ResponseEntity<InfoResponse> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
    Integer userId = Integer.parseInt(userDetails.getUsername()); // JWT에서 꺼낸 userId

    InfoResponse response = userService.getMyInfo(userId);
    return ResponseEntity.ok(response);
  }

  /**
   * 내 정보 수정 API
   *
   * @param request        수정 요청 DTO
   * @param authentication 현재 인증된 사용자 정보 (JWT 토큰 기반)
   * @return 수정 완료
   */
  @PatchMapping("/users/me")
  public ResponseEntity<UpdateUserResponse> updateMyInfo(@RequestBody UpdateUserRequest request,
      Authentication authentication) {
    Integer userId = Integer.parseInt(authentication.getName());

    UpdateUserResponse response = userService.updateUserInfo(userId, request);

    return ResponseEntity
        .status(response.getStatus())
        .body(response);
  }

  /**
   * 사용자 탈퇴 API
   */
  @PatchMapping("/users/me/inactive")
  public ResponseEntity<String> deactivateUser(Authentication authentication) {
    Integer userId = Integer.parseInt(authentication.getName());
    userService.deactivateUser(userId);
    return ResponseEntity.ok("회원 탈퇴 처리되었습니다.");
  }

  /**
   * 사용자 복구 API
   */
  @PatchMapping("/users/me/restore")
  public ResponseEntity<String> restoreUser(Authentication authentication) {
    Integer userId = Integer.parseInt(authentication.getName());
    userService.restoreUser(userId);
    return ResponseEntity.ok("회원 복구 처리되었습니다.");
  }

  /**
   * 마이페이지 상단 정보 조회
   */
  @GetMapping("/users/mypage")
  public ResponseEntity<MyPageResponse> getMyPageInfo(Authentication authentication) {
    Integer userId = Integer.parseInt(authentication.getName());
    MyPageResponse response = userService.getMyPageInfo(userId);
    return ResponseEntity.ok(response);
  }

  /**
   * 랭킹 페이지의 타 회원 정보 조회
   */
  @GetMapping("/users/{userId}")
  public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Integer userId) {
    return ResponseEntity.ok(userService.getUserProfile(userId));
  }
}