package com.codeit.otboo.domain.location.dto.response;

import java.util.List;

public record LocationResponse(
        Double latitude,
        Double longitude,
        int x,
        int y,
        List<String> locationNames
) {}
