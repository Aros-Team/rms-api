/* (C) 2026 */

package aros.services.rms.core.notification.infrastructure.persistence.mapper;

import aros.services.rms.core.notification.domain.UserNotification;
import aros.services.rms.core.notification.infrastructure.persistence.entity.UserNotificationEntity;
import org.springframework.stereotype.Component;

/** Maps between the notification domain and its JPA entity. */
@Component
public class UserNotificationMapper {

  /** Converts an entity to the domain model. */
  public UserNotification toDomain(UserNotificationEntity entity) {
    return UserNotification.builder()
        .id(entity.getId())
        .userId(entity.getUserId())
        .code(entity.getCode())
        .severity(entity.getSeverity())
        .title(entity.getTitle())
        .message(entity.getMessage())
        .route(entity.getRoute())
        .dedupeKey(entity.getDedupeKey())
        .createdAt(entity.getCreatedAt())
        .readAt(entity.getReadAt())
        .build();
  }

  /** Converts a domain notification to a new entity. */
  public UserNotificationEntity toEntity(UserNotification notification) {
    return UserNotificationEntity.builder()
        .id(notification.getId())
        .userId(notification.getUserId())
        .code(notification.getCode())
        .severity(notification.getSeverity())
        .title(notification.getTitle())
        .message(notification.getMessage())
        .route(notification.getRoute())
        .dedupeKey(notification.getDedupeKey())
        .createdAt(notification.getCreatedAt())
        .readAt(notification.getReadAt())
        .build();
  }
}
