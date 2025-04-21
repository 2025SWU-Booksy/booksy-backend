package com.booksy.domain.user.controller;

import com.booksy.domain.user.dto.LoginRequest;
import com.booksy.domain.user.dto.LoginResponse;
import com.booksy.domain.user.dto.SignupRequest;
import com.booksy.domain.user.dto.SignupResponse;
import com.booksy.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}