package com.workzeno.workzeno.notification.mapper;

import com.workzeno.workzeno.notification.domain.Notification;
import com.workzeno.workzeno.notification.dto.NotificationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "actorEmail", source = "actor.email")
    @Mapping(target = "read", expression = "java(notification.isRead())")
    NotificationResponse toResponse(Notification notification);
}