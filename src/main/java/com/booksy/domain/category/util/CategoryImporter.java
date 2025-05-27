//package com.booksy.domain.category.util;
//
//import com.booksy.domain.category.dto.CategoryCsvRow;
//import com.booksy.domain.category.entity.Category;
//import com.booksy.domain.category.repository.CategoryRepository;
//import com.booksy.global.error.ErrorCode;
//import com.booksy.global.error.exception.ApiException;
//import jakarta.annotation.PostConstruct;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//
///**
// * aladin_categories.csv 파일을 파싱하여 Category 엔티티로 변환하고 DB에 저장하는 컴포넌트.
// * 계층형 구조를 가진 카테고리 트리를 구성하며, 3회에 걸쳐 parent → child 순으로 처리한다.
// */
//@Component
//@RequiredArgsConstructor
//public class CategoryImporter {
//
//  private final CategoryRepository categoryRepository;
//
//  /**
//   * CSV 파일을 읽어 카테고리를 DB에 저장한다.
//   *
//   * @PostConstruct로 등록되어 있어 애플리케이션 시작 시 자동 실행됨.
//   */
//  @PostConstruct
//  public void importCsvToDatabase() {
//
//    if (categoryRepository.count() > 0) {
//      System.out.println("🚫 Category already initialized. Skipping import.");
//      return;
//    }
//
//    List<CategoryCsvRow> rows = CsvReader.readCategoryCsv();
//
//    Map<Long, Category> categoryMap = new HashMap<>();
//
//    for (CategoryCsvRow row : rows) {
//      String cidStr = row.getCid();
//      if (cidStr == null || cidStr.isBlank()) {
//        continue;
//      }
//
//      Long cid;
//      try {
//        cid = Long.parseLong(cidStr);
//      } catch (NumberFormatException e) {
//        continue;
//      }
//
//      // 가장 하위 depth에 있는 name, depth 계산
//      String[] names = {row.getDepth1(), row.getDepth2(), row.getDepth3(), row.getDepth4(),
//        row.getDepth5()};
//      String name = null;
//      int depth = 0;
//      for (int i = names.length - 1; i >= 0; i--) {
//        if (names[i] != null && !names[i].isBlank()) {
//          name = names[i];
//          depth = i + 1;
//          break;
//        }
//      }
//
//      if (name == null || depth == 0) {
//        continue;
//      }
//
//      Long parentCid = findParentCidFromRow(row, depth, rows);
//      Category parent = parentCid != null ? categoryMap.get(parentCid) : null;
//
//      Category category = Category.builder()
//        .id(cid)
//        .name(name)
//        .depth(depth)
//        .mall(row.getMall())
//        .parent(parent)
//        .build();
//
//      categoryMap.put(cid, category);
//    }
//    try {
//      List<Category> sortedCategories = categoryMap.values().stream()
//        .sorted(Comparator.comparingInt(Category::getDepth))
//        .toList();
//      categoryRepository.saveAll(sortedCategories);
//    } catch (Exception e) {
//      throw new ApiException(ErrorCode.CATEGORY_SAVE_FAILED);
//    }
//  }
//
//  /**
//   * 현재 row의 상위 카테고리와 동일한 name 구조를 가진 row의 cid를 찾는다.
//   *
//   * @param row   현재 row
//   * @param depth 현재 row의 depth
//   * @param rows  전체 row 리스트
//   * @return parent cid or null
//   */
//  private Long findParentCidFromRow(CategoryCsvRow row, int depth, List<CategoryCsvRow> rows) {
//    if (depth <= 1) {
//      return null;
//    }
//
//    return rows.stream()
//      .filter(r -> {
//        int rDepth = getActualDepth(r);
//        if (rDepth != depth - 1) {
//          return false;
//        }
//
//        for (int i = 0; i < depth - 1; i++) {
//          String nameA = getDepthName(r, i);
//          String nameB = getDepthName(row, i);
//          if ((nameA == null || nameA.isBlank()) && (nameB == null || nameB.isBlank())) {
//            continue;
//          }
//          if (nameA == null || nameB == null || !nameA.trim().equals(nameB.trim())) {
//            return false;
//          }
//        }
//        return true;
//      })
//      .map(r -> {
//        try {
//          return Long.parseLong(r.getCid());
//        } catch (Exception e) {
//          return null;
//        }
//      })
//      .filter(Objects::nonNull)
//      .findFirst()
//      .orElse(null);
//  }
//
//  /**
//   * 가장 마지막 depth 값 기준으로 실제 depth 계산
//   */
//  private int getActualDepth(CategoryCsvRow row) {
//    String[] names = {row.getDepth1(), row.getDepth2(), row.getDepth3(), row.getDepth4(),
//      row.getDepth5()};
//    for (int i = names.length - 1; i >= 0; i--) {
//      if (names[i] != null && !names[i].isBlank()) {
//        return i + 1;
//      }
//    }
//    return 0;
//  }
//
//  /**
//   * 인덱스 기반으로 row에서 depth 이름 추출
//   */
//  private String getDepthName(CategoryCsvRow row, int depthIndex) {
//    return switch (depthIndex) {
//      case 0 -> row.getDepth1();
//      case 1 -> row.getDepth2();
//      case 2 -> row.getDepth3();
//      case 3 -> row.getDepth4();
//      case 4 -> row.getDepth5();
//      default -> null;
//    };
//  }
//}
package com.booksy.domain.category.util;

