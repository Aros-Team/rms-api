/* (C) 2026 */

package aros.services.rms.core.notification.domain;

import java.util.List;

/** One page of notifications plus the current unread total for the badge. */
public record NotificationPage(List<NotificationView> items, PageInfo page, long unreadCount) {

  /** Pagination metadata. */
  public record PageInfo(int limit, int offset, long total) {}
}
