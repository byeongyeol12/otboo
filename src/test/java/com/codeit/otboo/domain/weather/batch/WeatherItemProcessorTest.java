package com.codeit.otboo.domain.weather.batch;

import com.codeit.otboo.domain.weather.component.KmaApiClient;
import com.codeit.otboo.domain.weather.component.WeatherParser;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.vo.HumidityInfo;
import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;
import com.codeit.otboo.domain.weather.entity.vo.TemperatureInfo;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherItemProcessorTest {

    @Mock
    private KmaApiClient kmaApiClient;
    @Mock
    private WeatherRepository weatherRepository;

    private WeatherParser weatherParser;
    private WeatherBatchConfig weatherBatchConfig;

    @BeforeEach
    void setUp() {
        weatherParser = new WeatherParser(new ObjectMapper());
        weatherBatchConfig = new WeatherBatchConfig(kmaApiClient, weatherParser, weatherRepository, null, null, null, null, null, null);
    }

    @Test
    @DisplayName("ItemProcessor는 API 응답을 받아 여러 날짜의 Weather 엔티티 리스트로 정확히 가공한다")
    void itemProcessor_processesRawData_toWeatherList() throws Exception {
        // given
        LocationInfo location = new LocationInfo(37.0, 127.0, 60, 127);

        Weather yesterdayWeather = Weather.builder()
                .temperature(new TemperatureInfo(25, 20, 30, 0))
                .humidity(new HumidityInfo(80.0, 0.0))
                .build();

        String fakeApiResponse = """
            {"response":{"body":{"items":{"item":[
                {"category":"TMX","fcstDate":"20250723","fcstValue":"32.0"},
                {"category":"TMN","fcstDate":"20250723","fcstValue":"22.0"},
                {"category":"REH","fcstDate":"20250723","fcstValue":"85.0"},
                {"category":"TMX","fcstDate":"20250724","fcstValue":"33.0"},
                {"category":"TMN","fcstDate":"20250724","fcstValue":"23.0"},
                {"category":"REH","fcstDate":"20250724","fcstValue":"90.0"}
            ]}}}}
            """;

        when(kmaApiClient.fetchWeatherForecast(anyInt(), anyInt())).thenReturn(fakeApiResponse);
        when(weatherRepository.findLatestWeatherByLocation(anyInt(), anyInt())).thenReturn(Optional.of(yesterdayWeather));

        // when
        ItemProcessor<LocationInfo, List<Weather>> processor = weatherBatchConfig.weatherItemProcessor();
        List<Weather> result = processor.process(location);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        Weather day1 = result.get(0);
        assertThat(day1.getTemperature().max()).isEqualTo(32.0);
        assertThat(day1.getTemperature().comparedToDayBefore()).isEqualTo(2.0);
        assertThat(day1.getHumidity().comparedToDayBefore()).isEqualTo(5.0);

        Weather day2 = result.get(1);
        assertThat(day2.getTemperature().max()).isEqualTo(33.0);
        assertThat(day2.getTemperature().comparedToDayBefore()).isEqualTo(1.0);
        assertThat(day2.getHumidity().comparedToDayBefore()).isEqualTo(5.0);
    }
}