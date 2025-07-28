package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.weather.component.LocationConverter;
import com.codeit.otboo.domain.weather.dto.WeatherDto;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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

        // 2. '오늘' 날짜의 시작 시각을 계산
        Instant startOfToday = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .atStartOfDay(ZoneId.of("Asia/Seoul"))
                .toInstant();

        // 3. DB에서 해당 위치의 '오늘 이후' 데이터만 조회
        List<Weather> dailyWeathers = weatherRepository.findFutureWeatherByLocation(
                loc.x(), loc.y(), startOfToday
        );

        // 4. 조회된 엔티티 목록을 DTO 목록으로 변환하여 반환
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