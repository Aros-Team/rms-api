/* (C) 2026 */

package aros.services.rms.core.notification.domain;

import java.time.Instant;

/** Wire-ready view of a notification. */
public record NotificationView(
    Long id,
    String code,
    NotificationSeverity severity,
    String title,
    String message,
    String route,
    Instant createdAt,
    boolean read) {

  /** Projects a domain notification into its wire view. */
  public static NotificationView from(UserNotification notification) {
    return new NotificationView(
        notification.getId(),
        notification.getCode(),
        notification.getSeverity(),
        notification.getTitle(),
        notification.getMessage(),
        notification.getRoute(),
        notification.getCreatedAt(),
        notification.isRead());
  }
}
