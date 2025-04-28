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
  public ResponseEntity<String> updateMyInfo(@RequestBody UpdateUserRequest request,
      Authentication authentication) {
    // JWT 토큰에서 userId 가져오기
    Integer userId = Integer.parseInt(authentication.getName());

    // 서비스 호출 (수정)
    userService.updateUserInfo(userId, request);

    return ResponseEntity.ok("내 정보 수정이 완료되었습니다.");
  }
}