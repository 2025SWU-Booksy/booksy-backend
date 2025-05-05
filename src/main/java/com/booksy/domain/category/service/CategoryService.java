package com.booksy.domain.category.service;

import com.booksy.domain.category.dto.CategoryResponseDto;
import com.booksy.domain.category.entity.Category;
import com.booksy.domain.category.repository.CategoryRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;

  /**
   * mall(국내도서/외국도서 등)에 해당하는 전체 카테고리를 트리 구조로 반환한다.
   * - DB에서 flat하게 가져온 카테고리 리스트를 계층형 구조로 변환
   * - parent가 없는 최상위 카테고리부터 시작하여 자식 항목들을 재귀적으로 연결
   *
   * @param mall "국내도서" 또는 "외국도서" 등
   * @return 트리 구조의 카테고리 응답 리스트
   */
  public List<CategoryResponseDto> getCategoryTree(String mall) {
    List<Category> categories = categoryRepository.findByMall(mall);

    Map<Long, CategoryResponseDto> dtoMap = new HashMap<>();
    List<CategoryResponseDto> roots = new ArrayList<>();

    // 1차 변환: Entity → DTO
    for (Category c : categories) {
      dtoMap.put(c.getId(), CategoryResponseDto.fromEntity(c));
    }

    // 2차 연결: 부모 자식 관계 구성
    for (Category c : categories) {
      CategoryResponseDto current = dtoMap.get(c.getId());
      Category parent = c.getParent();

      if (parent != null && dtoMap.containsKey(parent.getId())) {
        dtoMap.get(parent.getId()).getChildren().add(current);
      } else {
        roots.add(current);
      }
    }

    return roots;
  }
}
