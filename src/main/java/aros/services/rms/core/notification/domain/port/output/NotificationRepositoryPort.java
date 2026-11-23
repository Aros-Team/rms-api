/* (C) 2026 */

package aros.services.rms.core.notification.domain.port.output;

import aros.services.rms.core.notification.domain.UserNotification;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Persistence boundary for the notification feed. */
public interface NotificationRepositoryPort {

  /**
   * Inserts the notification unless its dedupe key is already present.
   *
   * @param notification notification to persist
   * @return the persisted row, or empty on duplicate
   */
  Optional<UserNotification> insertIfAbsent(UserNotification notification);

  /**
   * Finds notifications newest first.
   *
   * @param unreadOnly exclude read rows when true
   * @param limit page size
   * @param offset zero-based offset
   * @return matching rows
   */
  List<UserNotification> find(boolean unreadOnly, int limit, int offset);

  /**
   * Counts notifications.
   *
   * @param unreadOnly count only unread rows when true
   * @return row count
   */
  long count(boolean unreadOnly);

  /**
   * Marks one row as read when still unread.
   *
   * @param id notification id
   * @param readAt transition timestamp
   * @return number of rows updated
   */
  int markRead(Long id, Instant readAt);

  /**
   * Marks every unread row as read.
   *
   * @param readAt transition timestamp
   * @return number of rows updated
   */
  int markAllRead(Instant readAt);

  /** Deletes every notification. */
  void clear();
}
