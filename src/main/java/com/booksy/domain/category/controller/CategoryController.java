package com.booksy.domain.category.controller;


import com.booksy.domain.category.dto.CategoryResponseDto;
import com.booksy.domain.category.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  /**
   * 특정 mall(국내도서/외국도서)에 대한 카테고리 트리 조회 API
   * GET /api/categories?mall=국내도서
   *
   * @param mall 카테고리 마켓 종류 (예: 국내도서, 외국도서)
   * @return 트리 구조로 정렬된 카테고리 응답 리스트
   */
  @GetMapping("/tree")
  public List<CategoryResponseDto> getCategoryTree(@RequestParam String mall) {
    return categoryService.getCategoryTree(mall);
  }

  @GetMapping
  public ResponseEntity<List<CategoryResponseDto>> getCategories(
    @RequestParam(required = false) Long parentId) {
    List<CategoryResponseDto> categories = categoryService.getCategoriesByParentId(parentId);
    return ResponseEntity.ok(categories);
  }

}
