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
 * 네이버 로그인 서비스 로직 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NaverOAuthService {

  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final RestTemplate restTemplate = new RestTemplate();

  private OAuthUserInfo getUserInfoFromNaver(String accessToken) {
    String url = "https://openapi.naver.com/v1/nid/me";

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    ResponseEntity<Map> response = restTemplate.exchange(
        url, HttpMethod.GET, entity, Map.class);

    Map<String, Object> responseBody = (Map<String, Object>) response.getBody().get("response");

    return OAuthUserInfo.builder()
        .email((String) responseBody.get("email"))
        .providerUserId((String) responseBody.get("id"))
        .provider(Provider.NAVER)
        .build();
  }

  /**
   * 네이버 로그인 로직
   */
  public LoginResponse login(OAuthLoginRequestDto request) {
    try {
      OAuthUserInfo info = getUserInfoFromNaver(request.getAccessToken());

      Optional<User> userOpt = userRepository.findByProviderAndProviderUserId(
          info.getProvider(), info.getProviderUserId());

      if (userOpt.isEmpty()) {
        return new LoginResponse(
            ErrorCode.ENTITY_NOT_FOUND.getStatus(), "FAIL",
            "등록되지 않은 사용자입니다. 회원가입이 필요합니다.", null);
      }

      User user = userOpt.get();
      String token = jwtTokenProvider.generateToken(user.getId());

      if (user.getStatus() == UserStatus.INACTIVE) {
        String message = user.getUpdatedAt()
            .plusDays(7)
            .format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) + "에 계정이 삭제됩니다. 복구하시겠습니까?";

        return new LoginResponse(ErrorCode.HANDLE_ACCESS_DENIED.getStatus(), "INACTIVE", message,
            token);
      }

      return new LoginResponse(200, "SUCCESS", "로그인 되었습니다.", token);

    } catch (Exception e) {
      log.error("네이버 로그인 실패", e);
      return new LoginResponse(500, "ERROR", "로그인 중 오류 발생: " + e.getMessage(), null);
    }
  }

  /**
   * 네이버 회원가입 로직
   *
   * @param request
   * @return
   */
  @Transactional
  public SignupResponse signup(OAuthSignupRequestDto request) {
    try {
      OAuthUserInfo info = getUserInfoFromNaver(request.getAccessToken());

      if (userRepository.findByProviderAndProviderUserId(
          info.getProvider(), info.getProviderUserId()).isPresent()) {
        return new SignupResponse(409, "FAIL", "이미 가입된 사용자입니다. 로그인해주세요.");
      }

      String nickname = request.getNickname() != null
          ? request.getNickname()
          : info.getEmail().split("@")[0];

      User user = User.builder()
          .email(null)
          .provider(Provider.NAVER)
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
      log.error("네이버 회원가입 실패", e);
      return new SignupResponse(500, "ERROR", "회원가입 중 오류 발생: " + e.getMessage());
    }
  }
}
