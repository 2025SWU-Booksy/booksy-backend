package com.booksy.domain.category.util;

import com.booksy.domain.category.dto.CategoryCsvRow;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.ClassPathResource;

/**
 * 알라딘 카테고리 CSV 파일을 읽어 {@link CategoryCsvRow} 리스트로 변환하는 유틸리티 클래스.
 */
public class CsvReader {

  /**
   * classpath 상의 aladin_categories.csv 파일을 읽어 CategoryCsvRow 리스트로 반환한다.
   * - BOM(Byte Order Mark) 제거
   * - 공백/빈값을 null로 처리
   *
   * @return CSV에서 읽은 카테고리 행 리스트
   * @exception RuntimeException CSV 읽기 실패 시
   */
  public static List<CategoryCsvRow> readCategoryCsv() {
    try (InputStream is = new ClassPathResource("aladin_categories.csv").getInputStream();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(is, StandardCharsets.UTF_8))) {

      // BOM(Byte Order Mark) 제거
      reader.mark(1);
      int first = reader.read();
      if (first != 0xFEFF) {
        reader.reset();
      }

      CsvToBean<CategoryCsvRow> csvToBean = new CsvToBeanBuilder<CategoryCsvRow>(reader)
          .withType(CategoryCsvRow.class)
          .withIgnoreLeadingWhiteSpace(true)
          .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
          .build();

      return csvToBean.parse();
    } catch (IOException e) {
      throw new RuntimeException("CSV 읽기 실패", e);
    }
  }
}
