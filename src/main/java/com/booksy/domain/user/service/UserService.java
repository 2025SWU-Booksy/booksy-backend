package com.booksy.domain.user.service;

import com.booksy.domain.badge.repository.UserBadgeRepository;
import com.booksy.domain.category.entity.Category;
import com.booksy.domain.category.entity.UserCategory;
import com.booksy.domain.category.repository.CategoryRepository;
import com.booksy.domain.plan.repository.PlanRepository;
import com.booksy.domain.plan.type.PlanStatus;
import com.booksy.domain.readinglog.repository.TimeRecordRepository;
import com.booksy.domain.user.dto.*;
import com.booksy.domain.user.entity.User;
import com.booksy.domain.user.entity.UserStatus;
import com.booksy.domain.user.repository.UserRepository;
import com.booksy.global.error.ErrorCode;
import com.booksy.global.error.exception.ApiException;
import com.booksy.global.util.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관련 비즈니스 로직 처리 클래스
 */
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final CategoryRepository categoryRepository;
  private final UserBadgeRepository userBadgeRepository;
  private final TimeRecordRepository timeRecordRepository;
  private final PlanRepository planRepository;

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
        .favoriteGenres(new ArrayList<>())
        .build();

    // 선호 장르 저장
    if (request.getPreferredCategoryIds() != null) {
      updatePreferredGenres(user, request.getPreferredCategoryIds());
    }

    // 저장
    userRepository.save(user);

    // 응답 리턴
    return new SignupResponse(200, "SUCCESS", "회원가입이 성공했습니다.");
  }

  /**
   * 선호장르 저장
   */
  @Transactional
  public void updatePreferredGenres(User user, List<Long> categoryIds) {
    user.getFavoriteGenres().clear();

    List<UserCategory> newFavorites = categoryIds.stream()
        .map(categoryId -> {
          Category category = categoryRepository.findById(categoryId)
              .orElseThrow(() -> new ApiException(ErrorCode.ENTITY_NOT_FOUND));
          return UserCategory.builder()
              .user(user)
              .category(category)
              .build();
        })
        .toList();

    user.getFavoriteGenres().addAll(newFavorites);
  }

  /**
   * 로그인 처리
   */

  public LoginResponse login(LoginRequest request) {

    // 이메일로 사용자 검색
    Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

    if (userOptional.isEmpty()) {
      return new LoginResponse(401, "FAIL", "존재하지 않는 이메일입니다.", null);
    }

    User user = userOptional.get();

    // 비밀번호 검증
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      return new LoginResponse(401, "FAIL", "비밀번호가 일치하지 않습니다.", null);
    }

    // JWT 토큰 생성 (INACTIVE 상태에서도 토큰 발급)
    String token = jwtTokenProvider.generateToken(user.getId());

    // INACTIVE 상태 체크
    if (user.getStatus() == UserStatus.INACTIVE) {
      // 삭제 예정일 계산: updatedAt + 7일
      LocalDateTime deletionDate = user.getUpdatedAt().plusDays(7);
      String formattedDate = deletionDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
      String message = formattedDate + "에 계정이 삭제됩니다. 복구하시겠습니까?";

      // 응답 반환 (토큰 포함)
      return new LoginResponse(403, "INACTIVE", message, token);
    }

    // 응답 반환
    return new LoginResponse(200, "SUCCESS", "로그인 되었습니다.", token);
  }

  /**
   * 내 정보 조회
   */
  public InfoResponse getMyInfo(Integer userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

    // 유저 선호 장르 ID 리스트 추출
    List<Long> preferredCategoryIds = user.getFavoriteGenres().stream()
        .map(userCategory -> userCategory.getCategory().getId())
        .toList();

    return InfoResponse.builder()
        .email(user.getEmail())
        .nickname(user.getNickname())
        .age(user.getAge())
        .gender(user.getGender())
        .profileImage(user.getProfileImage())
        .preferredCategoryIds(preferredCategoryIds)
        .build();
  }

  /**
   * 사용자 정보 수정 비밀번호 변경 시 현재 비밀번호 확인 후 새 비밀번호로 변경 정보값이 있을 때만 업데이트
   */
  @Transactional
  public UpdateUserResponse updateUserInfo(Integer userId, UpdateUserRequest request) {
    try {
      // 사용자 조회
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

      // 새 비밀번호가 입력됐을 때만 처리
      if (request.getNewPassword() != null) {
        // 현재 비밀번호 확인 필수
        if (request.getCurrentPassword() == null) {
          return new UpdateUserResponse(400, "FAIL", "비밀번호 변경을 위해서는 현재 비밀번호를 입력해야 합니다.");
        }

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
          return new UpdateUserResponse(400, "FAIL", "현재 비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호 암호화 후 저장
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.updatePassword(encodedPassword);
      }

      // 닉네임 업데이트
      if (request.getNickname() != null) {
        user.updateNickname(request.getNickname());
      }

      // 나이 업데이트
      if (request.getAge() != null) {
        user.updateAge(request.getAge());
      }

      // 성별 업데이트
      if (request.getGender() != null) {
        user.updateGender(request.getGender());
      }

      // 선호 장르
      if (request.getPreferredCategoryIds() != null) {
        updatePreferredGenres(user, request.getPreferredCategoryIds());
      }

      userRepository.save(user);

      return new UpdateUserResponse(200, "SUCCESS", "사용자 정보가 업데이트되었습니다.");

    } catch (UsernameNotFoundException e) {
      return new UpdateUserResponse(404, "FAIL", e.getMessage());
    }
  }

  /**
   * 사용자 탈퇴 처리
   */
  public void deactivateUser(Integer userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

    user.updateStatus(UserStatus.INACTIVE); // 상태를 INACTIVE로 변경
    userRepository.save(user);
  }

  /**
   * 사용자 복구 처리
   */
  public void restoreUser(Integer userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

    user.updateStatus(UserStatus.ACTIVE);
    userRepository.save(user);
  }

  /**
   * 현재 로그인한 사용자의 엔티티를 반환
   */

  public User getCurrentUser(Authentication authentication) {
    String userId = authentication.getName(); // 토큰에 저장된 userId 추출
    return userRepository.findById(Integer.parseInt(userId))
        .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
  }

  /**
   * 마이페이지 상단 요약 정보 조회 메서드
   */

  public MyPageResponse getMyPageInfo(Integer userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ApiException(ErrorCode.ENTITY_NOT_FOUND));

    int badgeCount = userBadgeRepository.countByUserId(userId);

    return MyPageResponse.builder()
        .nickname(user.getNickname())
        .email(user.getEmail())
        .profileImage(user.getProfileImage())
        .level(user.getLevel())
        .badgeCount(badgeCount)
        .build();
  }

  /**
   * time record 데이터의 duration을 시:분으로 변환
   */
  private String formatMinutesToHHMM(int minutes) {
    int hours = minutes / 60;
    int mins = minutes % 60;
    return String.format("%02d:%02d", hours, mins);
  }

  /**
   * 랭킹 페이지의 타 회원 정보 조회 메서드
   */
  public UserProfileResponse getUserProfile(Integer userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ApiException(ErrorCode.ENTITY_NOT_FOUND));

    // 어제 날짜 기준
    LocalDate yesterday = LocalDate.now().minusDays(1);

    // 어제 독서 시간 (분 단위 합계 → hh:mm:ss 변환)
    int minutes = timeRecordRepository.getTotalReadingMinutesByDate(userId, yesterday);
    String formattedTime = formatMinutesToHHMM(minutes);

    // COMPLETED 상태 플랜 개수 (완독 수)
    int completedCount = planRepository.countByUserIdAndStatus(userId, PlanStatus.COMPLETED);

    return UserProfileResponse.builder()
        .nickname(user.getNickname())
        .profileImage(user.getProfileImage())
        .level(user.getLevel())
        .yesterdayReadingTime(formattedTime)
        .totalCompletedBooks(completedCount)
        .build();
  }

  /**
   * 마이페이지 통계 API - 일/주/월 단위로 통계 조회
   *
   * @param userId 사용자 ID
   * @param scope  통계 범위 (day, week, month)
   * @return 통계 리스트 DTO
   */
  public List<ReadingStatisticsItemDto> getReadingStatistics(Integer userId, String scope) {
    return switch (scope) {
      case "day" -> getDailyStatistics(userId);
      case "week" -> getWeeklyStatistics(userId);
      case "month" -> getMonthlyStatistics(userId);
      default -> throw new IllegalArgumentException("Invalid scope: " + scope);
    };
  }

  /**
   * 일 단위 통계 - 이번 달 1일부터 마지막일까지 날짜별 총 독서 시간
   */
  private List<ReadingStatisticsItemDto> getDailyStatistics(Integer userId) {
    List<ReadingStatisticsItemDto> result = new ArrayList<>();
    LocalDate today = LocalDate.now();
    LocalDate firstDay = today.withDayOfMonth(1);
    LocalDate lastDay = today.withDayOfMonth(today.lengthOfMonth());

    for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
      int totalMinutes = timeRecordRepository.getTotalReadingMinutesByDate(userId, date);
      result.add(new ReadingStatisticsItemDto(
          date.toString(),
          formatToHHMM(totalMinutes),
          formatToHHMM(totalMinutes)
      ));
    }
    return result;
  }

  /**
   * 주 단위 통계 - 최근 4주(일~토)의 총 독서 시간 및 평균 독서 시간 계산
   */
  private List<ReadingStatisticsItemDto> getWeeklyStatistics(Integer userId) {
    List<ReadingStatisticsItemDto> result = new ArrayList<>();
    LocalDate today = LocalDate.now();
    LocalDate endOfWeek = today.with(DayOfWeek.SATURDAY);
    for (int i = 3; i >= 0; i--) {
      LocalDate end = endOfWeek.minusWeeks(i);
      LocalDate start = end.minusDays(6);
      int totalMinutes = 0;
      for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
        totalMinutes += timeRecordRepository.getTotalReadingMinutesByDate(userId, d);
      }
      int averageMinutes = totalMinutes / 7;
      String label = start + " ~ " + end;
      result.add(new ReadingStatisticsItemDto(
          label,
          formatToHHMM(averageMinutes),
          formatToHHMM(totalMinutes)
      ));
    }
    return result;
  }

  /**
   * 월 단위 통계 - 올해 1월부터 12월까지 월별 총 독서 시간 및 평균 계산
   */
  private List<ReadingStatisticsItemDto> getMonthlyStatistics(Integer userId) {
    List<ReadingStatisticsItemDto> result = new ArrayList<>();
    YearMonth thisMonth = YearMonth.now();
    for (int i = 1; i <= 12; i++) {
      YearMonth ym = YearMonth.of(thisMonth.getYear(), i);
      int totalMinutes = 0;
      for (int d = 1; d <= ym.lengthOfMonth(); d++) {
        LocalDate date = ym.atDay(d);
        totalMinutes += timeRecordRepository.getTotalReadingMinutesByDate(userId, date);
      }
      int averageMinutes = totalMinutes / ym.lengthOfMonth();
      result.add(new ReadingStatisticsItemDto(
          ym.toString(),
          formatToHHMM(averageMinutes),
          formatToHHMM(totalMinutes)
      ));
    }
    return result;
  }

  /**
   * 분 단위 시간을 "HH:MM" 형식으로 변환
   */
  private String formatToHHMM(int minutes) {
    int hour = minutes / 60;
    int minute = minutes % 60;
    return String.format("%02d:%02d", hour, minute);
  }
}