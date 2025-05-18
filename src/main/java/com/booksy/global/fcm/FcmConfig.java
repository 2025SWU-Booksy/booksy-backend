package com.booksy.global.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FcmConfig {

  @PostConstruct
  public void initialize() throws IOException {
    if (FirebaseApp.getApps().isEmpty()) {
      InputStream serviceAccount =
          new ClassPathResource("firebase/swu-booksy-d87a9c837a1b.json").getInputStream();

      FirebaseOptions options = FirebaseOptions.builder()
          .setCredentials(GoogleCredentials.fromStream(serviceAccount))
          .build();

      FirebaseApp.initializeApp(options);
      System.out.println("✅ Firebase 초기화 완료");
    }
  }
}
