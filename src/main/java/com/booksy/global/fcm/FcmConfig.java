package com.booksy.global.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FcmConfig {

  @PostConstruct
  public void initialize() throws IOException {
    if (FirebaseApp.getApps().isEmpty()) {
      FileInputStream serviceAccount =
        new FileInputStream("src/main/resources/firebase/swu-booksy-d87a9c837a1b.json");

      FirebaseOptions options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build();

      FirebaseApp.initializeApp(options);
      System.out.println("✅ Firebase 초기화 완료");
    }
  }
}
