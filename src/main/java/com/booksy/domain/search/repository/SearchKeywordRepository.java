package com.booksy.domain.search.repository;

import com.booksy.domain.search.entity.SearchKeyword;
import com.booksy.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {

  // 사용자와 키워드로 기존 검색어 존재 여부를 조회
  Optional<SearchKeyword> findByUserAndKeyword(User user, String keyword);

  // 사용자의 최근 검색어를 최신순으로 조회
  List<SearchKeyword> findAllByUserOrderByUpdatedAtDesc(User user);

  // 사용자와 키워드 기준으로 검색어 삭제
  void deleteByUserAndKeyword(User user, String keyword);

  // 사용자의 모든 검색어 삭제
  void deleteAllByUser(User user);

  // 기존 검색어의 수정 시간을 현재 시간으로 갱신
  @Modifying
  @Query("UPDATE SearchKeyword s SET s.updatedAt = CURRENT_TIMESTAMP WHERE s.user = :user AND s"
    + ".keyword = :keyword")
  void updateTimestamp(@Param("user") User user, @Param("keyword") String keyword);
}
