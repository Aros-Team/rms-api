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
    Instant createdAt,
    Long userId) {

  /** Command without a custom timestamp or recipient: global notification stamped {@code now}. */
  public NotificationCommand(
      String code,
      NotificationSeverity severity,
      String title,
      String message,
      String route,
      String dedupeKey) {
    this(code, severity, title, message, route, dedupeKey, null, null);
  }

  /** Command without a recipient: global notification. */
  public NotificationCommand(
      String code,
      NotificationSeverity severity,
      String title,
      String message,
      String route,
      String dedupeKey,
      Instant createdAt) {
    this(code, severity, title, message, route, dedupeKey, createdAt, null);
  }

  /** Command targeted at one user. */
  public NotificationCommand(
      String code,
      NotificationSeverity severity,
      String title,
      String message,
      String route,
      String dedupeKey,
      Long userId) {
    this(code, severity, title, message, route, dedupeKey, null, userId);
  }
}
