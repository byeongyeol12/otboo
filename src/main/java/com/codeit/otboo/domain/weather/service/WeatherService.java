package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.weather.dto.WeatherDto;
import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;

import java.util.List;

public interface WeatherService {

    /**
     * latitude/longitude 에 해당하는 위치의
     * “일별 요약” WeatherDto 리스트를 반환합니다.
     */
    List<WeatherDto> getWeather(double latitude, double longitude);

    /**
     * 내부에서 Grid 변환에만 쓰이므로 그대로 두세요.
     */
    LocationInfo getWeatherLocation(double latitude, double longitude);
}