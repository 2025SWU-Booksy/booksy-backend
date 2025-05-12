package com.booksy.domain.oauth.service;

import com.booksy.domain.oauth.dto.OAuthLoginRequestDto;
import com.booksy.domain.oauth.dto.OAuthSignupRequestDto;
import com.booksy.domain.oauth.dto.OAuthUserInfo;
import com.booksy.domain.user.dto.LoginResponse;
import com.booksy.domain.user.dto.SignupResponse;
import com.booksy.domain.user.entity.Provider;
import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.entity.UserStatus;
import com.booksy.domain.user.repository.UserRepository;
import com.booksy.global.error.ErrorCode;
import com.booksy.global.util.JwtTokenProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 구글 로그인 서비스 로직 클래스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthService {

  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;

  @Value("${app.google.client.id}")
  private String googleClientId;

  /**
   * ID 토큰 검증 후 사용자 정보 반환
   */
  private OAuthUserInfo verifyIdToken(String idToken) throws GeneralSecurityException {
    try {
      GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
          new NetHttpTransport(),
          JacksonFactory.getDefaultInstance())
          .setAudience(Collections.singletonList(googleClientId))
          .build();

      GoogleIdToken googleIdToken = verifier.verify(idToken);
      if (googleIdToken == null) {
        throw new IllegalArgumentException("유효하지 않은 ID 토큰입니다.");
      }

      Payload payload = googleIdToken.getPayload();
      return OAuthUserInfo.builder()
          .email(payload.getEmail())
          .providerUserId(payload.getSubject())
          .provider(Provider.GOOGLE)
          .build();

    } catch (Exception e) {
      log.error("Google ID 토큰 검증 실패", e);
      throw new IllegalArgumentException("ID 토큰 검증 중 오류가 발생했습니다.");
    }
  }

  /**
   * 로그인 처리
   */
  public LoginResponse login(OAuthLoginRequestDto request) {
    try {
      OAuthUserInfo info = verifyIdToken(request.getAccessToken());

      Optional<User> userOpt = userRepository.findByProviderAndProviderUserId(
          info.getProvider(), info.getProviderUserId());

      if (userOpt.isEmpty()) {
        return new LoginResponse(
            ErrorCode.ENTITY_NOT_FOUND.getStatus(),
            "FAIL",
            "등록되지 않은 사용자입니다. 회원가입이 필요합니다.",
            null
        );
      }

      User user = userOpt.get();
      String token = jwtTokenProvider.generateToken(user.getId());

      if (user.getStatus() == UserStatus.INACTIVE) {
        LocalDateTime deletionDate = user.getUpdatedAt().plusDays(7);
        String message = deletionDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            + "에 계정이 삭제됩니다. 복구하시겠습니까?";

        return new LoginResponse(
            ErrorCode.HANDLE_ACCESS_DENIED.getStatus(),
            "INACTIVE",
            message,
            token
        );
      }

      return new LoginResponse(200, "SUCCESS", "로그인 되었습니다.", token);

    } catch (Exception e) {
      log.error("소셜 로그인 실패", e);
      return new LoginResponse(
          ErrorCode.INTERNAL_SERVER_ERROR.getStatus(),
          "ERROR",
          "로그인 중 오류 발생: " + e.getMessage(),
          null
      );
    }
  }

  /**
   * 회원가입 처리
   */
  @Transactional
  public SignupResponse signup(OAuthSignupRequestDto request) {
    try {
      OAuthUserInfo info = verifyIdToken(request.getAccessToken());

      if (userRepository.findByProviderAndProviderUserId(
          info.getProvider(), info.getProviderUserId()).isPresent()) {
        return new SignupResponse(
            ErrorCode.DUPLICATE_RESOURCE.getStatus(),
            "FAIL",
            "이미 가입된 사용자입니다. 로그인해주세요."
        );
      }

      // 동일 이메일로 일반 회원가입 되어있을 때
      if (userRepository.findByEmail(info.getEmail()).isPresent()) {
        return new SignupResponse(
            ErrorCode.DUPLICATE_RESOURCE.getStatus(),
            "FAIL",
            "이미 해당 이메일로 가입된 계정이 존재합니다."
        );
      }

      String nickname = request.getNickname() != null
          ? request.getNickname()
          : info.getEmail().split("@")[0];

      User user = User.builder()
          .email(info.getEmail())
          .provider(Provider.GOOGLE)
          .providerUserId(info.getProviderUserId())
          .nickname(nickname)
          .age(request.getAge())
          .gender(request.getGender())
          .profileImage(request.getProfileImage())
          .isPushEnabled(true)
          .status(UserStatus.ACTIVE)
          .build();

      userRepository.save(user);

      // 선호 카테고리 추후 추가

      return new SignupResponse(201, "SUCCESS", "회원가입이 완료되었습니다.");

    } catch (Exception e) {
      log.error("소셜 회원가입 실패", e);
      return new SignupResponse(
          ErrorCode.INTERNAL_SERVER_ERROR.getStatus(),
          "ERROR",
          "회원가입 중 오류 발생: " + e.getMessage()
      );
    }
  }
}