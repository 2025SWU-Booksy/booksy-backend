package com.booksy.domain.search.repository;

import com.booksy.domain.search.entity.SearchKeyword;
import com.booksy.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {

  // 유저와 키워드 기준으로 중복 검색어 있는지 확인
  Optional<SearchKeyword> findByUserAndKeyword(User user, String keyword);
}
