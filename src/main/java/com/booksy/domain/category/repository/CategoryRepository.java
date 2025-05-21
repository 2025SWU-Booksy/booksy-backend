package com.booksy.domain.category.repository;

import com.booksy.domain.category.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  List<Category> findByMall(String mall);
  
  // 최상위 카테고리 (parent가 null)
  List<Category> findByParentIsNull();

  // 특정 카테고리의 자식들
  List<Category> findByParentId(Long parentId);
}
