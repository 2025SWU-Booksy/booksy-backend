package com.booksy.domain.category.util;

import com.booksy.domain.category.dto.CategoryCsvRow;
import com.booksy.domain.category.entity.Category;
import com.booksy.domain.category.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * aladin_categories.csv 파일을 파싱하여 Category 엔티티로 변환하고 DB에 저장하는 컴포넌트.
 * 계층형 구조를 가진 카테고리 트리를 구성하며, 3회에 걸쳐 parent → child 순으로 처리한다.
 */
@Component
@RequiredArgsConstructor
public class CategoryImporter {

  private final CategoryRepository categoryRepository;

  /**
   * CSV 파일을 읽어 카테고리를 DB에 저장한다.
   *
   * @PostConstruct로 등록되어 있어 애플리케이션 시작 시 자동 실행됨.
   */
  @PostConstruct
  public void importCsvToDatabase() {
    System.out.println("🚀 CategoryImporter 실행 시작");

    List<CategoryCsvRow> rows = CsvReader.readCategoryCsv();

    Set<Long> existingIds = categoryRepository.findAll().stream()
        .map(Category::getId)
        .collect(Collectors.toSet());

    Map<Long, Category> categoryMap = new HashMap<>();
    int skippedNullCid = 0;
    int skippedParseError = 0;
    int skippedNoDepthOrName = 0;
    int skippedNoParent = 0;

    // 최대 3회 반복 (parent → child 순서 보장)
    for (int round = 0; round < 3; round++) {
      int addedThisRound = 0;

      for (CategoryCsvRow row : rows) {
        String cidStr = row.getCid();
        if (cidStr == null || cidStr.isBlank()) {
          skippedNullCid++;
          continue;
        }

        Long cid;
        try {
          cid = Long.parseLong(cidStr);
        } catch (NumberFormatException e) {
          skippedParseError++;
          continue;
        }

        // 이미 DB에 존재하거나 이번에 추가된 경우 스킵
        if (existingIds.contains(cid) || categoryMap.containsKey(cid)) {
          continue;
        }

        // 가장 하위 depth에 있는 name, depth 계산
        String[] names = {row.getDepth1(), row.getDepth2(), row.getDepth3(), row.getDepth4(),
            row.getDepth5()};
        String name = null;
        int depth = 0;
        for (int i = names.length - 1; i >= 0; i--) {
          if (names[i] != null && !names[i].isBlank()) {
            name = names[i];
            depth = i + 1;
            break;
          }
        }

        if (name == null || depth == 0) {
          skippedNoDepthOrName++;
          continue;
        }

        // 상위 카테고리 CID 탐색
        Long parentCid = findParentCidFromRow(row, depth, rows);
        if (parentCid != null && !categoryMap.containsKey(parentCid)) {
          skippedNoParent++;
          continue;
        }

        Category parent = parentCid != null ? categoryMap.get(parentCid) : null;

        Category category = Category.builder()
            .id(cid)
            .name(name)
            .depth(depth)
            .mall(row.getMall())
            .parent(parent)
            .build();

        categoryMap.put(cid, category);
        addedThisRound++;
      }

      System.out.println("🔁 패스 " + (round + 1) + "회차 - 새로 추가된 category 수: " + addedThisRound);
      if (addedThisRound == 0) {
        break;
      }
    }

    System.out.println("💾 저장할 category 수: " + categoryMap.size());
    System.out.println("🚫 skipped: cid null = " + skippedNullCid);
    System.out.println("🚫 skipped: parse error = " + skippedParseError);
    System.out.println("🚫 skipped: no name/depth = " + skippedNoDepthOrName);
    System.out.println("🚫 skipped: parent not found = " + skippedNoParent);

    try {
      // depth 기준 정렬 후 저장
      List<Category> sortedCategories = categoryMap.values().stream()
          .sorted(Comparator.comparingInt(Category::getDepth))
          .toList();
      categoryRepository.saveAll(sortedCategories);
      System.out.println("✅ 저장 완료");
    } catch (Exception e) {
      System.out.println("❌ 저장 중 에러 발생: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * 현재 row의 상위 카테고리와 동일한 name 구조를 가진 row의 cid를 찾는다.
   *
   * @param row   현재 row
   * @param depth 현재 row의 depth
   * @param rows  전체 row 리스트
   * @return parent cid or null
   */
  private Long findParentCidFromRow(CategoryCsvRow row, int depth, List<CategoryCsvRow> rows) {
    if (depth <= 1) {
      return null;
    }

    return rows.stream()
        .filter(r -> {
          int rDepth = getActualDepth(r);
          if (rDepth != depth - 1) {
            return false;
          }

          for (int i = 0; i < depth - 1; i++) {
            String nameA = getDepthName(r, i);
            String nameB = getDepthName(row, i);
            if ((nameA == null || nameA.isBlank()) && (nameB == null || nameB.isBlank())) {
              continue;
            }
            if (nameA == null || nameB == null || !nameA.trim().equals(nameB.trim())) {
              return false;
            }
          }
          return true;
        })
        .map(r -> {
          try {
            return Long.parseLong(r.getCid());
          } catch (Exception e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  /**
   * 가장 마지막 depth 값 기준으로 실제 depth 계산
   */
  private int getActualDepth(CategoryCsvRow row) {
    String[] names = {row.getDepth1(), row.getDepth2(), row.getDepth3(), row.getDepth4(),
        row.getDepth5()};
    for (int i = names.length - 1; i >= 0; i--) {
      if (names[i] != null && !names[i].isBlank()) {
        return i + 1;
      }
    }
    return 0;
  }

  /**
   * 인덱스 기반으로 row에서 depth 이름 추출
   */
  private String getDepthName(CategoryCsvRow row, int depthIndex) {
    return switch (depthIndex) {
      case 0 -> row.getDepth1();
      case 1 -> row.getDepth2();
      case 2 -> row.getDepth3();
      case 3 -> row.getDepth4();
      case 4 -> row.getDepth5();
      default -> null;
    };
  }
}
