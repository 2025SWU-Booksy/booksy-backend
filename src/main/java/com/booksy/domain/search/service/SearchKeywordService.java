package com.booksy.domain.search.service;

import com.booksy.domain.search.entity.SearchKeyword;
import com.booksy.domain.search.repository.SearchKeywordRepository;
import com.booksy.domain.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchKeywordService {

  private final SearchKeywordRepository searchKeywordRepository;

  @Transactional
  public void saveSearchKeyword(String keyword, User user) {
    searchKeywordRepository.findByUserAndKeyword(user, keyword)
      .ifPresentOrElse(
        existing -> existing.touch(),
        () -> {
          SearchKeyword newKeyword = SearchKeyword.builder()
            .keyword(keyword)
            .user(user)
            .build();
          searchKeywordRepository.save(newKeyword);
        }
      );
  }
}
