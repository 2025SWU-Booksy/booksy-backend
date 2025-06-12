package com.booksy.domain.notification.scheculer;

import com.booksy.domain.notification.entity.DeviceToken;
import com.booksy.domain.notification.repository.DeviceTokenRepository;
import com.booksy.domain.notification.service.NotificationService;
import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

  private final UserRepository userRepository;
  private final DeviceTokenRepository deviceTokenRepository;
  private final NotificationService notificationService;

  // 매일 특정 시간에 실행 (한국 시간)
  @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
  public void sendDailyPushNotification() {
    log.info("🕒 [푸시 스케줄 실행] sendDailyPushNotification() 실행됨");
    List<User> users = userRepository.findByIsPushEnabledTrue();

    for (User user : users) {
      List<DeviceToken> tokens = deviceTokenRepository.findAllByUserId(user.getId());

      for (DeviceToken token : tokens) {
        notificationService.sendPushToToken(
          token.getToken(),
          "📚 독서 시간이에요!",
          "오늘도 10분 독서로 습관을 쌓아보세요!"
        );
      }
    }
  }
}
