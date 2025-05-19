package com.booksy.domain.search.service;

import com.booksy.domain.search.entity.SearchKeyword;
import com.booksy.domain.search.repository.SearchKeywordRepository;
import com.booksy.domain.user.entity.User;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchKeywordService {

  private final SearchKeywordRepository searchKeywordRepository;

  /**
   * 검색 키워드를 저장합니다.
   * 동일한 키워드가 이미 존재하면 해당 키워드의 업데이트 시간을 갱신합니다.
   *
   * @param keyword 저장할 검색 키워드
   * @param user    키워드를 저장할 사용자
   */
  @Transactional
  public void saveSearchKeyword(String keyword, User user) {
    Optional<SearchKeyword> existing = searchKeywordRepository.findByUserAndKeyword(user, keyword);

    if (existing.isPresent()) {
      searchKeywordRepository.updateTimestamp(user, keyword);
    } else {
      SearchKeyword newKeyword = SearchKeyword.builder()
        .keyword(keyword)
        .user(user)
        .build();
      searchKeywordRepository.save(newKeyword);
    }
  }

  /**
   * 사용자의 최근 검색 키워드 목록을 조회합니다.
   * 최대 10개의 키워드만 최신순으로 반환합니다.
   *
   * @param user 키워드를 조회할 사용자
   * @return 최근 검색 키워드 목록
   */
  @Transactional
  public List<String> getRecentKeywords(User user) {
    return searchKeywordRepository.findAllByUserOrderByUpdatedAtDesc(user).stream()
      .limit(10) // 최대 10개
      .map(SearchKeyword::getKeyword)
      .toList();
  }

  /**
   * 사용자의 특정 검색 키워드를 삭제합니다.
   *
   * @param user    키워드를 삭제할 사용자
   * @param keyword 삭제할 검색 키워드
   */
  @Transactional
  public void deleteKeyword(User user, String keyword) {
    searchKeywordRepository.deleteByUserAndKeyword(user, keyword);
  }

  /**
   * 사용자의 모든 검색 키워드를 삭제합니다.
   *
   * @param user 키워드를 삭제할 사용자
   */
  @Transactional
  public void deleteAllKeywords(User user) {
    searchKeywordRepository.deleteAllByUser(user);
  }

}
