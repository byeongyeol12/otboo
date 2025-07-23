package com.codeit.otboo.domain.notification.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.entity.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
	@Mapping(target = "receiverId", source = "receiver.id")
	NotificationDto toNotificationDto(Notification notification);

	@Mapping(target = "receiverId", source = "receiver.id")
	List<NotificationDto> toNotificationDtoList(List<Notification> notification);
}
