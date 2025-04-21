package com.booksy.global.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰 생성 및 검증 담당 유틸 클래스
 */
@Component
public class JwtTokenProvider {

  @Value("${jwt.secret}")
  private String secretKey;

  @Value("${jwt.token-validity-in-seconds}")
  private long tokenValidityInSeconds;

  private Key key;

  @PostConstruct
  public void init() {
    this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
  }

  public String generateToken(String email) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + tokenValidityInSeconds * 1000);

    return Jwts.builder()
        .setSubject(email)
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * JWT 토큰 생성
   *
   * @param userId 사용자 ID
   * @return 생성된 JWT 토큰
   */
  public String generateToken(Integer userId) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + tokenValidityInSeconds * 1000);

    return Jwts.builder()
        .setSubject(userId.toString())  // 사용자 ID를 문자열로 변환하여 저장
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * 토큰에서 사용자 ID 추출
   */
  public Integer getUserId(String token) {
    String subject = Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();

    return Integer.parseInt(subject);
  }

  /**
   * 토큰 유효성 검증
   *
   * @param token 검증할 JWT 토큰
   * @return 토큰이 유효하면 true, 그렇지 않으면 false
   */
  public boolean validateToken(String token) {
    try {
      // 토큰 파싱 시도
      Jwts.parserBuilder()
          .setSigningKey(key)  // 서명 검증에 사용할 키 설정
          .build()
          .parseClaimsJws(token);  // 토큰 파싱

      // 파싱 성공 시 유효한 토큰으로 간주
      return true;
    } catch (Exception e) {
      // 파싱 중 예외 발생 시 유효하지 않은 토큰으로 간주
      return false;
    }
  }

}

