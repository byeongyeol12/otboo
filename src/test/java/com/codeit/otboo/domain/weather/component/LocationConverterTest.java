package com.codeit.otboo.domain.weather.component;

import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocationConverterTest {

    private LocationConverter locationConverter;

    @BeforeEach
    void setUp() {
        // LocationConverter는 의존성이 없으므로, 테스트에서 직접 생성합니다.
        locationConverter = new LocationConverter();
    }

    @Test
    @DisplayName("위도와 경도를 기상청 격자 X, Y 좌표로 정확하게 변환한다")
    void toGrid_convertsLatLng_toCorrectGridXY() {
        // given: 서울 시청 근처 위도/경도
        double latitude = 37.5665;
        double longitude = 126.9780;

        // when: 격자 좌표로 변환하면
        LocationInfo result = locationConverter.toGrid(latitude, longitude);

        // then: 예상되는 격자 좌표 값과 일치해야 한다 (기상청 공식 기준)
        assertThat(result).isNotNull();
        assertThat(result.x()).isEqualTo(60);
        assertThat(result.y()).isEqualTo(127);
    }
}