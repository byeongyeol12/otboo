package com.codeit.otboo.domain.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카카오 지역 명칭 DTO")
public record KakaoRegionDto(

        @Schema(description = "시/도 이름", example = "서울특별시")
        String region1,

        @Schema(description = "시/군/구 이름", example = "강남구")
        String region2,

        @Schema(description = "읍/면/동 이름", example = "역삼동")
        String region3

) {}
