package com.codeit.otboo.domain.feed.dto.response;

import java.util.List;
import java.util.UUID;

import com.codeit.otboo.domain.clothes.dto.response.ClothesAttributeWithDefDto;

public record OotdDto(
	UUID clothesId,
	String name,
	String imageUrl,
	String type,
	List<ClothesAttributeWithDefDto> attributes
) { }
