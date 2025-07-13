package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.weather.component.LocationConverter;
import com.codeit.otboo.domain.weather.dto.WeatherDto;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.vo.HumidityInfo;
import com.codeit.otboo.domain.weather.entity.vo.PrecipitationInfo;
import com.codeit.otboo.domain.weather.entity.vo.TemperatureInfo;
import com.codeit.otboo.domain.weather.entity.vo.WindSpeedInfo;
import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final WeatherRepository weatherRepository;
    private final LocationConverter locationConverter;

    @Override
    @Transactional(readOnly = true)
    public List<WeatherDto> getWeather(double latitude, double longitude) {
        // 1) 위도/경도를 격자 좌표로 변환
        LocationInfo loc = locationConverter.toGrid(latitude, longitude);

        // 2) 조회 기준을 '오늘 00:00 (KST)' 로 설정
        ZoneId zone = ZoneId.of("Asia/Seoul");
        OffsetDateTime todayStart = LocalDate.now(zone)
                .atStartOfDay(zone)
                .toOffsetDateTime();

        // 3) 오늘 00시 이후로 저장된 모든 시간별 데이터를 가져와 DTO로 매핑
        List<WeatherDto> hourly = weatherRepository
                .findWeathersByLocation(loc.x(), loc.y(), todayStart)
                .stream()
                .map(this::mapToWeatherDto)
                .toList();

        // 4) LocalDate 단위로 그룹핑
        Map<LocalDate, List<WeatherDto>> byDate = hourly.stream()
                .collect(Collectors.groupingBy(w ->
                        LocalDateTime.ofInstant(w.forecastAt(), zone)
                                .toLocalDate()
                ));

        // 5) 일자 순 정렬 후, 각 날짜별 요약 생성
        List<LocalDate> dates = byDate.keySet().stream()
                .sorted()
                .toList();

        List<WeatherDto> summary = new ArrayList<>();
        Double prevAvgTemp = null;
        Double prevAvgHum  = null;

        for (LocalDate date : dates) {
            List<WeatherDto> dayList = byDate.get(date);

            // 발표 시각: 가장 최신 forecastedAt
            Instant latestForecastedAt = dayList.stream()
                    .map(WeatherDto::forecastedAt)
                    .max(Instant::compareTo)
                    .orElse(Instant.now());

            // 온도·습도·풍속·강수량·강수확률의 평균/최저/최고값 계산
            double avgTemp = dayList.stream().mapToDouble(d -> d.temperature().current()).average().orElse(0);
            double minTemp = dayList.stream().mapToDouble(d -> d.temperature().current()).min().orElse(0);
            double maxTemp = dayList.stream().mapToDouble(d -> d.temperature().current()).max().orElse(0);

            double avgHum = dayList.stream().mapToDouble(d -> d.humidity().current()).average().orElse(0);
            double avgWind = dayList.stream().mapToDouble(d -> d.windSpeed().speed()).average().orElse(0);

            double avgPrecipAmount = dayList.stream().mapToDouble(d -> d.precipitation().amount()).average().orElse(0);
            double avgPrecipProb   = dayList.stream().mapToDouble(d -> d.precipitation().probability()).average().orElse(0);

            // 전일 대비 변화량
            double tempDiff = prevAvgTemp != null ? avgTemp - prevAvgTemp : 0;
            double humDiff  = prevAvgHum  != null ? avgHum  - prevAvgHum  : 0;

            // 대표값(첫번째)에서 location, skyStatus, asWord, precipitationType 가져오기
            WeatherDto first = dayList.get(0);

            // 6) 일별 요약 DTO 생성
            WeatherDto daySummary = new WeatherDto(
                    UUID.randomUUID(),
                    latestForecastedAt,
                    date.atStartOfDay(zone).toInstant(),
                    first.location(),
                    first.skyStatus(),
                    new PrecipitationInfo(first.precipitation().type(), avgPrecipAmount, avgPrecipProb),
                    new HumidityInfo(avgHum, humDiff),
                    new TemperatureInfo(avgTemp, minTemp, maxTemp, tempDiff),
                    new WindSpeedInfo(avgWind, first.windSpeed().asWord()),
                    first.precipitationType()
            );

            summary.add(daySummary);
            prevAvgTemp = avgTemp;
            prevAvgHum  = avgHum;
        }

        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public LocationInfo getWeatherLocation(double latitude, double longitude) {
        return locationConverter.toGrid(latitude, longitude);
    }

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
