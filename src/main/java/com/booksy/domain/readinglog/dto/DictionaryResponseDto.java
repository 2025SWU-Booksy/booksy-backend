package com.booksy.domain.readinglog.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DictionaryResponseDto {

  private String word;
  private List<String> definitions;
}