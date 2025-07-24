package com.codeit.otboo.domain.weather.batch;

import com.codeit.otboo.domain.notification.service.WeatherAlertService;
import com.codeit.otboo.domain.user.entity.Profile;
import com.codeit.otboo.domain.user.repository.ProfileRepository;
import com.codeit.otboo.domain.weather.component.KmaApiClient;
import com.codeit.otboo.domain.weather.component.WeatherParser;
import com.codeit.otboo.domain.weather.dto.KmaWeatherResponse;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.vo.*;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Configuration
//@EnableBatchProcessing
@EnableScheduling
@RequiredArgsConstructor
public class WeatherBatchConfig {

    private final KmaApiClient kmaApiClient;
    private final WeatherParser weatherParser;
    private final WeatherRepository weatherRepository;
    private final JobRepository jobRepository;
    private final ProfileRepository profileRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobLauncher jobLauncher;
    private final WeatherAlertService weatherAlertService; // ✨ 의존성 변경

    @Bean
    public Job fetchWeatherJob() {
        return new JobBuilder("fetchWeatherJob", jobRepository)
                .start(fetchWeatherStep())
                .build();
    }

    @Bean
    public Step fetchWeatherStep() {
        return new StepBuilder("fetchWeatherStep", jobRepository)
                .<LocationInfo, List<Weather>>chunk(10, transactionManager)
                .reader(locationInfoItemReader())
                .processor(weatherItemProcessor())
                .writer(weatherListItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<LocationInfo> locationInfoItemReader() {
        Collection<Profile> distinctProfiles = profileRepository.findAll().stream()
                .filter(p -> p.getX() != null && p.getY() != null)
                .collect(Collectors.toMap(
                        p -> p.getX() + "," + p.getY(),
                        p -> p,
                        (p1, p2) -> p1
                )).values();

        List<LocationInfo> locations = distinctProfiles.stream()
                .map(p -> new LocationInfo(p.getLatitude(), p.getLongitude(), p.getX(), p.getY()))
                .collect(Collectors.toList());

        log.info("Found {} unique and valid locations to fetch weather for.", locations.size());
        return new ListItemReader<>(locations);
    }

    @Bean
    @StepScope
    public ItemProcessor<LocationInfo, List<Weather>> weatherItemProcessor() {
        return locationInfo -> {
            String rawData = kmaApiClient.fetchWeatherForecast(locationInfo.x(), locationInfo.y());
            List<KmaWeatherResponse.Item> items = weatherParser.parse(rawData);
            if (items.isEmpty()) return null;

            Map<String, List<KmaWeatherResponse.Item>> itemsByDate = items.stream()
                    .collect(Collectors.groupingBy(KmaWeatherResponse.Item::getFcstDate));

            List<String> sortedDates = new ArrayList<>(itemsByDate.keySet());
            Collections.sort(sortedDates);

            List<Weather> dailySummaries = new ArrayList<>();
            Optional<Weather> yesterdayWeatherOpt = weatherRepository.findLatestWeatherByLocation(locationInfo.x(), locationInfo.y());

            Double prevDayMaxTemp = yesterdayWeatherOpt.map(w -> w.getTemperature().max()).orElse(null);
            Double prevDayHumidity = yesterdayWeatherOpt.map(w -> w.getHumidity().current()).orElse(null);

            for (String forecastDateStr : sortedDates) {
                List<KmaWeatherResponse.Item> dailyItems = itemsByDate.get(forecastDateStr);
                Map<String, String> dailyValues = dailyItems.stream()
                        .collect(Collectors.toMap(
                                KmaWeatherResponse.Item::getCategory,
                                KmaWeatherResponse.Item::getFcstValue,
                                (v1, v2) -> v1
                        ));

                double minTemp = parseKmaDouble(dailyValues.get("TMN"));
                double maxTemp = parseKmaDouble(dailyValues.get("TMX"));
                if (minTemp == 0.0 && maxTemp != 0.0) { minTemp = maxTemp; }
                if (maxTemp == 0.0 && minTemp != 0.0) { maxTemp = minTemp; }

                if (minTemp == 0.0 && maxTemp == 0.0) {
                    minTemp = dailyItems.stream().filter(it -> "TMP".equals(it.getCategory())).mapToDouble(it -> parseKmaDouble(it.getFcstValue())).min().orElse(0.0);
                    maxTemp = dailyItems.stream().filter(it -> "TMP".equals(it.getCategory())).mapToDouble(it -> parseKmaDouble(it.getFcstValue())).max().orElse(0.0);
                }

                double currentTemp = parseKmaDouble(dailyValues.getOrDefault("TMP", String.valueOf(maxTemp)));
                double currentHumidity = parseKmaDouble(dailyValues.get("REH"));

                double tempDiff = 0.0;
                double humDiff = 0.0;
                if (prevDayMaxTemp != null && maxTemp != 0.0) {
                    tempDiff = maxTemp - prevDayMaxTemp;
                }
                if (prevDayHumidity != null && currentHumidity != 0.0) {
                    humDiff = currentHumidity - prevDayHumidity;
                }

                TemperatureInfo tempInfo = new TemperatureInfo(currentTemp, minTemp, maxTemp, tempDiff);
                HumidityInfo humidityInfo = new HumidityInfo(currentHumidity, humDiff);

                PrecipitationType pty = mapToPrecipitationType(dailyValues.getOrDefault("PTY", "0"));
                SkyStatus sky = mapToSkyStatus(dailyValues.getOrDefault("SKY", "1"));
                double pcp = parseKmaDouble(dailyValues.get("PCP"));
                double pop = parseKmaDouble(dailyValues.get("POP"));
                double wsd = parseKmaDouble(dailyValues.get("WSD"));

                LocalDate forecastDate = LocalDate.parse(forecastDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));

                Weather dailyWeather = Weather.builder()
                        .location(locationInfo)
                        .temperature(tempInfo)
                        .humidity(humidityInfo)
                        .skyStatus(sky)
                        .precipitation(new PrecipitationInfo(pty, pcp, pop))
                        .windSpeed(new WindSpeedInfo(wsd, mapToWindSpeedAsWord(wsd)))
                        .precipitationType(pty)
                        .forecastedAt(Instant.now())
                        .forecastAt(forecastDate.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant())
                        .build();

                dailySummaries.add(dailyWeather);

                if (maxTemp != 0.0) prevDayMaxTemp = maxTemp;
                if (currentHumidity != 0.0) prevDayHumidity = currentHumidity;
            }
            return dailySummaries;
        };
    }

    @Bean
    public ItemWriter<List<Weather>> weatherListItemWriter() {
        return chunk -> {
            List<Weather> weathersToSave = chunk.getItems().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            weatherRepository.saveAll(weathersToSave);
            weatherAlertService.generateWeatherAlerts(weathersToSave);
        };
    }

    @Scheduled(cron = "0 10 2,5,8,11,14,17,20,23 * * *", zone = "Asia/Seoul")
    public void runFetchWeatherJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("scheduledTime", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();
            jobLauncher.run(fetchWeatherJob(), params);
            log.info("Scheduled weather fetch job has been run successfully.");
        } catch (Exception e) {
            log.error("Failed to run scheduled weather fetch job", e);
        }
    }

