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

    // WeatherParser는 실제 로직을 테스트해야 하므로, Mock이 아닌 실제 객체를 사용합니다.
    private WeatherParser weatherParser;

    // 테스트 대상 로직이 포함된 WeatherBatchConfig. ItemProcessor를 생성하기 위해 필요합니다.
    private WeatherBatchConfig weatherBatchConfig;

    @BeforeEach
    void setUp() {
        weatherParser = new WeatherParser(new ObjectMapper());
        // 실제 Job 실행과 무관한 의존성은 null로 전달해도 단위 테스트에 영향이 없습니다.
        weatherBatchConfig = new WeatherBatchConfig(kmaApiClient, weatherParser, weatherRepository, null, null, null, null, null);
    }

    @Test
    @DisplayName("ItemProcessor는 API 응답을 받아 여러 날짜의 Weather 엔티티 리스트로 정확히 가공한다")
    void itemProcessor_processesRawData_toWeatherList() throws Exception {
        // given: 이런 상황이 주어졌을 때
        LocationInfo location = new LocationInfo(37.0, 127.0, 60, 127);

        // '어제' 날씨 데이터 (전일 대비 계산용)
        Weather yesterdayWeather = Weather.builder()
                .temperature(new TemperatureInfo(25, 20, 30, 0))
                .humidity(new HumidityInfo(80.0, 0.0))
                .build();

        // KMA API가 반환할 가짜 응답 데이터
        String fakeApiResponse = """
            {"response":{"body":{"items":{"item":[
                {"category":"TMX","fcstDate":"20250723","fcstValue":"32.0"},
                {"category":"TMN","fcstDate":"20250723","fcstValue":"22.0"},
                {"category":"TMP","fcstDate":"20250723","fcstValue":"28.0"},
                {"category":"REH","fcstDate":"20250723","fcstValue":"85.0"},
                {"category":"TMX","fcstDate":"20250724","fcstValue":"33.0"},
                {"category":"TMN","fcstDate":"20250724","fcstValue":"23.0"},
                {"category":"REH","fcstDate":"20250724","fcstValue":"90.0"}
            ]}}}}
            """;

        // Mock 객체들의 동작을 정의
        when(kmaApiClient.fetchWeatherForecast(anyInt(), anyInt())).thenReturn(fakeApiResponse);
        when(weatherRepository.findLatestWeatherByLocation(anyInt(), anyInt())).thenReturn(Optional.of(yesterdayWeather));

        // when: ItemProcessor를 실행하면
        ItemProcessor<LocationInfo, List<Weather>> processor = weatherBatchConfig.weatherItemProcessor();
        List<Weather> result = processor.process(location);

        // then: 결과는 이래야 한다
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2); // 2일치 예보가 생성되었는지 확인

        // 7월 23일 데이터 검증
        Weather day1 = result.get(0);
        assertThat(day1.getTemperature().min()).isEqualTo(22.0);
        assertThat(day1.getTemperature().max()).isEqualTo(32.0);
        assertThat(day1.getTemperature().comparedToDayBefore()).isEqualTo(2.0); // 32 - 30 = 2.0
        assertThat(day1.getHumidity().comparedToDayBefore()).isEqualTo(5.0); // 85 - 80 = 5.0

        // 7월 24일 데이터 검증
        Weather day2 = result.get(1);
        assertThat(day2.getTemperature().min()).isEqualTo(23.0);
        assertThat(day2.getTemperature().max()).isEqualTo(33.0);
        assertThat(day2.getTemperature().comparedToDayBefore()).isEqualTo(1.0); // 33 - 32 = 1.0
        assertThat(day2.getHumidity().comparedToDayBefore()).isEqualTo(5.0); // 90 - 85 = 5.0
    }
}