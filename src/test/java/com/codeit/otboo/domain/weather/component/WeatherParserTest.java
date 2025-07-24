package com.codeit.otboo.domain.weather.component;

import com.codeit.otboo.domain.weather.component.WeatherParser;
import com.codeit.otboo.domain.weather.dto.KmaWeatherResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherParserTest {

    private WeatherParser weatherParser;

    @BeforeEach
    void setUp() {
        // WeatherParser는 외부 의존성이 ObjectMapper 뿐이므로, 테스트에서 직접 생성해서 사용합니다.
        weatherParser = new WeatherParser(new ObjectMapper());
    }

    @Test
    @DisplayName("기상청 API의 JSON 문자열을 KmaWeatherResponse.Item 리스트로 정확히 파싱한다")
    void parse_withValidJson_returnsItemList() {
        // given: 기상청 API 응답과 유사한 JSON 데이터
        String fakeApiResponse = """
            {
                "response": {
                    "header": {"resultCode": "00", "resultMsg": "NORMAL_SERVICE"},
                    "body": {
                        "dataType": "JSON",
                        "items": {
                            "item": [
                                {"category": "TMP", "fcstValue": "25"},
                                {"category": "PCP", "fcstValue": "강수없음"},
                                {"category": "PCP", "fcstValue": "1.0mm"}
                            ]
                        }
                    }
                }
            }
            """;

        // when: 파싱 메서드를 실행하면
        List<KmaWeatherResponse.Item> result = weatherParser.parse(fakeApiResponse);

        // then: 결과는 이래야 한다
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3); // 3개의 아이템이 정상적으로 파싱되었는지 확인
        assertThat(result.get(0).getCategory()).isEqualTo("TMP");
        assertThat(result.get(0).getFcstValue()).isEqualTo("25");
        assertThat(result.get(1).getFcstValue()).isEqualTo("강수없음");
    }

    @Test
    @DisplayName("API 응답 형식이 잘못되었을 경우, 빈 리스트를 반환한다")
    void parse_withInvalidJson_returnsEmptyList() {
        // given: 비정상적인 JSON 데이터
        String invalidJson = "this is not a json string";

        // when: 파싱 메서드를 실행하면
        List<KmaWeatherResponse.Item> result = weatherParser.parse(invalidJson);

        // then: 에러 없이 빈 리스트를 반환해야 한다
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}