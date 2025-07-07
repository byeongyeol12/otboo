package com.codeit.otboo.global.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
  // COMMON
  INVALID_INPUT_VALUE(400, "C001", "잘못된 입력값입니다."),
  METHOD_NOT_ALLOWED(405, "C002", "지원하지 않는 HTTP 메서드입니다."),
  ENTITY_NOT_FOUND(404, "C003", "엔티티를 찾을 수 없습니다."),
  INTERNAL_SERVER_ERROR(500, "C004", "서버 오류입니다."),
  ACCESS_DENIED(403, "C005", "접근 권한이 없습니다."),

  // AUTH
  INVALID_TOKEN(401, "A001", "유효하지 않은 토큰입니다."),
  EXPIRED_TOKEN(401, "A002", "만료된 토큰입니다."),
  UNAUTHORIZED(401, "A003", "인증 정보가 없습니다."),
  FORBIDDEN(403, "A004", "권한이 없습니다."),

  // USER
  USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다."),
  EMAIL_DUPLICATED(400, "U002", "이미 가입된 이메일입니다."),
  PASSWORD_MISMATCH(400, "U003", "비밀번호가 일치하지 않습니다."),

  // CLOTHES
  CLOTHES_NOT_FOUND(404, "CL001", "의상 정보를 찾을 수 없습니다."),

  // WEATHER
  WEATHER_NOT_FOUND(404, "W001", "날씨 정보를 찾을 수 없습니다."),

  // FEED
  FEED_NOT_FOUND(404, "F001", "피드 정보를 찾을 수 없습니다."),

  // FOLLOW
  FOLLOW_NOT_FOUND(404, "FL001", "팔로우 정보를 찾을 수 없습니다."),
  FOLLOW_NOT_MYSELF(400, "FL002", "자기 자신을 팔로우할 수 없습니다."),
  FOLLOW_ALREADY_USER(400, "FL003", "이미 팔로우한 사람입니다."),
  FOLLOW_CANCEL_ONLY_MINE(400, "FL004", "본인의 팔로우만 취소할 수 있습니다."),
  // DM
  DM_NOT_FOUND(404, "D001", "DM 정보를 찾을 수 없습니다."),

  // NOTIFICATION
  NOTIFICATION_NOT_FOUND(404, "N001", "알림 정보를 찾을 수 없습니다."),
  NOTIFICATION_ALREADY_READ(400,"N002", "알림을 이미 읽은 상태입니다."),
  // RECOMMENDATION
  RECOMMENDATION_NOT_FOUND(404, "R001", "추천 정보를 찾을 수 없습니다."),
  ;

  private final int status;
  private final String code;
  private final String message;

  ErrorCode(int status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}
