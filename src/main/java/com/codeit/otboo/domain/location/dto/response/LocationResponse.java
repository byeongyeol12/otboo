package com.codeit.otboo.domain.location.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "위치 정보 응답 DTO")
public record LocationResponse(

        @Schema(description = "위도", example = "37.4979")
        Double latitude,

        @Schema(description = "경도", example = "127.0276")
        Double longitude,

        @Schema(description = "기상청 X 좌표", example = "60")
        int x,

        @Schema(description = "기상청 Y 좌표", example = "127")
        int y,

        @Schema(description = "위치 명칭 리스트 (시/도, 시/군/구, 읍/면/동 등)", example = "[\"서울특별시\", \"강남구\", \"역삼동\"]")
        List<String> locationNames

) {}
