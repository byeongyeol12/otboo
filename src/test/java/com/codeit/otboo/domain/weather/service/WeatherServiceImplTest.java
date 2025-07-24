package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.weather.component.LocationConverter;
import com.codeit.otboo.domain.weather.dto.WeatherDto;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherServiceImplTest {

    @Mock
    private WeatherRepository weatherRepository;
    @Mock
    private LocationConverter locationConverter;

    @InjectMocks
    private WeatherServiceImpl weatherService;

    @Test
    @DisplayName("getWeather는 특정 위치의 날씨 목록을 DTO로 변환하여 반환한다")
    void getWeather_returnsWeatherDtoList() {
        // given
        double lat = 37.0, lon = 127.0;
        LocationInfo location = new LocationInfo(lat, lon, 60, 127);
        Weather mockWeather = Weather.builder().build(); // 테스트용 가짜 날씨

        when(locationConverter.toGrid(lat, lon)).thenReturn(location);
        when(weatherRepository.findByLocationXAndLocationYOrderByForecastAtAsc(60, 127))
                .thenReturn(List.of(mockWeather));

        // when
        List<WeatherDto> result = weatherService.getWeather(lat, lon);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }
}