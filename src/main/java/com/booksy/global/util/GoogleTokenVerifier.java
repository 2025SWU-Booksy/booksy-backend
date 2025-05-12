package com.booksy.global.util;

import com.booksy.domain.oauth.dto.OAuthUserInfo;
import com.booksy.domain.user.entity.Provider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleTokenVerifier {

  @Value("${app.google.client.id}")
  private String googleClientId;

  /**
   * 구글 id Token을 검증하고 사용자 정보 추출
   *
   * @param idToken 프론트에서 받은 구글 idToken
   * @return OAuthUserInfo (email, providerUserId, provider)
   */
  public OAuthUserInfo verify(String idToken) {
    try {
      GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
          new NetHttpTransport(),
          JacksonFactory.getDefaultInstance()
      ).setAudience(Collections.singletonList(googleClientId))
          .build();

      GoogleIdToken googleIdToken = verifier.verify(idToken);
      if (googleIdToken == null) {
        throw new IllegalArgumentException("유효하지 않은 idToken입니다.");
      }

      Payload payload = googleIdToken.getPayload();
      String email = payload.getEmail();
      String sub = payload.getSubject(); // providerUserId

      return OAuthUserInfo.builder()
          .email(email)
          .providerUserId(sub)
          .provider(Provider.GOOGLE)
          .build();

    } catch (Exception e) {
      throw new RuntimeException("Google ID Token 검증 실패", e);
    }
  }
}