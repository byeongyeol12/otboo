package com.codeit.otboo.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class KakaoAddressResponse {

    @JsonProperty("documents")
    private List<Document> documents;

    @Getter
    @NoArgsConstructor
    public static class Document {
        @JsonProperty("address")
        private Address address;
    }

    @Getter
    @NoArgsConstructor
    public static class Address {
        @JsonProperty("address_name")
        private String addressName; // 예: "경기 양주시 옥정동"

        @JsonProperty("region_1depth_name")
        private String region1; // 예: "경기도"

        @JsonProperty("region_2depth_name")
        private String region2; // 예: "양주시"

        @JsonProperty("region_3depth_name")
        private String region3; // 예: "옥정동"
    }
}