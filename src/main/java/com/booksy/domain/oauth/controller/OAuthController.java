package com.booksy.domain.oauth.controller;

import com.booksy.domain.oauth.dto.OAuthLoginRequestDto;
import com.booksy.domain.oauth.dto.OAuthSignupRequestDto;
import com.booksy.domain.oauth.service.GoogleOAuthService;
import com.booksy.domain.oauth.service.NaverOAuthService;
import com.booksy.domain.user.dto.LoginResponse;
import com.booksy.domain.user.dto.SignupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OAuthController {

  private final GoogleOAuthService googleOAuthService;
  private final NaverOAuthService naverOAuthService;

  @PostMapping("/login/google")
  public ResponseEntity<LoginResponse> loginWithGoogle(
      @RequestBody OAuthLoginRequestDto request
  ) {
    return ResponseEntity.ok(googleOAuthService.login(request));
  }

  @PostMapping("/signup/google")
  public ResponseEntity<SignupResponse> signupWithGoogle(
      @RequestBody OAuthSignupRequestDto request
  ) {
    return ResponseEntity.ok(googleOAuthService.signup(request));
  }

  @PostMapping("/login/naver")
  public ResponseEntity<LoginResponse> loginWithNaver(
      @RequestBody OAuthLoginRequestDto request
  ) {
    return ResponseEntity.ok(naverOAuthService.login(request));
  }

  @PostMapping("/signup/naver")
  public ResponseEntity<SignupResponse> signupWithNaver(
      @RequestBody OAuthSignupRequestDto request
  ) {
    return ResponseEntity.ok(naverOAuthService.signup(request));
  }

  /**
   * 네이버 OAuth 콜백 핸들러 (백엔드 테스트용)
   */
  @GetMapping("/callback/naver")
  public ResponseEntity<?> naverCallback(@RequestParam String code, @RequestParam String state) {
    return ResponseEntity.ok("네이버에서 받은 code: " + code + ", state: " + state);
  }
}
