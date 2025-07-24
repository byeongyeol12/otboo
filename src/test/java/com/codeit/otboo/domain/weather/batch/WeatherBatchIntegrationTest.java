package com.codeit.otboo.domain.weather.batch;

import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.user.entity.Profile;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.ProfileRepository;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.weather.component.KmaApiClient;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.global.config.QueryDslConfig;
import com.codeit.otboo.global.enumType.Gender;
import com.codeit.otboo.global.enumType.Role;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;
import com.codeit.otboo.domain.weather.entity.vo.PrecipitationType;
import com.codeit.otboo.domain.weather.entity.vo.TemperatureInfo;
import com.codeit.otboo.domain.weather.entity.vo.HumidityInfo;
import com.codeit.otboo.domain.weather.entity.vo.PrecipitationInfo;
import com.codeit.otboo.domain.weather.entity.vo.SkyStatus;
import com.codeit.otboo.domain.weather.entity.vo.WindSpeedInfo;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
class WeatherBatchIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    /**
     * 테스트 실행에 필요한 모든 설정값들을 동적으로 직접 주입합니다.
     */
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.batch.jdbc.initialize-schema", () -> "always");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("jwt.secret", () -> "this-is-a-super-secret-key-for-test-purpose-only-12345");
        registry.add("jwt.expiration", () -> "600000");
        registry.add("jwt.refresh-token-validity-in-ms", () -> "604800000");
        registry.add("api.kma.service-key", () -> "TEST_KMA_KEY");
        registry.add("API_KAKAO_REST_KEY", () -> "TEST_KAKAO_KEY");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("fetchWeatherJob")
    private Job fetchWeatherJob;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private WeatherRepository weatherRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @MockBean
    private KmaApiClient kmaApiClient;

    @BeforeEach
    void setUp() {
        weatherRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("날씨 배치 Job을 실행하면, API 데이터를 가공하여 DB에 여러 날짜의 날씨를 저장한다")
    void fetchWeatherJob_runsSuccessfully_andSavesWeatherData() throws Exception {
        // given: DB에 위치 정보가 있는 사용자가 있고,
        User user = new User();
        user.setEmail("test@test.com");
        user.setName("tester");
        user.setRole(Role.USER);
        user.setPasswordHash("dummy-password-hash");
        userRepository.save(user);

        Profile profile = new Profile(user, "테스터", Gender.MALE);
        profile.updateLocation(37.0, 127.0, 60, 127, "테스트 위치");
        profileRepository.save(profile);

        // KMA API가 2일치 예보가 담긴 가짜 응답을 반환하도록 설정
        String fakeApiResponse = """
                {"response":{"body":{"items":{"item":[
                    {"category":"TMX","fcstDate":"20250723","fcstValue":"30"},
                    {"category":"TMN","fcstDate":"20250723","fcstValue":"20"},
                    {"category":"TMX","fcstDate":"20250724","fcstValue":"31"},
                    {"category":"TMN","fcstDate":"20250724","fcstValue":"21"}
                ]}}}}
                """;
        when(kmaApiClient.fetchWeatherForecast(anyInt(), anyInt())).thenReturn(fakeApiResponse);

        // when: 날씨 배치 Job을 실행하면
        jobLauncher.run(fetchWeatherJob, new JobParametersBuilder()
                .addString("jobId", UUID.randomUUID().toString()) // 매번 다른 Job 인스턴스 실행
                .toJobParameters());

        // then: weathers 테이블에 2일치 데이터가 저장되어야 한다
        assertThat(weatherRepository.count()).isEqualTo(2);
    }
    @Test
    @DisplayName("내일 비 예보가 있을 때, 배치 Job이 실행되면 알림이 생성되어야 한다")
    void fetchWeatherJob_whenRainIsForecasted_shouldCreateNotification() throws Exception {
        // given: DB에 위치 정보가 있는 사용자와 '오늘 맑음' 날씨가 있고,
        User user = new User();
        user.setEmail("test@test.com");
        user.setName("tester");
        user.setRole(Role.USER);
        user.setPasswordHash("dummy-password-hash");
        userRepository.save(user);

        Profile profile = new Profile(user, "테스터", Gender.MALE);
        profile.updateLocation(37.0, 127.0, 60, 127, "테스트 위치");
        profileRepository.save(profile);

        // '오늘' 날씨는 맑음으로 DB에 미리 저장
        weatherRepository.save(Weather.builder()
                .location(new LocationInfo(37.0, 127.0, 60, 127))
                .forecastAt(LocalDate.now(ZoneId.of("Asia/Seoul")).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant())
                .forecastedAt(Instant.now())
                .precipitationType(PrecipitationType.NONE)
                .temperature(new TemperatureInfo(25, 20, 30, 0))
                .skyStatus(SkyStatus.CLEAR)
                .humidity(new HumidityInfo(80.0, 0.0))
                .precipitation(new PrecipitationInfo(PrecipitationType.NONE, 0.0, 0.0))
                .windSpeed(new WindSpeedInfo(1.0, "약함"))
                .build());

        // KMA API가 '내일은 비'라는 가짜 응답을 반환하도록 설정
        String todayStr = LocalDate.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String tomorrowStr = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String fakeRainyResponse = """
            {"response":{"body":{"items":{"item":[
                {"category":"PTY", "fcstDate":"%s", "fcstValue":"0"},
                {"category":"TMX", "fcstDate":"%s", "fcstValue":"30"},
                {"category":"PTY", "fcstDate":"%s", "fcstValue":"1"},
                {"category":"TMX", "fcstDate":"%s", "fcstValue":"25"}
            ]}}}}
            """.formatted(todayStr, todayStr, tomorrowStr, tomorrowStr);
        when(kmaApiClient.fetchWeatherForecast(anyInt(), anyInt())).thenReturn(fakeRainyResponse);

        // when: 날씨 배치 Job을 실행하면
        jobLauncher.run(fetchWeatherJob, new JobParametersBuilder()
                .addString("jobId", UUID.randomUUID().toString())
                .toJobParameters());

        // then: notifications 테이블에 알림이 1개 저장되어야 한다
        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getContent()).contains("비 또는 눈 소식");
    }
}