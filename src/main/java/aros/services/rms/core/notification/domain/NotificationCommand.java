/* (C) 2026 */

package aros.services.rms.core.notification.domain;

import java.time.Instant;

/** Input to create a notification from a producer. */
public record NotificationCommand(
    String code,
    NotificationSeverity severity,
    String title,
    String message,
    String route,
    String dedupeKey,
    Instant createdAt) {

  /** Command without a custom timestamp: the service stamps {@code now}. */
  public NotificationCommand(
      String code,
      NotificationSeverity severity,
      String title,
      String message,
      String route,
      String dedupeKey) {
    this(code, severity, title, message, route, dedupeKey, null);
  }
}
