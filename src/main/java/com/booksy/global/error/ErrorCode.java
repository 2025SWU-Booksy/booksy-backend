package com.booksy.global.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.boot.logging.LogLevel;

/**
 * 에러 코드 enum
 * - 애플리케이션에서 발생할 수 있는 다양한 에러를 코드와 메시지로 정의합니다.
 * - 각 에러 코드는 상태 코드, 코드 값(도메인 별 넘버링), 메시지, 로그 레벨을 포함합니다.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCode {

  // COMMON
  INVALID_INPUT_VALUE(400, "C001", "Invalid input value", LogLevel.ERROR),
  METHOD_NOT_ALLOWED(405, "C002", "Method not allowed", LogLevel.ERROR),
  NO_HANDLER_FOUND(404, "C003", "No handler found", LogLevel.ERROR),
  NO_RESOURCE_FOUND(404, "C004", "Resource not found", LogLevel.ERROR),
  HANDLE_ACCESS_DENIED(403, "C005", "Access denied", LogLevel.ERROR),
  INTERNAL_SERVER_ERROR(500, "C006", "Internal server error", LogLevel.ERROR),
  INVALID_TYPE_VALUE(400, "C007", "Invalid Type Value", LogLevel.ERROR),
  UNAUTHORIZED_ACCESS(403, "C008", "user id mismatch", LogLevel.ERROR),
  POSITIVE_VALUE_REQUIRED(400, "C009", "Value must be positive", LogLevel.ERROR),

  // ENTITY
  ENTITY_NOT_FOUND(404, "E001", "Entity not found", LogLevel.WARN),
  DUPLICATE_RESOURCE(409, "E002", "Resource already exists", LogLevel.WARN),
  ILLEGAL_STATE(400, "E003", "Illegal state", LogLevel.ERROR),

  // VALIDATION
  FIELD_REQUIRED(400, "V001", "Required field is missing", LogLevel.WARN),
  INVALID_FORMAT(400, "V002", "Invalid format", LogLevel.WARN),

  /* 필요한 도메인 에러코드 추가 */

  // BOOK
  BOOK_NOT_FOUND_INTERNAL(404, "B001", "Book not found", LogLevel.WARN),
  BOOK_NOT_FOUND_EXTERNAL(404, "B002", "Book not found from Aladin API", LogLevel.WARN),

  // PLAN
  PLAN_NOT_FOUND(404, "P001", "Plan not found", LogLevel.WARN),


  // SECURE
  UNAUTHENTICATED(401, "SC001", "User not authenticated", LogLevel.WARN);


  private final int status;
  private final String code;
  private final String message;
  private final LogLevel logLevel;

  ErrorCode(final int status, final String code, final String message, LogLevel logLevel) {
    this.status = status;
    this.message = message;
    this.code = code;
    this.logLevel = logLevel;
  }

  public String getMessage() {
    return this.message;
  }

  public String getCode() {
    return code;
  }

  public int getStatus() {
    return status;
  }

  public LogLevel getLogLevel() {
    return logLevel;
  }
}
