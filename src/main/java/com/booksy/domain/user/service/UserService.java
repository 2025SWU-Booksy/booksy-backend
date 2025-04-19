package com.booksy.domain.user.service;

import com.booksy.domain.user.dto.SignupRequest;
import com.booksy.domain.user.dto.SignupResponse;
import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.entity.UserStatus;
import com.booksy.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * 회원가입 처리 - 이메일 중복 확인 - 닉네임 null이면 이메일로 대체 - 비밀번호 해시 - UserStatus는 ACTIVE로 설정 - 유저 저장 - 응답 메시지
   * 리턴
   */
  public SignupResponse signup(SignupRequest request) {

    // 이메일 중복 확인
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      return new SignupResponse(400, "FAIL", "이미 존재하는 이메일입니다.");
    }

    // 닉네임이 null이면 → 이메일로 대체
    String nickname = request.getNickname() != null
        ? request.getNickname()
        : request.getEmail();

    // 비밀번호 암호화
    String encodedPassword = passwordEncoder.encode(request.getPassword());

    // User 엔티티 생성
    User user = User.builder()
        .email(request.getEmail())
        .password(encodedPassword)
        .age(request.getAge())
        .gender(request.getGender())
        .nickname(nickname)
        .profileImage(request.getProfileImage())
        .status(UserStatus.ACTIVE)
        .isPushEnabled(true)
        .build();

    // 저장
    userRepository.save(user);

    // 응답 리턴
    return new SignupResponse(200, "SUCCESS", "회원가입이 성공했습니다.");
  }
}