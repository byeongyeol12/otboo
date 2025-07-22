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
	SAME_AS_OLD_PASSWORD(400, "U004", "새 비밀번호는 이전 비밀번호와 달라야 합니다."),

	// PROFILE
	PROFILE_NOT_FOUND(404, "P001", "프로필 정보를 찾을 수 없습니다."),
	PROFILE_ALREADY_EXISTS(400, "P006", "이미 등록된 프로필이 존재합니다."),
	LOCATION_NOT_SET(400, "P003", "프로필에 위치 정보가 설정되지 않았습니다."),
	PROFILE_IMAGE_UPLOAD_FAILED(500, "P002", "프로필 이미지 업로드에 실패했습니다."),

	// CLOTHES
	CLOTHES_NOT_FOUND(404, "CL001", "의상 정보를 찾을 수 없습니다."),
	CLOTHES_INVALID_TYPE(400, "CL002", "유효하지 않은 의상 종류입니다."),
	CLOTHES_FORBIDDEN(403, "CL003", "해당 의상에 대한 권한이 없습니다."),

	// ATTRIBUTE_DEF
	ATTRIBUTE_DEF_DUPLICATED(400, "AD001", "이미 존재하는 속성입니다."),
	ATTRIBUTE_DEF_NOT_FOUND(404, "AD002", "속성 정의 정보를 찾을 수 없습니다."),
	ATTRIBUTE_DEF_INVALID_SORT_FIELD(400, "AD003", "유효하지 않은 정렬 필드입니다."),
	ATTRIBUTE_DEF_IN_USE(409, "AD004", "사용 중인 의상이 있어 삭제할 수 없는 속성입니다."),

	// WEATHER
	WEATHER_NOT_FOUND(404, "W001", "요청한 ID의 날씨 정보를 찾을 수 없습니다."),
	WEATHER_NOT_FOUND_FOR_LOCATION(404, "W002", "해당 위치의 날씨 정보를 찾을 수 없습니다."),

	// FEED
	FEED_NOT_FOUND(404, "F001", "피드 정보를 찾을 수 없습니다."),
  	FEED_LIKE_ALREADY(409, "F002", "이미 해당 피드에 좋아요를 눌렀습니다."),
  	FEED_LIKE_NOT_FOUND(404, "F003", "해당 피드에 좋아요를 누르지 않았습니다."),

	// FOLLOW
	FOLLOW_NOT_FOUND(404, "FL001", "팔로우 정보를 찾을 수 없습니다."),
	FOLLOW_NOT_MYSELF(400, "FL002", "자기 자신을 팔로우할 수 없습니다."),
	FOLLOW_ALREADY_USER(400, "FL003", "이미 팔로우한 사람입니다."),
	FOLLOW_CANCEL_ONLY_MINE(400, "FL004", "본인의 팔로우만 취소할 수 있습니다."),

	// DM
	DM_NOT_FOUND(404, "D001", "DM 정보를 찾을 수 없습니다."),
	DM_Redis_MESSAGE_ERROR(400, "D002", "Redis 메시지 발행 실패"),

	// NOTIFICATION
	NOTIFICATION_NOT_FOUND(404, "N001", "알림 정보를 찾을 수 없습니다."),
	NOTIFICATION_ALREADY_READ(400, "N002", "알림을 이미 읽은 상태입니다."),
	NOTIFICATION_CREATE_FAILED(400, "N003", "알림 생성 실패했습니다."),

	// SSE
	SSE_HANDLER_FAILED(400, "S001", "SSE 알림 전송 실패했습니다."),

	//WEBSOCKET
	WEBSOCKET_INVALID_TOKEN(400,"W001","웹 소켓 토큰 인증 실패했습니다."),
	// RECOMMENDATION
	RECOMMENDATION_NOT_FOUND(404, "R001", "추천 정보를 찾을 수 없습니다."),

	// LOCATION
	LOCATION_NOT_FOUND(404, "LC001", "위치 정보를 찾을 수 없습니다.") ;

	private final int status;
	private final String code;
	private final String message;

	ErrorCode(int status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}
}
