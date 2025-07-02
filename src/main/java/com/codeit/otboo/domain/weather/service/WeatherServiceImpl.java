package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.weather.component.KmaApiClient;
import com.codeit.otboo.domain.weather.component.LocationConverter;
import com.codeit.otboo.domain.weather.component.WeatherParser;
import com.codeit.otboo.domain.weather.dto.WeatherDto;
import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    // Repository는 이제 사용하지 않습니다.
    // private final WeatherRepository weatherRepository;
    private final LocationConverter locationConverter;
    private final KmaApiClient kmaApiClient;
    private final WeatherParser weatherParser;

    @Override
    public List<WeatherDto> getWeather(double latitude, double longitude) {
        // 1. 위도/경도를 격자 X, Y로 변환합니다.
        LocationInfo locationInfo = locationConverter.toGrid(latitude, longitude);

        // 2. 기상청 API를 실시간으로 호출하여 원본 JSON 데이터를 받습니다.
        String rawJsonData = kmaApiClient.fetchWeatherForecast(locationInfo.x(), locationInfo.y());

        // 3. JSON 데이터를 파싱하고 가공하여 DTO 리스트로 변환합니다.
        return weatherParser.parseAndGroup(rawJsonData, latitude, longitude);
    }

    @Override
    public LocationInfo getWeatherLocation(double latitude, double longitude) {
        return locationConverter.toGrid(latitude, longitude);
    }
}