package com.codeit.otboo.domain.location;

import java.util.List;

import lombok.Getter;

@Getter
public class KakaoRegionResponse {
	private List<Document> documents;

	@Getter
	public static class Document {
		private String region_1depth_name;
		private String region_2depth_name;
		private String region_3depth_name;
	}

	public KakaoRegionDto toDto() {
		Document doc = documents.get(0);
		return new KakaoRegionDto(
			doc.getRegion_1depth_name(),
			doc.getRegion_2depth_name(),
			doc.getRegion_3depth_name()
		);
	}
}