import com.booksy.domain.category.dto.CategoryCsvRow;
import com.booksy.domain.category.entity.Category;
import com.booksy.domain.category.repository.CategoryRepository;
import com.booksy.global.error.ErrorCode;
import com.booksy.global.error.exception.ApiException;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * aladin_categories.csv 파일을 파싱하여 Category 엔티티로 변환하고 DB에 저장하는 컴포넌트.
 * 계층형 구조를 가진 카테고리 트리를 구성하며, depth 순으로 처리하여 parent-child 관계를 올바르게 설정한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryImporter {

  private final CategoryRepository categoryRepository;

  /**
   * CSV 파일을 읽어 카테고리를 DB에 저장한다.
   */
  @PostConstruct
  public void importCsvToDatabase() {
    if (categoryRepository.count() > 0) {
      log.info("🚫 Category already initialized. Skipping import.");
      return;
    }

    try {
      List<CategoryCsvRow> rows = CsvReader.readCategoryCsv();
      log.info("📊 Read {} rows from CSV", rows.size());

      // 1. CSV 행을 CategoryData로 변환
      List<CategoryData> categoryDataList = convertToCategoryData(rows);
      log.info("✅ Converted {} valid categories", categoryDataList.size());

      // 2. depth별로 그룹화하고 정렬
      Map<Integer, List<CategoryData>> categoryByDepth = categoryDataList.stream()
        .collect(Collectors.groupingBy(CategoryData::getDepth));

      // 3. depth 순으로 처리하여 parent-child 관계 설정
      Map<Long, Category> categoryMap = new HashMap<>();

      for (int depth = 1; depth <= 7; depth++) { // depth 범위 확장
        List<CategoryData> categoriesAtDepth = categoryByDepth.getOrDefault(depth,
          Collections.emptyList());
        if (categoriesAtDepth.isEmpty()) {
          continue;
        }

        log.info("Processing depth {}: {} categories", depth, categoriesAtDepth.size());

        for (CategoryData categoryData : categoriesAtDepth) {
          Category parent = null;
          if (depth > 1) {
            Long parentCid = findParentCid(categoryData, categoryDataList);
            parent = parentCid != null ? categoryMap.get(parentCid) : null;

            if (parentCid != null && parent == null) {
              log.warn("⚠️ Parent not found for category {}: parentCid={}",
                categoryData.getCid(), parentCid);
            }
          }

          Category category = Category.builder()
            .id(categoryData.getCid())
            .name(categoryData.getName())
            .depth(categoryData.getDepth())
            .mall(categoryData.getMall())
            .parent(parent)
            .build();

          categoryMap.put(categoryData.getCid(), category);
        }
      }

      // 4. DB 저장
      List<Category> categories = new ArrayList<>(categoryMap.values());
      categories.sort(Comparator.comparingInt(Category::getDepth));
      categoryRepository.saveAll(categories);

      log.info("✅ Successfully imported {} categories", categories.size());
      logCategoryStats(categories);

    } catch (Exception e) {
      log.error("❌ Failed to import categories", e);
      throw new ApiException(ErrorCode.CATEGORY_SAVE_FAILED);
    }
  }

  /**
   * CSV 행을 CategoryData로 변환
   */
  private List<CategoryData> convertToCategoryData(List<CategoryCsvRow> rows) {
    List<CategoryData> result = new ArrayList<>();

    for (CategoryCsvRow row : rows) {
      try {
        CategoryData categoryData = parseCategoryData(row);
        if (categoryData != null) {
          result.add(categoryData);
        }
      } catch (Exception e) {
        log.warn("⚠️ Failed to parse row: {}", row, e);
      }
    }

    return result;
  }

  /**
   * CSV 행을 파싱하여 CategoryData 생성
   */
  private CategoryData parseCategoryData(CategoryCsvRow row) {
    String cidStr = row.getCid();
    if (cidStr == null || cidStr.isBlank()) {
      return null;
    }

    Long cid;
    try {
      cid = Long.parseLong(cidStr.trim());
    } catch (NumberFormatException e) {
      return null;
    }

    // depth별 이름 배열 생성 (depth7까지 확장)
    String[] depthNames = {
      trimToNull(row.getDepth1()),
      trimToNull(row.getDepth2()),
      trimToNull(row.getDepth3()),
      trimToNull(row.getDepth4()),
      trimToNull(row.getDepth5()),
      trimToNull(row.getDepth6()),
      trimToNull(row.getDepth7())
    };

    // 실제 depth와 name 찾기
    String name = null;
    int depth = 0;

    for (int i = depthNames.length - 1; i >= 0; i--) {
      if (depthNames[i] != null) {
        name = depthNames[i];
        depth = i + 1;
        break;
      }
    }

    if (name == null || depth == 0) {
      return null;
    }

    return new CategoryData(cid, name, depth, row.getMall(), depthNames);
  }

  /**
   * 부모 카테고리 ID 찾기 (개선된 로직)
   */
  private Long findParentCid(CategoryData categoryData, List<CategoryData> allCategories) {
    if (categoryData.getDepth() <= 1) {
      return null;
    }

    int targetDepth = categoryData.getDepth() - 1;
    String[] currentPath = categoryData.getDepthNames();

    // 같은 경로의 상위 depth 카테고리 찾기
    return allCategories.stream()
      .filter(cat -> cat.getDepth() == targetDepth)
      .filter(cat -> isParentPath(cat.getDepthNames(), currentPath, targetDepth))
      .map(CategoryData::getCid)
      .findFirst()
      .orElse(null);
  }

  /**
   * 부모 경로인지 확인
   */
  private boolean isParentPath(String[] parentPath, String[] childPath, int parentDepth) {
    for (int i = 0; i < parentDepth; i++) {
      if (!Objects.equals(parentPath[i], childPath[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * 문자열 trim 후 null 체크
   */
  private String trimToNull(String str) {
    if (str == null) {
      return null;
    }
    str = str.trim();
    return str.isEmpty() ? null : str;
  }

  /**
   * 카테고리 통계 로깅
   */
  private void logCategoryStats(List<Category> categories) {
    Map<Integer, Long> depthCount = categories.stream()
      .collect(Collectors.groupingBy(Category::getDepth, Collectors.counting()));

    log.info("📈 Category statistics:");
    for (int depth = 1; depth <= 7; depth++) { // depth 범위 확장
      long count = depthCount.getOrDefault(depth, 0L);
      if (count > 0) {
        log.info("  Depth {}: {} categories", depth, count);
      }
    }

    long withParent = categories.stream()
      .mapToLong(cat -> cat.getParent() != null ? 1 : 0)
      .sum();
    log.info("  Categories with parent: {}/{}", withParent, categories.size());
  }

  /**
   * 카테고리 데이터를 담는 내부 클래스
   */
  private static class CategoryData {

    private final Long cid;
    private final String name;
    private final int depth;
    private final String mall;
    private final String[] depthNames;

    public CategoryData(Long cid, String name, int depth, String mall, String[] depthNames) {
      this.cid = cid;
      this.name = name;
      this.depth = depth;
      this.mall = mall;
      this.depthNames = depthNames.clone(); // 방어적 복사
    }

    public Long getCid() {
      return cid;
    }

    public String getName() {
      return name;
    }

    public int getDepth() {
      return depth;
    }

    public String getMall() {
      return mall;
    }

    public String[] getDepthNames() {
      return depthNames.clone();
    } // 방어적 복사

    @Override
    public String toString() {
      return String.format("CategoryData{cid=%d, name='%s', depth=%d, path=%s}",
        cid, name, depth, Arrays.toString(depthNames));
    }
  }
}