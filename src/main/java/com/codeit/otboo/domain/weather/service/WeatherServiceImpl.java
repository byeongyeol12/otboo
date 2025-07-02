package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.weather.component.LocationConverter;
import com.codeit.otboo.domain.weather.dto.WeatherDto;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final WeatherRepository weatherRepository;
    private final LocationConverter locationConverter;
    // 이제 실시간 API 호출이 필요 없으므로 KmaApiClient와 WeatherParser는 제거합니다.
    // private final KmaApiClient kmaApiClient;
    // private final WeatherParser weatherParser;

    @Override
    @Transactional(readOnly = true) // 데이터 조회만 하므로 readOnly=true로 성능 최적화
    public List<WeatherDto> getWeather(double latitude, double longitude) {
        // 1. 위도/경도를 격자 X, Y로 변환합니다.
        LocationInfo locationInfo = locationConverter.toGrid(latitude, longitude);

        // 2. Repository를 통해 DB에 저장된, 현재 시각 이후의 날씨 정보만 조회합니다.
        List<Weather> weathers = weatherRepository.findWeathersByLocation(
                locationInfo.x(),
                locationInfo.y(),
                OffsetDateTime.now() // 현재 시각
        );

        // 3. 조회된 Entity 목록을 DTO 목록으로 변환하여 반환합니다.
        return weathers.stream()
                .map(this::mapToWeatherDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LocationInfo getWeatherLocation(double latitude, double longitude) {
        // 이 기능은 그대로 유지합니다.
        return locationConverter.toGrid(latitude, longitude);
    }

    // Entity를 DTO로 변환하는 private 헬퍼 메소드
    private WeatherDto mapToWeatherDto(Weather weather) {
        return new WeatherDto(
                weather.getId(),
                weather.getForecastedAt(),
                weather.getForecastAt(),
                weather.getLocation(),
                weather.getSkyStatus(),
                weather.getPrecipitation(),
                weather.getHumidity(),
                weather.getTemperature(),
                weather.getWindSpeed()
        );
    }
}
