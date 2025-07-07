package com.codeit.otboo.domain.user.dto.response;

import com.codeit.otboo.domain.user.entity.Profile;
import com.codeit.otboo.global.enumType.Gender;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public record ProfileResponseDto(
        UUID userId, String name, Gender gender, Instant birthDate,
        LocationDto location, Integer temperatureSensitivity, String profileImageUrl
) {
    public record LocationDto(Double latitude, Double longitude, Integer x, Integer y, List<String> locationNames) {}

    public static ProfileResponseDto from(Profile profile) {
        List<String> locationNames = profile.getLocationNames() != null ?
                Arrays.asList(profile.getLocationNames().split(",")) : List.of();

        LocationDto locationDto = new LocationDto(
                profile.getLatitude(),
                profile.getLongitude(),
                profile.getX(),
                profile.getY(),
                locationNames
        );

        return new ProfileResponseDto(
                profile.getUser().getId(),
                profile.getNickname(), // User의 name 대신 Profile의 nickname 사용
                profile.getGender(),
                profile.getBirthDate(),
                locationDto,
                profile.getTemperatureSensitivity(),
                profile.getProfileImageUrl()
        );
    }
}
