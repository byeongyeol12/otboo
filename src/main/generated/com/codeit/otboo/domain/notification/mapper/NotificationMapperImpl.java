package com.codeit.otboo.domain.notification.mapper;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.entity.NotificationLevel;
import com.codeit.otboo.domain.user.entity.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-07T10:10:12+0900",
    comments = "version: 1.5.3.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.2.jar, environment: Java 17.0.15 (Homebrew)"
)
@Component
public class NotificationMapperImpl implements NotificationMapper {

    @Override
    public NotificationDto toNotificationDto(Notification notification) {
        if ( notification == null ) {
            return null;
        }

        UUID receiverId = null;
        UUID id = null;
        Instant createdAt = null;
        String title = null;
        String content = null;
        NotificationLevel level = null;

        receiverId = notificationReceiverId( notification );
        id = notification.getId();
        createdAt = notification.getCreatedAt();
        title = notification.getTitle();
        content = notification.getContent();
        level = notification.getLevel();

        NotificationDto notificationDto = new NotificationDto( id, createdAt, receiverId, title, content, level );

        return notificationDto;
    }

    @Override
    public List<NotificationDto> toNotificationDtoList(List<Notification> notification) {
        if ( notification == null ) {
            return null;
        }

        List<NotificationDto> list = new ArrayList<NotificationDto>( notification.size() );
        for ( Notification notification1 : notification ) {
            list.add( toNotificationDto( notification1 ) );
        }

        return list;
    }

    private UUID notificationReceiverId(Notification notification) {
        if ( notification == null ) {
            return null;
        }
        User receiver = notification.getReceiver();
        if ( receiver == null ) {
            return null;
        }
        UUID id = receiver.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
