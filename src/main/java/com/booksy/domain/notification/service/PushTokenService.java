package com.booksy.domain.notification.service;

import com.booksy.domain.notification.entity.DeviceToken;
import com.booksy.domain.notification.repository.DeviceTokenRepository;
import com.booksy.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PushTokenService {

  private final DeviceTokenRepository deviceTokenRepository;

  public void saveOrUpdateToken(User user, String token) {
    deviceTokenRepository.findByToken(token).ifPresentOrElse(
      existing -> {
        if (!existing.getUser().getId().equals(user.getId())) {
          existing.setUser(user);
          deviceTokenRepository.save(existing);
        }
      },
      () -> {
        DeviceToken newToken = new DeviceToken();
        newToken.setUser(user);
        newToken.setToken(token);
        deviceTokenRepository.save(newToken);
      }
    );
  }
}
