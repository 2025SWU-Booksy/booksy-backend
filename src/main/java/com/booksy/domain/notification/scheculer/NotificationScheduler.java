package com.booksy.domain.notification.scheculer;

import com.booksy.domain.notification.entity.DeviceToken;
import com.booksy.domain.notification.repository.DeviceTokenRepository;
import com.booksy.domain.notification.service.NotificationService;
import com.booksy.domain.plan.entity.Plan;
import com.booksy.domain.plan.repository.PlanRepository;
import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
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
  private final PlanRepository planRepository;

  private static final List<String> BODY_TEMPLATES = List.of(
    "‘%s’ 오늘도 한 챕터 도전해볼까요?",
    "‘%s’ 읽기 좋은 시간이에요. 지금 시작해볼까요?",
    "오늘은 ‘%s’ 먼저 보는 걸로~!",
    "‘%s’ 오늘 조금이라도 읽어보는 건 어때요?",
    "‘%s’이 오늘의 작은 휴식이 되어줄 거예요.",
    "‘%s’ 오늘 10분만 읽고 앱 꺼도 괜찮아요!",
    "‘%s’ 놓치면 아쉬운 오늘의 한 페이지!",
    "‘%s’ 오늘의 한 줄 기록도 잊지 마세요.",
    "‘%s’ 읽는 당신, 너무 멋져요!",
    "‘%s’ 이제 진짜 결말이 궁금하지 않아요?"
  );

  // 매일 특정 시간에 실행 (한국 시간)
  @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
  public void sendDailyPushNotification() {
    LocalDate today = LocalDate.now();
    List<User> users = userRepository.findByIsPushEnabledTrue();

    for (User user : users) {
      List<Plan> plans = planRepository.findTodayReadingPlans(user, today);

      if (plans.isEmpty()) {
        continue;
      }

      Plan plan = plans.get(0); // 랜덤으로 하나
      String bookTitle = plan.getBook().getTitle();

      // 랜덤 메시지 선택
      String bodyTemplate = BODY_TEMPLATES.get(
        ThreadLocalRandom.current().nextInt(BODY_TEMPLATES.size()));
      String body = String.format(bodyTemplate, bookTitle);
      String title = "📚 오늘의 독서 추천";

      List<DeviceToken> tokens = deviceTokenRepository.findAllByUserId(user.getId());
      for (DeviceToken token : tokens) {
        notificationService.sendPushToToken(token.getToken(), title, body);
      }
    }
  }
}
