package com.codeit.otboo.domain.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

// 기상청 API의 전체 응답 구조를 나타내는 클래스
@Getter
@NoArgsConstructor
@Schema(description = "기상청 날씨 API 전체 응답 DTO")
public class KmaWeatherResponse {
    @Schema(description = "최상위 응답 객체")
    @JsonProperty("response")
    private Response response;

    @Getter
    @NoArgsConstructor
    @Schema(description = "응답 객체")
    public static class Response {
        @Schema(description = "헤더 정보")
        @JsonProperty("header")
        private Header header;

        @Schema(description = "바디 정보")
        @JsonProperty("body")
        private Body body;
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "헤더 정보 DTO")
    public static class Header {
        @Schema(description = "결과 코드", example = "00")
        private String resultCode;

        @Schema(description = "결과 메시지", example = "NORMAL_SERVICE")
        private String resultMsg;
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "바디 정보 DTO")
    public static class Body {
        @Schema(description = "데이터 타입", example = "JSON")
        private String dataType;

        @Schema(description = "데이터 아이템들")
        private Items items;

        @Schema(description = "페이지 번호", example = "1")
        private int pageNo;

        @Schema(description = "한 페이지당 데이터 개수", example = "1000")
        private int numOfRows;

        @Schema(description = "전체 데이터 개수", example = "250")
        private int totalCount;
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "날씨 데이터 아이템 리스트 DTO")
    public static class Items {
        @Schema(description = "날씨 예보 항목 리스트")
        @JsonProperty("item")
        private List<Item> itemList;
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "단일 예보 아이템 DTO")
    public static class Item {
        @Schema(description = "기준 날짜(yyyyMMdd)", example = "20250630")
        private String baseDate;

        @Schema(description = "기준 시각(HHmm)", example = "1400")
        private String baseTime;

        @Schema(description = "카테고리(예: T1H, SKY, PTY 등)", example = "T1H")
        private String category;

        @Schema(description = "예보 날짜(yyyyMMdd)", example = "20250630")
        private String fcstDate;

        @Schema(description = "예보 시각(HHmm)", example = "1500")
        private String fcstTime;

        @Schema(description = "예보 값 (예: '27', '1', '0' 등)", example = "27")
        private String fcstValue;

        @Schema(description = "격자 X 좌표", example = "60")
        private int nx;

        @Schema(description = "격자 Y 좌표", example = "127")
        private int ny;
    }
}
