package com.codeit.otboo.exception;

import com.codeit.otboo.global.error.ErrorCode; // 위치에 따라 패키지 조정

public class CustomException extends RuntimeException {

  private final ErrorCode errorCode;

  // ErrorCode만 받는 생성자
  public CustomException(ErrorCode errorCode) {
    super(errorCode.getMessage()); // message는 ErrorCode에서 가져옴
    this.errorCode = errorCode;
  }

  // ErrorCode + 상세 메시지
  public CustomException(ErrorCode errorCode, String detailMessage) {
    super(detailMessage);
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
