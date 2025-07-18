// WeatherAlertService.java
package com.codeit.otboo.domain.notification.service;

import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherAlertService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

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
                UUID eventId = UUID.nameUUIDFromBytes((tomorrow.toString() + ":" + location.x() + "," + location.y()).getBytes());

                List<Notification> notifications = new ArrayList<>();
                for (User user : usersToNotify) {
                    Notification notification = Notification.builder()
                            .receiver(user)
                            .title("내일 날씨 변화 알림")
                            .content(alertMessage)
                            .level(NotificationLevel.INFO)
                            .confirmed(false)
                            .eventRefId(eventId)
                            .build();
                    notifications.add(notification);
                }
                notificationRepository.saveAll(notifications);
                log.info("Generated {} weather alerts for location (x={}, y={})", notifications.size(), location.x(), location.y());
            }
        }
    }

    private String checkSpecialWeather(Weather today, Weather tomorrow) {
        if (today.getPrecipitationType() == PrecipitationType.NONE &&
                (tomorrow.getPrecipitationType() == PrecipitationType.RAIN || tomorrow.getPrecipitationType() == PrecipitationType.SNOW)) {
            return "내일은 비 또는 눈 소식이 있어요. 외출 시 우산을 챙겨주세요! ☂️";
        }

        double tempDiff = tomorrow.getTemperature().max() - today.getTemperature().max();
        if (tempDiff >= 5.0) {
            return "내일은 오늘보다 5도 이상 더워요! 시원하게 입으세요. ☀️";
        } else if (tempDiff <= -5.0) {
            return "내일은 오늘보다 5도 이상 추워져요! 따뜻하게 챙겨입으세요. ❄️";
        }

        return null;
    }
}