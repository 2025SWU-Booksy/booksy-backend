package com.booksy.domain.category.dto;

import com.booksy.domain.category.entity.Category;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponseDto {

  private Long id;
  private String name;
  private List<CategoryResponseDto> children;

  public static CategoryResponseDto fromEntity(Category category) {
    return CategoryResponseDto.builder()
        .id(category.getId())
        .name(category.getName())
        .children(new ArrayList<>())
        .build();
  }
}