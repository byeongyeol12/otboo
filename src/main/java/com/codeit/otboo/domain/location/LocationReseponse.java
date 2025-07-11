package com.codeit.otboo.domain.location;

import java.util.List;

public record LocationReseponse(
	Double latitude,
	Double longitude,
	int x,
	int y,
	List<String> locationNames
) {
}
