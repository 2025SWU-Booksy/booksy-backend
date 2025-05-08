package com.booksy.domain.readinglog.dto;

import java.util.List;
import lombok.Getter;

/**
 * 독서로그 선택 삭제 요청 dto
 */
@Getter
public class LogDeleteRequestDto {

  private List<Long> logIds;
}