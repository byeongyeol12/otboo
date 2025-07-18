package com.codeit.otboo.domain.notification.service;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.domain.weather.entity.Weather;
import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;
import com.codeit.otboo.domain.weather.entity.vo.PrecipitationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherAlertService {

    private final UserRepository userRepository;
    private final NotificationService notificationService; // 기존 NotificationServiceImpl 주입

    @Transactional
    public void generateWeatherAlerts(List<Weather> savedWeathers) {
        if (savedWeathers.isEmpty()) return;

        Map<LocationInfo, Map<LocalDate, Weather>> weatherByLocationAndDate = savedWeathers.stream()
                .collect(Collectors.groupingBy(
                        Weather::getLocation,
                        Collectors.toMap(
                                w -> LocalDate.ofInstant(w.getForecastAt(), ZoneId.of("Asia/Seoul")),
                                w -> w
                        )
                ));

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate tomorrow = today.plusDays(1);

        for (Map.Entry<LocationInfo, Map<LocalDate, Weather>> entry : weatherByLocationAndDate.entrySet()) {
            LocationInfo location = entry.getKey();
            Map<LocalDate, Weather> weatherByDate = entry.getValue();

            Weather todayWeather = weatherByDate.get(today);
            Weather tomorrowWeather = weatherByDate.get(tomorrow);

            if (todayWeather == null || tomorrowWeather == null) continue;

            String alertMessage = checkSpecialWeather(todayWeather, tomorrowWeather);

            if (alertMessage != null) {
                List<User> usersToNotify = userRepository.findByProfileXAndProfileY(location.x(), location.y());
                for (User user : usersToNotify) {

                    NotificationDto notificationDto = new NotificationDto(
                            null,           // id (UUID)
                            null,           // createdAt (Instant)
                            user.getId(),   // receiverId (UUID)
                            "내일 날씨 변화 알림", // title (String)
                            alertMessage,   // content (String)
                            NotificationLevel.INFO // level (NotificationLevel)
                    );
                    notificationService.createAndSend(notificationDto);
                }
                log.info("Requested to send {} weather alerts for location (x={}, y={})", usersToNotify.size(), location.x(), location.y());
            }
        }
    }

    private String checkSpecialWeather(Weather today, Weather tomorrow) {
        // 1. 내일 비/눈 예보가 있으면, 오늘의 날씨와 상관없이 무조건 알림을 보냅니다.
        if (tomorrow.getPrecipitationType() == PrecipitationType.RAIN || tomorrow.getPrecipitationType() == PrecipitationType.SNOW) {
            return "내일 비 또는 눈 소식이 있어요. 외출 시 우산을 챙겨주세요! ☂️";
        }

        // 2. 강수 예보가 없을 경우에만, 기온 변화를 체크합니다. (중복 알림 방지)
        double tempDiff = tomorrow.getTemperature().max() - today.getTemperature().max();
        if (tempDiff >= 5.0) {
            return "내일은 오늘보다 5도 이상 더워요! 시원하게 입으세요. ☀️";
        } else if (tempDiff <= -5.0) {
            return "내일은 오늘보다 5도 이상 추워져요! 따뜻하게 챙겨입으세요. ❄️";
        }

        // 위 조건에 모두 해당하지 않으면 알림을 보내지 않음
        return null;
    }
}