package com.codeit.otboo.domain.location.dto.response;

import com.codeit.otboo.domain.location.dto.KakaoRegionDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import java.util.List;

@Getter
@Schema(description = "카카오 주소 변환 API 응답 DTO")
public class KakaoRegionResponse {

    @Schema(description = "지역 정보 문서 리스트")
    private List<Document> documents;

    @Getter
    @Schema(description = "카카오 지역 정보 문서 DTO")
    public static class Document {

        @Schema(description = "시/도 이름", example = "서울특별시")
        private String region_1depth_name;

        @Schema(description = "시/군/구 이름", example = "강남구")
        private String region_2depth_name;

        @Schema(description = "읍/면/동 이름", example = "역삼동")
        private String region_3depth_name;
    }

    public KakaoRegionDto toDto() {
        if (documents == null || documents.isEmpty()) {
            return new KakaoRegionDto(null, null, null);
        }
        Document doc = documents.get(0);
        return new KakaoRegionDto(
                doc.getRegion_1depth_name(),
                doc.getRegion_2depth_name(),
                doc.getRegion_3depth_name()
        );
    }
}
