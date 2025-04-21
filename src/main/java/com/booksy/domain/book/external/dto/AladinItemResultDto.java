package com.booksy.domain.book.external.dto;

import java.util.List;
import lombok.Data;

@Data
public class AladinBookListResponseDto {

  private List<AladinItemDto> item;

}
