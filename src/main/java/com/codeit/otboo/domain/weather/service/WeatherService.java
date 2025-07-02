package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.weather.dto.WeatherDto;
import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;

import java.util.List;

public interface WeatherService {

    /**
     * 위도/경도를 기반으로 DB에 저장된 날씨 예보 목록을 조회합니다.
     */
    List<WeatherDto> getWeather(double latitude, double longitude);

    /**
     * 위도/경도를 기반으로 격자 좌표 및 지역명 정보를 조회합니다.
     */
    LocationInfo getWeatherLocation(double latitude, double longitude);
}