    // --- Helper Methods ---
    private double parseKmaDouble(String value) {
        if (value == null || value.contains("없음")) {
            return 0.0;
        }
        String numericValue = value.replaceAll("[^\\d.-]", "");
        if (numericValue.isEmpty() || "-".equals(numericValue)) {
            return 0.0;
        }
        try {
            return Double.parseDouble(numericValue);
        } catch (NumberFormatException e) {
            log.warn("Could not parse KMA double value: '{}'", value);
            return 0.0;
        }
    }

    private SkyStatus mapToSkyStatus(String skyCode) {
        return switch (skyCode) {
            case "1" -> SkyStatus.CLEAR;
            case "3" -> SkyStatus.MOSTLY_CLOUDY;
            case "4" -> SkyStatus.CLOUDY;
            default -> SkyStatus.CLEAR;
        };
    }

    private PrecipitationType mapToPrecipitationType(String ptyCode) {
        return switch (ptyCode) {
            case "1" -> PrecipitationType.RAIN;
            case "2" -> PrecipitationType.RAIN_SNOW;
            case "3" -> PrecipitationType.SNOW;
            case "4" -> PrecipitationType.SHOWER;
            default -> PrecipitationType.NONE;
        };
    }

    private String mapToWindSpeedAsWord(double speed) {
        if (speed < 4) return "약함";
        if (speed < 9) return "약간 강함";
        if (speed < 14) return "강함";
        return "매우 강함";
    }
}