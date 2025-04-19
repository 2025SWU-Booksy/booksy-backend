package com.booksy.domain.user.controller;

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

  @PostMapping("/signup")
  public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest request) {
    SignupResponse response = userService.signup(request);
    return ResponseEntity.status(response.getCode()).body(response);
  }
}