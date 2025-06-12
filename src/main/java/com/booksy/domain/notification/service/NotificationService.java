package com.booksy.domain.notification.service;

import com.booksy.domain.notification.entity.DeviceToken;
import com.booksy.domain.notification.repository.DeviceTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final DeviceTokenRepository deviceTokenRepository;

  public void sendPushToUser(Integer userId, String title, String body) {
    List<DeviceToken> tokens = deviceTokenRepository.findAllByUserId(userId);

    for (DeviceToken token : tokens) {
      Message message = Message.builder()
        .setToken(token.getToken())
        .setNotification(Notification.builder()
          .setTitle(title)
          .setBody(body)
          .build())
        .build();

      try {
        String response = FirebaseMessaging.getInstance().send(message);
        System.out.println("✅ FCM 전송 성공: " + response);
      } catch (FirebaseMessagingException e) {
        System.out.println("❌ FCM 전송 실패: " + e.getMessage());
      }
    }
  }

  public void sendPushToToken(String token, String title, String body) {
    Notification notification = Notification.builder()
      .setTitle(title)
      .setBody(body)
      .build();

    Message message = Message.builder()
      .setToken(token)
      .setNotification(notification)
      .build();

    try {
      FirebaseMessaging.getInstance().send(message);
      log.info("✅ 푸시 전송 성공: {}", token);
    } catch (FirebaseMessagingException e) {
      log.error("❌ 푸시 전송 실패: {}", token, e);
    }
  }
}
