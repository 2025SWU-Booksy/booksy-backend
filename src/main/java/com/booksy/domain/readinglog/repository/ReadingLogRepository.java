package com.booksy.domain.readinglog.repository;

import com.booksy.domain.readinglog.dto.ScrapBookResponseDto;
import com.booksy.domain.readinglog.entity.ReadingLog;
import com.booksy.domain.readinglog.type.ContentType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReadingLogRepository extends JpaRepository<ReadingLog, Long> {

  /**
   * 특정 플랜의 리뷰 or 스크랩 전체 조회용
   */
  List<ReadingLog> findAllByPlanIdAndContentType(Long planId, ContentType contentType);

  /**
   * 특정 사용자의 전체 스크랩 목록 조회 (플랜 상관x)
   */
  List<ReadingLog> findAllByUserIdAndContentType(Integer userId, ContentType contentType);


  /**
   * 도서별 스크랩 리스트를 조회
   */
  @Query("""
      SELECT new com.booksy.domain.readinglog.dto.ScrapBookResponseDto(
        p.id,
        b.title,
        b.author,
        b.imageUrl,
        COUNT(l),
        MAX(l.createdAt)
      )
      FROM ReadingLog l
      JOIN l.plan p
      JOIN p.book b
      WHERE l.user.id = :userId
        AND l.contentType = 'SCRAP'
      GROUP BY p.id, b.title, b.author, b.imageUrl
    """)
  List<ScrapBookResponseDto> findScrapGroupedByBook(@Param("userId") Integer userId);

  /**
   * 리딩로그 등록 완료 시 리딩 로그 뱃지 조건 검사
   */
  @Query("SELECT COUNT(r) FROM ReadingLog r WHERE r.user.id = :userId AND r.contentType = :type")
  int countByUserIdAndContentType(@Param("userId") Integer userId,
    @Param("type") ContentType type);

  int countByPlanIdAndContentType(Long planId, ContentType contentType);
}