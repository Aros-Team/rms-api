/* (C) 2026 */

package aros.services.rms.core.notification.domain.port.input;

import aros.services.rms.core.notification.domain.NotificationCommand;
import aros.services.rms.core.notification.domain.NotificationPage;
import aros.services.rms.core.notification.domain.NotificationView;

/** Application boundary for the notification feeds. */
public interface NotificationUseCase {

  /**
   * Persists a notification (unless its dedupe key already exists) and broadcasts it. A command
   * with {@code userId} is targeted at that user; otherwise it goes to the global admin feed.
   *
   * @param command producer payload
   * @return the created view, or empty when the command was a duplicate
   */
  java.util.Optional<NotificationView> create(NotificationCommand command);

  /**
   * Lists global notifications newest first.
   *
   * @param unreadOnly when true, excludes read notifications
   * @param limit page size
   * @param offset zero-based offset
   * @return the page plus the global unread count
   */
  NotificationPage list(boolean unreadOnly, int limit, int offset);

  /**
   * Lists notifications targeted at one user, newest first.
   *
   * @param userId recipient id
   * @param unreadOnly when true, excludes read notifications
   * @param limit page size
   * @param offset zero-based offset
   * @return the page plus the user's unread count
   */
  NotificationPage listForUser(Long userId, boolean unreadOnly, int limit, int offset);

  /**
   * Marks one global notification as read.
   *
   * @param id notification id
   * @return true when a row transitioned to read
   */
  boolean markRead(Long id);

  /**
   * Marks one notification owned by the user as read.
   *
   * @param userId recipient id
   * @param id notification id
   * @return true when a row transitioned to read
   */
  boolean markReadForUser(Long userId, Long id);

  /**
   * Marks every unread global notification as read.
   *
   * @return number of rows updated
   */
  int markAllRead();

  /**
   * Marks every unread notification of one user as read.
   *
   * @param userId recipient id
   * @return number of rows updated
   */
  int markAllReadForUser(Long userId);

  /** Deletes the whole global feed. */
  void clear();
}
