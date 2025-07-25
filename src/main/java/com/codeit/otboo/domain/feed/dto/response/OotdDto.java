package com.codeit.otboo.domain.feed.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeWithDefDto;

import java.util.List;
import java.util.UUID;

@Schema(description = "OOTD(오늘의 의상) DTO")
public record OotdDto(

		@Schema(description = "의류 ID", example = "a0bb2e60-5d98-48ea-a2a8-6c2cb62c67a9")
		UUID clothesId,

		@Schema(description = "의류 이름", example = "화이트 셔츠")
		String name,

		@Schema(description = "의류 이미지 URL", example = "https://cdn.example.com/clothes/123.jpg")
		String imageUrl,

		@Schema(description = "의류 타입(상의/하의/아우터 등)", example = "TOP")
		String type,

		@Schema(description = "의류 속성 리스트")
		List<ClothesAttributeWithDefDto> attributes

) { }
