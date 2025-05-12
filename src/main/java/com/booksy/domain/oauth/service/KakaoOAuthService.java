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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * 카카오 로그인 서비스 로직 클래스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuthService {

  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final RestTemplate restTemplate = new RestTemplate();

  private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

  private OAuthUserInfo getUserInfoFromKakao(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);

    HttpEntity<Void> entity = new HttpEntity<>(headers);

    ResponseEntity<Map> response = restTemplate.exchange(
        KAKAO_USER_INFO_URL,
        HttpMethod.GET,
        entity,
        Map.class
    );

    Map body = response.getBody();
    if (body == null) {
      throw new IllegalArgumentException("카카오 응답이 비어있음");
    }

    Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
    String email = (String) kakaoAccount.get("email"); // nullable 허용됨
    String id = String.valueOf(body.get("id"));

    return OAuthUserInfo.builder()
        .email(email)
        .providerUserId(id)
        .provider(Provider.KAKAO)
        .build();
  }

  public LoginResponse login(OAuthLoginRequestDto request) {
    try {
      OAuthUserInfo info = getUserInfoFromKakao(request.getAccessToken());

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
      log.error("카카오 로그인 실패", e);
      return new LoginResponse(
          ErrorCode.INTERNAL_SERVER_ERROR.getStatus(),
          "ERROR",
          "로그인 중 오류 발생: " + e.getMessage(),
          null
      );
    }
  }

  @Transactional
  public SignupResponse signup(OAuthSignupRequestDto request) {
    try {
      OAuthUserInfo info = getUserInfoFromKakao(request.getAccessToken());

      if (userRepository.findByProviderAndProviderUserId(
          info.getProvider(), info.getProviderUserId()).isPresent()) {
        return new SignupResponse(
            ErrorCode.DUPLICATE_RESOURCE.getStatus(),
            "FAIL",
            "이미 가입된 사용자입니다. 로그인해주세요."
        );
      }

      String nickname = request.getNickname() != null
          ? request.getNickname()
          : (info.getEmail() != null ? info.getEmail().split("@")[0] : "카카오회원");

      User user = User.builder()
          .email(info.getEmail())
          .provider(Provider.KAKAO)
          .providerUserId(info.getProviderUserId())
          .nickname(nickname)
          .age(request.getAge())
          .gender(request.getGender())
          .profileImage(request.getProfileImage())
          .isPushEnabled(true)
          .status(UserStatus.ACTIVE)
          .build();

      userRepository.save(user);

      return new SignupResponse(201, "SUCCESS", "회원가입이 완료되었습니다.");

    } catch (Exception e) {
      log.error("카카오 회원가입 실패", e);
      return new SignupResponse(
          ErrorCode.INTERNAL_SERVER_ERROR.getStatus(),
          "ERROR",
          "회원가입 중 오류 발생: " + e.getMessage()
      );
    }
  }
}
