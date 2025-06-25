package com.codeit.otboo.global.error;

import com.codeit.otboo.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
    ErrorCode code = ex.getErrorCode();
    log.warn("CustomException: {}", code.getMessage());
    return ResponseEntity
        .status(code.getStatus())
        .body(new ErrorResponse(code));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    log.error("Unhandled Exception", ex);
    ErrorCode code = ErrorCode.INTERNAL_SERVER_ERROR;
    return ResponseEntity
        .status(code.getStatus())
        .body(new ErrorResponse(code));
  }
}
