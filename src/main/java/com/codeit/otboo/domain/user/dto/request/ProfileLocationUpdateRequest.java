package com.codeit.otboo.domain.user.dto.request;

public record ProfileLocationUpdateRequest(
        double latitude,
        double longitude
) {}