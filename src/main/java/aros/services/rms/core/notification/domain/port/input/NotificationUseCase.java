/* (C) 2026 */

package aros.services.rms.core.notification.domain.port.input;

import aros.services.rms.core.notification.domain.NotificationCommand;
import aros.services.rms.core.notification.domain.NotificationPage;
import aros.services.rms.core.notification.domain.NotificationView;

/** Application boundary for the global notification feed. */
public interface NotificationUseCase {

  /**
   * Persists a notification (unless its dedupe key already exists) and broadcasts it.
   *
   * @param command producer payload
   * @return the created view, or empty when the command was a duplicate
   */
  java.util.Optional<NotificationView> create(NotificationCommand command);

  /**
   * Lists notifications newest first.
   *
   * @param unreadOnly when true, excludes read notifications
   * @param limit page size
   * @param offset zero-based offset
   * @return the page plus the global unread count
   */
  NotificationPage list(boolean unreadOnly, int limit, int offset);

  /**
   * Marks one notification as read.
   *
   * @param id notification id
   * @return true when a row transitioned to read
   */
  boolean markRead(Long id);

  /**
   * Marks every unread notification as read.
   *
   * @return number of rows updated
   */
  int markAllRead();

  /** Deletes the whole feed. */
  void clear();
}
