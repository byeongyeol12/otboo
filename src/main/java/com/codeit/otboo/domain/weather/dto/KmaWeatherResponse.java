package com.codeit.otboo.domain.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

// 기상청 API의 전체 응답 구조를 나타내는 클래스
@Getter
@NoArgsConstructor
public class KmaWeatherResponse {
    @JsonProperty("response")
    private Response response;

    @Getter
    @NoArgsConstructor
    public static class Response {
        @JsonProperty("header")
        private Header header;
        @JsonProperty("body")
        private Body body;
    }

    @Getter
    @NoArgsConstructor
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Getter
    @NoArgsConstructor
    public static class Body {
        private String dataType;
        private Items items;
        private int pageNo;
        private int numOfRows;
        private int totalCount;
    }

    @Getter
    @NoArgsConstructor
    public static class Items {
        @JsonProperty("item")
        private List<Item> itemList;
    }

    @Getter
    @NoArgsConstructor
    public static class Item {
        private String baseDate; // 20250630
        private String baseTime; // 1400
        private String category; // T1H, SKY, PTY 등
        private String fcstDate; // 20250630
        private String fcstTime; // 1500
        private String fcstValue; // "27", "1", "0" 등
        private int nx;
        private int ny;
    }
}