package com.codeit.otboo.domain.location.service;

import com.codeit.otboo.domain.location.api.KakaoLocalApiService;
import com.codeit.otboo.domain.location.dto.KakaoRegionDto;
import com.codeit.otboo.domain.location.dto.response.LocationResponse;
import com.codeit.otboo.domain.location.util.GridCoordinateConverter;
import com.codeit.otboo.domain.location.vo.GridCoordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private KakaoLocalApiService kakaoLocalApiService;

    @Mock
    private GridCoordinateConverter gridCoordinateConverter;

    @InjectMocks
    private LocationService locationService;

    @Test
    @DisplayName("좌표를 받으면, 카카오 API와 격자 변환을 거쳐 최종 위치 정보를 반환한다")
    void getLocationInfo_returnsCombinedLocationResponse() {
        // given: Mock 객체들의 동작을 미리 정의
        double lat = 37.5665;
        double lon = 126.9780;

        // Kakao API Mocking
        KakaoRegionDto mockRegionDto = new KakaoRegionDto("경기도", "양주시", "회천4동");
        when(kakaoLocalApiService.getRegionFromCoords(anyDouble(), anyDouble())).thenReturn(mockRegionDto);

        // GridCoordinateConverter Mocking
        GridCoordinate mockGridCoordinate = new GridCoordinate(60, 127);
        when(gridCoordinateConverter.toGrid(anyDouble(), anyDouble())).thenReturn(mockGridCoordinate);

        // when: 서비스 메서드를 호출하면
        LocationResponse result = locationService.getLocationInfo(lat, lon);

        // then: Mocking된 데이터들이 올바르게 조합되었는지 확인
        assertThat(result).isNotNull();
        assertThat(result.latitude()).isEqualTo(lat);
        assertThat(result.longitude()).isEqualTo(lon);
        assertThat(result.x()).isEqualTo(60);
        assertThat(result.y()).isEqualTo(127);
        assertThat(result.locationNames()).containsExactly("경기도", "양주시", "회천4동");
    }
}