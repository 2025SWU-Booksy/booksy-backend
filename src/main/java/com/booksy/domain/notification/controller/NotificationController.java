package com.booksy.domain.notification.controller;

import com.booksy.domain.notification.dto.NotificationRequestDto;
import com.booksy.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/push")
public class NotificationController {

  private final NotificationService notificationService;

  @PostMapping("/notify")
  public ResponseEntity<?> sendPushNotification(@RequestBody NotificationRequestDto dto) {
    notificationService.sendPushToUser(dto.getUserId(), dto.getTitle(), dto.getBody());
    return ResponseEntity.ok().build();
  }
}
