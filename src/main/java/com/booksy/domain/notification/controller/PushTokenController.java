package com.booksy.domain.notification.controller;

import com.booksy.domain.notification.dto.PushTokenRequestDto;
import com.booksy.domain.notification.service.PushTokenService;
import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.service.UserService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/push")
public class PushTokenController {

  private final PushTokenService pushTokenService;
  private final UserService userService;

  @PostMapping("/token")
  public ResponseEntity<?> registerToken(@RequestBody PushTokenRequestDto dto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.getCurrentUser(authentication);

    pushTokenService.saveOrUpdateToken(user, dto.getToken());
    return ResponseEntity.ok(Map.of("message", "FCM token saved"));
  }

}
