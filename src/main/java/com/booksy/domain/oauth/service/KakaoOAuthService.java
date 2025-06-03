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

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final RestTemplate restTemplate = new RestTemplate();

  private OAuthUserInfo getUserInfoFromKakao(String accessToken) {
    String url = "https://kapi.kakao.com/v2/user/me";

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    ResponseEntity<Map> response = restTemplate.exchange(
        url, HttpMethod.GET, entity, Map.class);

    Map<String, Object> responseBody = response.getBody();
    if (responseBody == null || responseBody.get("id") == null) {
      throw new IllegalArgumentException("카카오 사용자 정보가 비어있습니다.");
    }

    String providerUserId = String.valueOf(responseBody.get("id"));
    // 이메일은 제공되지 않을 수 있으므로 null 처리
    String email = null;

    Map<String, Object> kakaoAccount = (Map<String, Object>) responseBody.get("kakao_account");
    if (kakaoAccount != null && kakaoAccount.get("email") instanceof String) {
      email = (String) kakaoAccount.get("email");
    }

    return OAuthUserInfo.builder()
        .provider(Provider.KAKAO)
        .providerUserId(providerUserId)
        .email(email)
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
        String message = user.getUpdatedAt()
            .plusDays(7)
            .format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) + "에 계정이 삭제됩니다. 복구하시겠습니까?";
        return new LoginResponse(ErrorCode.HANDLE_ACCESS_DENIED.getStatus(), "INACTIVE", message,
            token);
      }

      return new LoginResponse(200, "SUCCESS", "로그인 되었습니다.", token);
    } catch (Exception e) {
      log.error("카카오 로그인 실패", e);
      return new LoginResponse(500, "ERROR", "로그인 중 오류 발생: " + e.getMessage(), null);
    }
  }

  @Transactional
  public SignupResponse signup(OAuthSignupRequestDto request) {
    try {
      OAuthUserInfo info = getUserInfoFromKakao(request.getAccessToken());

      if (userRepository.findByProviderAndProviderUserId(
          info.getProvider(), info.getProviderUserId()).isPresent()) {
        return new SignupResponse(409, "FAIL", "이미 가입된 사용자입니다. 로그인해주세요.");
      }

      // 닉네임 없으면 자동 생성
      String nickname = request.getNickname();
      if (nickname == null || nickname.isBlank()) {
        int rand = (int) (Math.random() * 10000);
        nickname = "닉네임" + String.format("%04d", rand);
      }

      User user = User.builder()
          .email(null) // 이메일은 null 허용
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
      return new SignupResponse(500, "ERROR", "회원가입 중 오류 발생: " + e.getMessage());
    }
  }
}
