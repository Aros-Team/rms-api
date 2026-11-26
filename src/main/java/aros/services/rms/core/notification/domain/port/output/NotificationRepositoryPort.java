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
   * Finds global (admin) notifications newest first.
   *
   * @param unreadOnly exclude read rows when true
   * @param limit page size
   * @param offset zero-based offset
   * @return matching rows
   */
  List<UserNotification> find(boolean unreadOnly, int limit, int offset);

  /**
   * Counts global notifications.
   *
   * @param unreadOnly count only unread rows when true
   * @return row count
   */
  long count(boolean unreadOnly);

  /**
   * Finds notifications targeted at one user, newest first.
   *
   * @param userId recipient id
   * @param unreadOnly exclude read rows when true
   * @param limit page size
   * @param offset zero-based offset
   * @return matching rows
   */
  List<UserNotification> findForUser(Long userId, boolean unreadOnly, int limit, int offset);

  /**
   * Counts notifications targeted at one user.
   *
   * @param userId recipient id
   * @param unreadOnly count only unread rows when true
   * @return row count
   */
  long countForUser(Long userId, boolean unreadOnly);

  /**
   * Marks one global row as read when still unread.
   *
   * @param id notification id
   * @param readAt transition timestamp
   * @return number of rows updated
   */
  int markRead(Long id, Instant readAt);

  /**
   * Marks one user-owned row as read when still unread.
   *
   * @param userId recipient id
   * @param id notification id
   * @param readAt transition timestamp
   * @return number of rows updated
   */
  int markReadForUser(Long userId, Long id, Instant readAt);

  /**
   * Marks every unread global row as read.
   *
   * @param readAt transition timestamp
   * @return number of rows updated
   */
  int markAllRead(Instant readAt);

  /**
   * Marks every unread row of one user as read.
   *
   * @param userId recipient id
   * @param readAt transition timestamp
   * @return number of rows updated
   */
  int markAllReadForUser(Long userId, Instant readAt);

  /** Deletes the global admin feed. */
  void clear();
}
