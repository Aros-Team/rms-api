/* (C) 2026 */

package aros.services.rms.core.notification.domain;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A persisted, user-facing notification.
 *
 * <p>Notifications are global to the installation: they describe operational events an
 * administrator must know about (orders, analytics alerts, broken business rules). The feed is not
 * scoped per user, so read state is shared, matching the single-tenant restaurant deployment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotification {

  private Long id;

  /** Stable machine code, e.g. {@code ORDER_READY} or {@code ANALYTICS_ALERT}. */
  private String code;

  private NotificationSeverity severity;
  private String title;
  private String message;

  /** In-app route the notification points at, or {@code null} when it has no destination. */
  private String route;

  /** Optional idempotency key owned by the producer (unique when present). */
  private String dedupeKey;

  private Instant createdAt;
  private Instant readAt;

  /** True when the notification has been read. */
  public boolean isRead() {
    return readAt != null;
  }
}
