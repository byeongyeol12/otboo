package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.domain.user.component.KakaoApiClient;
import com.codeit.otboo.domain.user.dto.request.ProfileLocationUpdateRequest;
import com.codeit.otboo.domain.user.dto.response.ProfileResponseDto;
import com.codeit.otboo.domain.user.entity.Profile;
import com.codeit.otboo.domain.user.repository.ProfileRepository;
import com.codeit.otboo.domain.weather.component.LocationConverter;
import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final LocationConverter locationConverter;
    private final KakaoApiClient kakaoApiClient;

    @Transactional
    public ProfileResponseDto updateLocation(UUID userId, ProfileLocationUpdateRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for user: " + userId));

        double latitude = request.latitude();
        double longitude = request.longitude();

        LocationInfo grid = locationConverter.toGrid(latitude, longitude);

        // 1. KakaoApiClient로부터 행정구역명 '목록(List)'을 받습니다.
        List<String> locationNameList = kakaoApiClient.fetchLocationNames(latitude, longitude);

        // 2. 받아온 목록을 콤마(,)로 구분된 하나의 '문자열(String)'로 변환합니다.
        String locationNamesAsString = String.join(",", locationNameList);

        // 3. 변환된 문자열을 엔티티의 업데이트 메소드에 전달합니다.
        profile.updateLocation(latitude, longitude, grid.x(), grid.y(), locationNamesAsString);

        return ProfileResponseDto.from(profile);
    }
}
