package com.codeit.otboo.domain.weather.component;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class KmaApiClientTest {

    private MockWebServer mockWebServer;
    private KmaApiClient kmaApiClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/").toString();
        kmaApiClient = new KmaApiClient(baseUrl, "TEST_SERVICE_KEY");
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("fetchWeatherForecast는 주어진 시간을 기준으로 올바른 base_date와 base_time을 계산하여 API를 호출한다")
    void fetchWeatherForecast_usesCorrectBaseDateTime() throws InterruptedException {
        // given: 새벽 1시 30분이라는 특정 시간
        LocalDate testDate = LocalDate.of(2025, 7, 28);
        LocalTime testTime = LocalTime.of(1, 30);

        // 예상되는 base_date는 전날인 27일, base_time은 2300
        String expectedBaseDate = "20250727";
        String expectedBaseTime = "2300";

        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"response\":{\"body\":{\"items\":{}}}}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // when: ✨ 시간을 직접 주입할 수 있는 새로운 메서드를 호출
        kmaApiClient.fetchWeatherForecast(60, 127, testDate, testTime);

        // then
        RecordedRequest request = mockWebServer.takeRequest();
        String path = request.getPath();

        assertThat(path).contains("base_date=" + expectedBaseDate);
        assertThat(path).contains("base_time=" + expectedBaseTime);
    }

    @Test
    @DisplayName("API 호출이 성공하면, 응답 본문을 문자열로 반환한다")
    void fetchWeatherForecast_whenApiCallSucceeds_returnsResponseBody() {
        // given
        String expectedResponse = "{\"status\":\"OK\"}";
        mockWebServer.enqueue(new MockResponse()
                .setBody(expectedResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // when
        String actualResponse = kmaApiClient.fetchWeatherForecast(60, 127);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }
}