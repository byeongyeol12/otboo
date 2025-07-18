// WeatherServiceImpl.java 파일

package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.weather.component.LocationConverter;
import com.codeit.otboo.domain.weather.dto.WeatherDto;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final WeatherRepository weatherRepository;
    private final LocationConverter locationConverter;

    @Override
    @Transactional(readOnly = true)
    public List<WeatherDto> getWeather(double latitude, double longitude) {
        // 1. 위도/경도를 격자 좌표로 변환
        LocationInfo loc = locationConverter.toGrid(latitude, longitude);

        // 2. DB에서 해당 위치의 모든 일별 데이터를 조회
        List<Weather> dailyWeathers = weatherRepository.findByLocationXAndLocationYOrderByForecastAtAsc(loc.x(), loc.y());

        // 3. 조회된 엔티티 목록을 DTO 목록으로 변환하여 반환
        return dailyWeathers.stream()
                .map(this::mapToWeatherDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LocationInfo getWeatherLocation(double latitude, double longitude) {
        return locationConverter.toGrid(latitude, longitude);
    }

    // 엔티티를 DTO로 변환하는 헬퍼 메서드
    private WeatherDto mapToWeatherDto(Weather w) {
        return new WeatherDto(
                w.getId(),
                w.getForecastedAt(),
                w.getForecastAt(),
                w.getLocation(),
                w.getSkyStatus(),
                w.getPrecipitation(),
                w.getHumidity(),
                w.getTemperature(),
                w.getWindSpeed(),
                w.getPrecipitationType()
        );
    }
}